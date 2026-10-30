package com.example.ime.service

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.example.ime.engine.VietnameseDictionary
import com.example.ime.engine.VietnameseEngine
import com.example.ime.model.KeyboardPreferences
import com.example.ime.model.TypingMethod
import com.example.ime.model.WorldLanguagesRepository
import com.example.ime.ui.ComposeKeyboardView

class VietnameseInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    companion object {
        private const val TAG = "AnyKeyboard26"
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var preferences: KeyboardPreferences
    private lateinit var engine: VietnameseEngine

    private var composingWordState by mutableStateOf("")
    private var suggestionsState by mutableStateOf<List<String>>(emptyList())
    private var lastCommittedWord: String? = null

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
                tonePlacement = initialSettings.tonePlacement
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing preferences/engine", e)
        }
    }

    private fun initWindowDecorView() {
        try {
            val win = window?.window ?: return
            val decorView = win.decorView ?: return
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
            engine = VietnameseEngine(s.typingMethod, s.tonePlacement)
        }

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@VietnameseInputMethodService.lifecycle))
            setViewTreeLifecycleOwner(this@VietnameseInputMethodService)
            setViewTreeViewModelStoreOwner(this@VietnameseInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@VietnameseInputMethodService)

            setContent {
                MaterialTheme {
                    val settings by preferences.settings.collectAsState()

                    // Keep engine synced with user preferences
                    engine.typingMethod = settings.typingMethod
                    engine.tonePlacement = settings.tonePlacement

                    ComposeKeyboardView(
                        settings = settings,
                        composingWord = composingWordState,
                        suggestions = suggestionsState,
                        onKeyTyped = { c -> handleKeyTyped(c) },
                        onBackspace = { handleBackspace() },
                        onFastDelete = { handleFastDelete() },
                        onSpace = { handleSpace() },
                        onEnter = { handleEnter() },
                        onSelectSuggestion = { suggestion -> handleSelectSuggestion(suggestion) },
                        onMoveCursorLeft = { moveCursorLeft() },
                        onMoveCursorRight = { moveCursorRight() },
                        onMoveCursorStart = { moveCursorStart() },
                        onMoveCursorEnd = { moveCursorEnd() },
                        onSelectAll = { selectAll() },
                        onPaste = { paste() },
                        onSwitchIme = { switchIme() },
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
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Exception) {
            Log.e(TAG, "Error onStartInputView", e)
        }
        resetComposing()
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

    override fun onDestroy() {
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Error onDestroy", e)
        }
        super.onDestroy()
    }

    private fun handleKeyTyped(c: Char) {
        val ic = currentInputConnection ?: return

        when (val result = engine.processChar(c)) {
            is VietnameseEngine.ProcessResult.Composing -> {
                composingWordState = result.text
                ic.setComposingText(result.text, 1)
                updateSuggestions(result.text)
            }
            is VietnameseEngine.ProcessResult.WordBoundary -> {
                if (result.committedWord.isNotEmpty()) {
                    ic.commitText(result.committedWord, 1)
                    lastCommittedWord = result.committedWord
                }
                ic.commitText(result.boundaryChar.toString(), 1)
                resetComposing()
            }
            is VietnameseEngine.ProcessResult.Unhandled -> {
                commitComposingWord()
                ic.commitText(c.toString(), 1)
                resetComposing()
            }
        }
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return

        if (engine.isWordActive()) {
            val handled = engine.processBackspace()
            if (handled) {
                composingWordState = engine.composedWord
                if (engine.composedWord.isEmpty()) {
                    ic.commitText("", 1)
                    resetComposing()
                } else {
                    ic.setComposingText(engine.composedWord, 1)
                    updateSuggestions(engine.composedWord)
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

    private fun handleFastDelete() {
        val ic = currentInputConnection ?: return
        if (engine.isWordActive()) {
            engine.reset()
            composingWordState = ""
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(5, 0)
        }
        updateSuggestions("")
    }

    private fun handleSpace() {
        val ic = currentInputConnection ?: return
        if (engine.isWordActive()) {
            val finalWord = engine.composedWord
            ic.commitText("$finalWord ", 1)
            lastCommittedWord = finalWord
            resetComposing()
        } else {
            ic.commitText(" ", 1)
            updateSuggestions("")
        }
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        if (engine.isWordActive()) {
            commitComposingWord()
        }
        val info = currentInputEditorInfo
        val imeAction = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (imeAction != EditorInfo.IME_ACTION_NONE && imeAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(imeAction)
        } else {
            sendKeyChar('\n')
        }
    }

    private fun handleSelectSuggestion(suggestion: String) {
        val ic = currentInputConnection ?: return
        ic.commitText("$suggestion ", 1)
        lastCommittedWord = suggestion
        resetComposing()
    }

    private fun commitComposingWord() {
        val ic = currentInputConnection ?: return
        if (engine.isWordActive()) {
            val word = engine.composedWord
            ic.commitText(word, 1)
            lastCommittedWord = word
            resetComposing()
        }
    }

    private fun resetComposing() {
        engine.reset()
        composingWordState = ""
        updateSuggestions("")
    }

    private fun updateSuggestions(currentWord: String) {
        suggestionsState = VietnameseDictionary.getSuggestions(
            currentWord = currentWord,
            previousWord = lastCommittedWord
        )
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

    private fun switchIme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val switched = switchToNextInputMethod(false)
            if (!switched) {
                showImePicker()
            }
        } else {
            showImePicker()
        }
    }

    private fun showImePicker() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }
}
