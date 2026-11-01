package com.example.ime.service

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.ime.CursorMovement
import com.example.ime.NavigationBarPolicy
import com.example.ime.engine.VietnameseCharUtils
import com.example.ime.engine.VietnameseDictionary
import com.example.ime.engine.VietnameseEngine
import com.example.ime.engine.TelexEngine
import com.example.ime.model.KeyboardPreferences
import com.example.ime.model.TonePlacement
import com.example.ime.model.TypingMethod
import com.example.ime.model.WorldLanguagesRepository
import com.example.ime.ui.ComposeKeyboardView

class VietnameseInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    companion object {
        private const val TAG = "Anykey26"
        private const val MAX_TELEX_WORD_LENGTH = 128
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var preferences: KeyboardPreferences
    private lateinit var engine: VietnameseEngine
    private val telexEngine = TelexEngine()

    private var composingWordState by mutableStateOf("")
    private var suggestionsState by mutableStateOf<List<String>>(emptyList())
    private var lastCommittedWord: String? = null
    private var hideVirtualNavigationBarState by mutableStateOf(false)
    private var currentImeActionState by mutableStateOf(EditorInfo.IME_ACTION_NONE)
    private var isMultilineState by mutableStateOf(false)
    private var cursorAtSentenceStartState by mutableStateOf(false)
    private var cursorCapsUpdateTokenState by mutableIntStateOf(0)
    private var deleteSelectionAnchor: Int? = null
    private var deleteSelectionEnd = 0
    private var deleteSelectionBounds: IntRange? = null
    private var fallbackShiftHeld = false
    private var fallbackSelectionSteps = 0

    override fun onCreate() {
        super.onCreate()
        try {
            if (!savedStateRegistry.isRestored) {
                savedStateRegistryController.performRestore(Bundle())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring savedStateRegistry", e)
        }

        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling ON_CREATE", e)
        }

        try {
            preferences = KeyboardPreferences.getInstance(this)
            val initialSettings = preferences.settings.value
            engine = VietnameseEngine(
                typingMethod = initialSettings.typingMethod,
                tonePlacement = if (initialSettings.tonePlacementEnabled) {
                    initialSettings.tonePlacement
                } else {
                    TonePlacement.MODERN
                }
            )
            telexEngine.configure(
                if (initialSettings.tonePlacementEnabled) initialSettings.tonePlacement
                else TonePlacement.MODERN
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing preferences/engine", e)
        }
    }

    private fun initWindowDecorView() {
        try {
            val win = window?.window ?: return
            win.attributes = win.attributes.apply {
                preferredRefreshRate = 0f
            }
            val decorView = win.decorView ?: return
            ViewCompat.setOnApplyWindowInsetsListener(decorView) { _, insets ->
                hideVirtualNavigationBarState =
                    NavigationBarPolicy.shouldHideVirtualNavigationBar(this, insets)
                insets
            }
            ViewCompat.requestApplyInsets(decorView)
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error binding decorView", e)
        }
    }

    override fun onCreateInputView(): View {
        initWindowDecorView()

        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Exception) {
            Log.e(TAG, "Error dispatching ON_START/ON_RESUME in onCreateInputView", e)
        }

        if (!::preferences.isInitialized) {
            preferences = KeyboardPreferences.getInstance(this)
        }
        if (!::engine.isInitialized) {
            val s = preferences.settings.value
            engine = VietnameseEngine(
                s.typingMethod,
                if (s.tonePlacementEnabled) s.tonePlacement else TonePlacement.MODERN
            )
            telexEngine.configure(
                if (s.tonePlacementEnabled) s.tonePlacement else TonePlacement.MODERN
            )
        }

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@VietnameseInputMethodService.lifecycle))
            setViewTreeLifecycleOwner(this@VietnameseInputMethodService)
            setViewTreeViewModelStoreOwner(this@VietnameseInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@VietnameseInputMethodService)

            setContent {
                MaterialTheme {
                    val settings by preferences.settings.collectAsState()
                    LaunchedEffect(settings.showSuggestions, settings.smartFeaturesEnabled) {
                        if (!settings.showSuggestions || !settings.smartFeaturesEnabled) {
                            suggestionsState = emptyList()
                            setCandidatesViewShown(false)
                        }
                    }

                    // Keep engine synced with user preferences
                    engine.typingMethod = settings.typingMethod
                    engine.tonePlacement = if (settings.tonePlacementEnabled) {
                        settings.tonePlacement
                    } else {
                        TonePlacement.MODERN
                    }
                    telexEngine.configure(engine.tonePlacement)

                    ComposeKeyboardView(
                        settings = settings,
                        autoCapitalizationAtSentenceStart = cursorAtSentenceStartState,
                        autoCapitalizationUpdateToken = cursorCapsUpdateTokenState,
                        composingWord = composingWordState,
                        suggestions = suggestionsState,
                        hasActiveSelection = { hasActiveSelection() },
                        onKeyTyped = { c -> handleKeyTyped(c) },
                        onEmojiSelected = { emoji ->
                            preferences.updateSettings { settings ->
                                settings.copy(
                                    recentEmojis = (listOf(emoji) + settings.recentEmojis.filterNot { it == emoji })
                                        .take(24)
                                )
                            }
                            handleEmojiSelected(emoji)
                        },
                        onBackspace = { handleBackspace() },
                        onBackspaceDown = { handleBackspaceDown() },
                        onStartDeleteSelection = { beginDeleteSelection() },
                        onMoveDeleteSelection = { moveDeleteSelection(it) },
                        onFinishDeleteSelection = { finishDeleteSelection() },
                        onSpace = { handleSpace() },
                        onEnter = { handleEnter() },
                        onSelectSuggestion = { suggestion -> handleSelectSuggestion(suggestion) },
                        onMoveCursorLeft = { moveCursorLeft() },
                        onMoveCursorRight = { moveCursorRight() },
                        onMoveCursorByTouchpad = { horizontal, vertical ->
                            moveCursorByTouchpad(horizontal, vertical)
                        },
                        onMoveCursorStart = { moveCursorStart() },
                        onMoveCursorEnd = { moveCursorEnd() },
                        onSelectAll = { selectAll() },
                        onPaste = { paste() },
                        onHideKeyboard = { requestHideSelf(0) },
                        onCycleLanguage = { cycleNextLanguage() },
                        onToggleTypingMethod = {
                            val current = settings.typingMethod
                            val next = when (current) {
                                TypingMethod.TELEX -> TypingMethod.VNI
                                TypingMethod.VNI -> TypingMethod.OFF
                                TypingMethod.OFF -> TypingMethod.TELEX
                            }
                            preferences.updateSettings { it.copy(typingMethod = next) }
                        },
                        onUpdateAlignment = { newAlignment ->
                            preferences.updateSettings { it.copy(alignment = newAlignment) }
                        },
                        onOpenSettings = {
                            val intent = Intent(this@VietnameseInputMethodService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        },
                        hideVirtualNavigationBar = hideVirtualNavigationBarState,
                        imeAction = currentImeActionState,
                        isMultiline = isMultilineState,
                        onUpdateDisplayStyle = { newStyle ->
                            preferences.updateSettings { it.copy(displayStyle = newStyle) }
                        }
                    )
                }
            }
        }

        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        initWindowDecorView()
        if (info != null) {
            val isMulti = (info.inputType and android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
            val rawAction = info.imeOptions and EditorInfo.IME_MASK_ACTION
            val hasNoEnterAction = (info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
            currentImeActionState = if (isMulti && hasNoEnterAction) EditorInfo.IME_ACTION_NONE else rawAction
            isMultilineState = isMulti
        } else {
            currentImeActionState = EditorInfo.IME_ACTION_NONE
            isMultilineState = false
        }
        updateAutoCapitalizationFromCursor(info)
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Exception) {
            Log.e(TAG, "Error onStartInputView", e)
        }
        resetComposing()
        if (!suggestionsEnabled()) {
            suggestionsState = emptyList()
            setCandidatesViewShown(false)
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        initWindowDecorView()
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Exception) {
            Log.e(TAG, "Error onWindowShown", e)
        }
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        } catch (e: Exception) {
            Log.e(TAG, "Error onWindowHidden", e)
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        commitComposingWord()
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        } catch (e: Exception) {
            Log.e(TAG, "Error onFinishInputView", e)
        }
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
        updateAutoCapitalizationFromCursor(currentInputEditorInfo)
        if (!::engine.isInitialized || !isComposingActive()) return

        if (candidatesStart < 0 ||
            candidatesEnd < candidatesStart ||
            newSelStart != candidatesEnd ||
            newSelEnd != candidatesEnd
        ) {
            resetComposing()
            currentInputConnection?.finishComposingText()
        }
    }

    private fun updateAutoCapitalizationFromCursor(info: EditorInfo?) {
        val capsMode = if (info != null) {
            currentInputConnection?.getCursorCapsMode(info.inputType) ?: 0
        } else {
            0
        }
        cursorAtSentenceStartState = (capsMode and TextUtils.CAP_MODE_SENTENCES) != 0
        cursorCapsUpdateTokenState++
    }

    override fun onDestroy() {
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Error onDestroy", e)
        }
        if (::engine.isInitialized) {
            engine.close()
        }
        telexEngine.close()
        super.onDestroy()
    }

    private fun handleKeyTyped(c: Char) {
        val ic = currentInputConnection ?: return
        if (engine.typingMethod == TypingMethod.TELEX) {
            handleTelexKeyTyped(c, ic)
            return
        }

        when (val result = engine.processChar(c)) {
            is VietnameseEngine.ProcessResult.Composing -> {
                composingWordState = result.text
                refreshInputSelection(ic)
                ic.setComposingText(result.text, 1)
                updateSuggestions(result.text)
            }
            is VietnameseEngine.ProcessResult.WordBoundary -> {
                if (result.committedWord.isNotEmpty()) {
                    commitTextAtCurrentSelection(ic, result.committedWord)
                    lastCommittedWord = result.committedWord
                }
                commitTextAtCurrentSelection(ic, result.boundaryChar.toString())
                resetComposing()
            }
            is VietnameseEngine.ProcessResult.Unhandled -> {
                commitComposingWord()
                commitTextAtCurrentSelection(ic, c.toString())
                resetComposing()
            }
        }
    }

    private fun handleTelexKeyTyped(
        c: Char,
        ic: android.view.inputmethod.InputConnection
    ) {
        if (!telexEngine.hasInput && c.isLetter()) {
            val textBeforeCursor = ic.getTextBeforeCursor(MAX_TELEX_WORD_LENGTH, 0)
                ?.toString()
                .orEmpty()
            val previousWord = textBeforeCursor.takeLastWhile(Char::isLetter)
            if (previousWord.isNotEmpty()) {
                val extracted = getExtractedText(ic)
                if (extracted != null &&
                    extracted.selectionStart >= 0 &&
                    extracted.selectionStart == extracted.selectionEnd
                ) {
                    val cursor = extracted.startOffset + extracted.selectionEnd
                    telexEngine.reset()
                    VietnameseCharUtils.toTelexSequence(previousWord).forEach {
                        telexEngine.processKey(it.toString())
                    }
                    val result = telexEngine.processKey(c.toString())
                    if (ic.setSelection(cursor - previousWord.length, cursor)) {
                        composingWordState = result
                        ic.setComposingText(result, 1)
                        updateSuggestions(result)
                        return
                    }
                    Log.w(TAG, "Input connection rejected selection for Telex recomposition")
                    telexEngine.reset()
                }
            }
        }

        if (c.isLetter()) {
            val result = telexEngine.processKey(c.toString())
            composingWordState = result
            refreshInputSelection(ic)
            ic.setComposingText(result, 1)
            updateSuggestions(result)
        } else {
            if (telexEngine.hasInput) {
                val finalWord = telexEngine.composingText
                commitTextAtCurrentSelection(ic, finalWord)
                lastCommittedWord = finalWord
            }
            commitTextAtCurrentSelection(ic, c.toString())
            resetComposing()
        }
    }

    private fun handleEmojiSelected(emojiString: String) {
        val ic = currentInputConnection ?: return
        commitComposingWord()
        telexEngine.reset()
        refreshInputSelection(ic)
        ic.commitText(emojiString, 1)
        updateSuggestions("")
    }

    private fun hasActiveSelection(): Boolean {
        val ic = currentInputConnection ?: return false
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) return true
        val extracted = getExtractedText(ic)
        return extracted != null &&
            extracted.selectionStart >= 0 &&
            extracted.selectionEnd >= 0 &&
            extracted.selectionStart != extracted.selectionEnd
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return

        // YÊU CẦU 4: Nếu đang có văn bản được bôi đen/chọn, xóa ngay lập tức và thoát
        val selectedText = ic.getSelectedText(0)
        val extracted = getExtractedText(ic)
        val isTextSelected = (!selectedText.isNullOrEmpty()) ||
            (extracted != null &&
                extracted.selectionStart >= 0 &&
                extracted.selectionEnd >= 0 &&
                extracted.selectionStart != extracted.selectionEnd)

        if (isTextSelected) {
            resetComposing()
            ic.commitText("", 1)
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            updateSuggestions("")
            return
        }

        refreshInputSelection(ic)

        if (isComposingActive()) {
            val handled = if (engine.typingMethod == TypingMethod.TELEX) {
                telexEngine.hasInput
            } else {
                engine.processBackspace()
            }
            if (handled) {
                val composed = currentComposedWord()
                if (engine.typingMethod == TypingMethod.TELEX) {
                    composingWordState = telexEngine.backspace()
                } else {
                    composingWordState = composed
                }
                if (composingWordState.isEmpty()) {
                    ic.commitText("", 1)
                    resetComposing()
                } else {
                    ic.setComposingText(composingWordState, 1)
                    updateSuggestions(composingWordState)
                }
            } else {
                resetComposing()
                ic.deleteSurroundingText(1, 0)
            }
        } else {
            ic.deleteSurroundingText(1, 0)
            updateSuggestions("")
        }
    }

    private fun handleBackspaceDown(): Boolean {
        val ic = currentInputConnection ?: return false
        val extracted = getExtractedText(ic) ?: return false
        if (extracted.selectionStart < 0 ||
            extracted.selectionEnd < 0 ||
            extracted.selectionStart == extracted.selectionEnd
        ) {
            return false
        }

        val downSent = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        val upSent = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
        if (!downSent || !upSent) {
            Log.w(TAG, "Input connection rejected selected-text delete key event")
        }
        resetComposing()
        deleteSelectionAnchor = null
        deleteSelectionBounds = null
        fallbackShiftHeld = false
        fallbackSelectionSteps = 0
        return true
    }

    private fun beginDeleteSelection() {
        val ic = currentInputConnection ?: return
        if (hasActiveSelection()) return
        commitComposingWord()
        deleteSelectionAnchor = null
        deleteSelectionBounds = null
        fallbackShiftHeld = false
        fallbackSelectionSteps = 0

        val extracted = getExtractedText(ic)
        if (extracted != null &&
            extracted.text != null &&
            extracted.selectionStart >= 0 &&
            extracted.selectionEnd >= 0
        ) {
            val offset = extracted.startOffset
            deleteSelectionAnchor = offset + extracted.selectionStart
            deleteSelectionEnd = offset + extracted.selectionEnd
            deleteSelectionBounds = offset..(offset + extracted.text.length)
            return
        }

        fallbackShiftHeld = ic.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT)
        )
    }

    private fun moveDeleteSelection(step: Int) {
        val ic = currentInputConnection ?: return
        val anchor = deleteSelectionAnchor
        if (anchor != null) {
            val bounds = deleteSelectionBounds ?: return
            deleteSelectionEnd = (deleteSelectionEnd + step)
                .coerceIn(bounds.first, bounds.last)
            val selectionStart = minOf(anchor, deleteSelectionEnd)
            val selectionEnd = maxOf(anchor, deleteSelectionEnd)
            if (!ic.setSelection(selectionStart, selectionEnd)) {
                Log.w(TAG, "Input connection rejected swipe-delete selection update")
            }
        } else if (fallbackShiftHeld) {
            sendSelectionArrow(ic, if (step > 0) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT)
            fallbackSelectionSteps += step
        }
    }

    private fun finishDeleteSelection() {
        val ic = currentInputConnection ?: return
        if (fallbackShiftHeld) {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT))
        }
        val hasSelection = deleteSelectionAnchor?.let { it != deleteSelectionEnd }
            ?: (fallbackShiftHeld && fallbackSelectionSteps != 0)
        if (hasSelection) {
            val downSent = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            val upSent = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
            if (!downSent || !upSent) {
                Log.w(TAG, "Input connection rejected swipe-delete key event")
            }
            updateSuggestions("")
        }
        deleteSelectionAnchor = null
        deleteSelectionBounds = null
        fallbackShiftHeld = false
        fallbackSelectionSteps = 0
    }

    private fun sendSelectionArrow(ic: android.view.inputmethod.InputConnection, keyCode: Int) {
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    private fun moveCursorByTouchpad(horizontalSteps: Int, verticalSteps: Int) {
        commitComposingWord()
        val ic = currentInputConnection ?: return
        val extracted = getExtractedText(ic)
        val text = extracted?.text
        if (extracted != null && text != null &&
            extracted.selectionEnd >= 0
        ) {
            val localPosition = CursorMovement.move(
                text = text,
                currentPosition = extracted.selectionEnd,
                horizontalSteps = horizontalSteps,
                verticalSteps = verticalSteps
            )
            val absolutePosition = extracted.startOffset + localPosition
            if (!ic.setSelection(absolutePosition, absolutePosition)) {
                Log.w(TAG, "Input connection rejected touchpad cursor update")
            }
            return
        }

        repeat(kotlin.math.abs(horizontalSteps)) {
            sendDownUpKeyEvents(
                if (horizontalSteps > 0) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
            )
        }
        repeat(kotlin.math.abs(verticalSteps)) {
            sendDownUpKeyEvents(
                if (verticalSteps > 0) KeyEvent.KEYCODE_DPAD_DOWN else KeyEvent.KEYCODE_DPAD_UP
            )
        }
    }

    private fun handleSpace() {
        val ic = currentInputConnection ?: return
        if (isComposingActive()) {
            val finalWord = currentComposedWord()
            commitTextAtCurrentSelection(ic, "$finalWord ")
            lastCommittedWord = finalWord
            resetComposing()
        } else {
            commitTextAtCurrentSelection(ic, " ")
            if (engine.typingMethod == TypingMethod.TELEX) {
                telexEngine.reset()
            }
            updateSuggestions("")
        }
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        if (isComposingActive()) {
            commitComposingWord()
        } else if (engine.typingMethod == TypingMethod.TELEX) {
            telexEngine.reset()
        }
        val info = currentInputEditorInfo
        val isMulti = info?.let { (it.inputType and android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0 } ?: false
        val imeAction = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE

        when {
            !isMulti && imeAction == EditorInfo.IME_ACTION_SEARCH -> ic.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
            !isMulti && imeAction == EditorInfo.IME_ACTION_SEND -> ic.performEditorAction(EditorInfo.IME_ACTION_SEND)
            !isMulti && imeAction == EditorInfo.IME_ACTION_DONE -> ic.performEditorAction(EditorInfo.IME_ACTION_DONE)
            !isMulti && imeAction == EditorInfo.IME_ACTION_GO -> ic.performEditorAction(EditorInfo.IME_ACTION_GO)
            !isMulti && imeAction != EditorInfo.IME_ACTION_NONE && imeAction != EditorInfo.IME_ACTION_UNSPECIFIED -> ic.performEditorAction(imeAction)
            else -> sendKeyChar('\n')
        }
    }

    private fun handleSelectSuggestion(suggestion: String) {
        val ic = currentInputConnection ?: return
        commitTextAtCurrentSelection(ic, "$suggestion ")
        lastCommittedWord = suggestion
        resetComposing()
    }

    private fun commitComposingWord() {
        val ic = currentInputConnection ?: return
        if (isComposingActive()) {
            val word = currentComposedWord()
            commitTextAtCurrentSelection(ic, word)
            lastCommittedWord = word
            resetComposing()
        }
    }

    private fun refreshInputSelection(
        ic: android.view.inputmethod.InputConnection
    ) {
        val extracted = getExtractedText(ic) ?: return
        if (extracted.selectionStart < 0 || extracted.selectionEnd < 0) return
        val selectionStart = extracted.startOffset + extracted.selectionStart
        val selectionEnd = extracted.startOffset + extracted.selectionEnd
        if (!ic.setSelection(selectionStart, selectionEnd)) {
            Log.w(TAG, "Input connection rejected refreshed selection")
        }
    }

    private fun commitTextAtCurrentSelection(
        ic: android.view.inputmethod.InputConnection,
        text: CharSequence
    ) {
        refreshInputSelection(ic)
        ic.commitText(text, 1)
    }

    private fun getExtractedText(
        ic: android.view.inputmethod.InputConnection
    ): ExtractedText? {
        val request = ExtractedTextRequest().apply {
            hintMaxChars = 20_000
            hintMaxLines = 1_000
        }

        return ic.getExtractedText(request, 0)
    }

    private fun resetComposing() {
        engine.reset()
        telexEngine.reset()
        composingWordState = ""
        val settings = preferences.settings.value
        if (!settings.showSuggestions || !settings.smartFeaturesEnabled) {
            suggestionsState = emptyList()
            setCandidatesViewShown(false)
        } else {
            updateSuggestions("")
        }
    }

    private fun isComposingActive(): Boolean =
        if (engine.typingMethod == TypingMethod.TELEX) telexEngine.hasInput
        else engine.isWordActive()

    private fun currentComposedWord(): String =
        if (engine.typingMethod == TypingMethod.TELEX) telexEngine.composingText
        else engine.composedWord

    private fun updateSuggestions(currentWord: String) {
        if (!suggestionsEnabled()) {
            suggestionsState = emptyList()
            setCandidatesViewShown(false)
            return
        }
        suggestionsState = VietnameseDictionary.getSuggestions(
            currentWord = currentWord,
            previousWord = lastCommittedWord
        )
        setCandidatesViewShown(suggestionsState.isNotEmpty())
    }

    private fun suggestionsEnabled(): Boolean {
        val settings = preferences.settings.value
        return settings.showSuggestions && settings.smartFeaturesEnabled
    }

    private fun cycleNextLanguage() {
        val settings = preferences.settings.value
        val enabledList = settings.enabledLanguages.toList()
        if (enabledList.isEmpty()) return
        val currentIndex = enabledList.indexOf(settings.activeLanguageId)
        val nextIndex = (currentIndex + 1) % enabledList.size
        val nextLangId = enabledList[nextIndex]

        preferences.updateSettings {
            val typingMethod = when (nextLangId) {
                "vi_telex" -> TypingMethod.TELEX
                "vi_vni" -> TypingMethod.VNI
                else -> TypingMethod.OFF
            }
            it.copy(
                activeLanguageId = nextLangId,
                typingMethod = typingMethod
            )
        }

    }

    private fun moveCursorLeft() {
        commitComposingWord()
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
    }

    private fun moveCursorRight() {
        commitComposingWord()
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
    }

    private fun moveCursorStart() {
        commitComposingWord()
        sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_HOME)
    }

    private fun moveCursorEnd() {
        commitComposingWord()
        sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_END)
    }

    private fun selectAll() {
        commitComposingWord()
        currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
    }

    private fun paste() {
        commitComposingWord()
        currentInputConnection?.performContextMenuAction(android.R.id.paste)
    }

}
