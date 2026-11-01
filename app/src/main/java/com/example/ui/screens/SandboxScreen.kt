package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.engine.VietnameseDictionary
import com.example.ime.engine.VietnameseEngine
import com.example.ime.model.KeyboardAlignment
import com.example.ime.model.KeyboardPreferences
import com.example.ime.model.KeyboardSettings
import com.example.ime.model.TonePlacement
import com.example.ime.model.WorldLanguagesRepository
import com.example.ime.ui.ComposeKeyboardView
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SandboxScreen(
    preferences: KeyboardPreferences,
    onNavigateToSettings: () -> Unit,
    isImeDefault: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val settings by preferences.settings.collectAsState()
    val testFieldHeight = (configuration.screenWidthDp * 0.32f).coerceIn(100f, 180f).dp

    // Test pad state
    val initialText = "Xin chào! Đây là bàn phím tiếng Việt thông minh với Telex & VNI."
    var textState by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }

    // Engine instance for interactive in-app keyboard preview
    val testEngine = remember {
        VietnameseEngine(
            typingMethod = settings.typingMethod,
            tonePlacement = if (settings.tonePlacementEnabled) {
                settings.tonePlacement
            } else {
                TonePlacement.MODERN
            }
        )
    }
    DisposableEffect(testEngine) {
        onDispose { testEngine.close() }
    }

    var composingWord by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var lastCommitted by remember { mutableStateOf<String?>(null) }
    var deleteSelectionAnchor by remember { mutableIntStateOf(0) }
    var deleteSelectionEnd by remember { mutableIntStateOf(0) }

    LaunchedEffect(settings.typingMethod, settings.tonePlacement, settings.tonePlacementEnabled) {
        testEngine.typingMethod = settings.typingMethod
        testEngine.tonePlacement = if (settings.tonePlacementEnabled) {
            settings.tonePlacement
        } else {
            TonePlacement.MODERN
        }
    }

    fun updateSuggestions(current: String) {
        suggestions = VietnameseDictionary.getSuggestions(current, lastCommitted)
    }

    fun insertTextAtCursor(inserted: String) {
        val currentText = textState.text
        val selStart = textState.selection.min
        val selEnd = textState.selection.max
        val newText = currentText.substring(0, selStart) + inserted + currentText.substring(selEnd)
        val newPos = selStart + inserted.length
        textState = TextFieldValue(newText, TextRange(newPos))
    }

    fun handleKeyTyped(c: Char) {
        when (val res = testEngine.processChar(c)) {
            is VietnameseEngine.ProcessResult.Composing -> {
                composingWord = res.text
                updateSuggestions(res.text)
            }
            is VietnameseEngine.ProcessResult.WordBoundary -> {
                val toInsert = (if (res.committedWord.isNotEmpty()) res.committedWord else "") + res.boundaryChar
                insertTextAtCursor(toInsert)
                if (res.committedWord.isNotEmpty()) lastCommitted = res.committedWord
                testEngine.reset()
                composingWord = ""
                updateSuggestions("")
            }
            is VietnameseEngine.ProcessResult.Unhandled -> {
                if (testEngine.isWordActive()) {
                    insertTextAtCursor(testEngine.composedWord)
                    lastCommitted = testEngine.composedWord
                    testEngine.reset()
                    composingWord = ""
                }
                insertTextAtCursor(c.toString())
                updateSuggestions("")
            }
        }
    }

    fun handleBackspace() {
        if (testEngine.isWordActive()) {
            val handled = testEngine.processBackspace()
            if (handled) {
                composingWord = testEngine.composedWord
                updateSuggestions(composingWord)
            } else {
                testEngine.reset()
                composingWord = ""
            }
        } else {
            val currentText = textState.text
            val selStart = textState.selection.min
            val selEnd = textState.selection.max
            if (selStart != selEnd) {
                val newText = currentText.substring(0, selStart) + currentText.substring(selEnd)
                textState = TextFieldValue(newText, TextRange(selStart))
            } else if (selStart > 0) {
                val newText = currentText.substring(0, selStart - 1) + currentText.substring(selStart)
                textState = TextFieldValue(newText, TextRange(selStart - 1))
            }
            updateSuggestions("")
        }
    }

    fun beginDeleteSelection() {
        if (testEngine.isWordActive()) {
            insertTextAtCursor(testEngine.composedWord)
            testEngine.reset()
            composingWord = ""
        }
        deleteSelectionAnchor = textState.selection.min
        deleteSelectionEnd = textState.selection.max
    }

    fun moveDeleteSelection(step: Int) {
        deleteSelectionEnd = (deleteSelectionEnd + step)
            .coerceIn(deleteSelectionAnchor, textState.text.length)
        textState = textState.copy(
            selection = TextRange(deleteSelectionAnchor, deleteSelectionEnd)
        )
    }

    fun finishDeleteSelection() {
        if (deleteSelectionAnchor != deleteSelectionEnd) {
            val text = textState.text
            textState = TextFieldValue(
                text.substring(0, deleteSelectionAnchor) + text.substring(deleteSelectionEnd),
                TextRange(deleteSelectionAnchor)
            )
        }
        updateSuggestions("")
    }

    fun moveCursorByTouchpad(horizontalSteps: Int, verticalSteps: Int) {
        if (testEngine.isWordActive()) {
            insertTextAtCursor(testEngine.composedWord)
            testEngine.reset()
            composingWord = ""
        }

        var position = textState.selection.end.coerceIn(0, textState.text.length)
        repeat(abs(horizontalSteps)) {
            position = (position + if (horizontalSteps > 0) 1 else -1)
                .coerceIn(0, textState.text.length)
        }
        repeat(abs(verticalSteps)) {
            val text = textState.text
            val previousLineBreak = text.lastIndexOf('\n', (position - 1).coerceAtLeast(0))
            val lineStart = previousLineBreak + 1
            val column = position - lineStart
            if (verticalSteps < 0) {
                if (previousLineBreak >= 0) {
                    val priorLineBreak = text.lastIndexOf('\n', (previousLineBreak - 1).coerceAtLeast(0))
                    val priorLineStart = priorLineBreak + 1
                    position = (priorLineStart + column).coerceAtMost(previousLineBreak)
                } else {
                    position = (position - 20).coerceAtLeast(0)
                }
            } else {
                val nextLineBreak = text.indexOf('\n', position)
                if (nextLineBreak >= 0) {
                    val nextLineEnd = text.indexOf('\n', nextLineBreak + 1)
                        .let { if (it < 0) text.length else it }
                    position = (nextLineBreak + 1 + column).coerceAtMost(nextLineEnd)
                } else {
                    position = (position + 20).coerceAtMost(text.length)
                }
            }
        }
        textState = textState.copy(selection = TextRange(position))
    }

    fun cycleNextLanguage() {
        val enabledList = settings.enabledLanguages.toList()
        if (enabledList.isEmpty()) return
        val currentIndex = enabledList.indexOf(settings.activeLanguageId)
        val nextIndex = (currentIndex + 1) % enabledList.size
        val nextLangId = enabledList[nextIndex]
        val typingMethod = WorldLanguagesRepository.typingMethodFor(nextLangId)
        preferences.updateSettings {
            it.copy(activeLanguageId = nextLangId, typingMethod = typingMethod)
        }
    }

    fun handleSpace() {
        if (testEngine.isWordActive()) {
            val word = testEngine.composedWord
            insertTextAtCursor("$word ")
            lastCommitted = word
            testEngine.reset()
            composingWord = ""
            updateSuggestions("")
        } else {
            insertTextAtCursor(" ")
            updateSuggestions("")
        }
    }

    fun handleSuggestionSelect(suggestion: String) {
        insertTextAtCursor("$suggestion ")
        lastCommitted = suggestion
        testEngine.reset()
        composingWord = ""
        updateSuggestions("")
    }

    fun moveCursorLeft() {
        if (testEngine.isWordActive()) {
            insertTextAtCursor(testEngine.composedWord)
            testEngine.reset()
            composingWord = ""
        }
        val pos = (textState.selection.start - 1).coerceAtLeast(0)
        textState = textState.copy(selection = TextRange(pos))
    }

    fun moveCursorRight() {
        if (testEngine.isWordActive()) {
            insertTextAtCursor(testEngine.composedWord)
            testEngine.reset()
            composingWord = ""
        }
        val pos = (textState.selection.start + 1).coerceAtMost(textState.text.length)
        textState = textState.copy(selection = TextRange(pos))
    }

    fun moveCursorStart() {
        if (testEngine.isWordActive()) {
            insertTextAtCursor(testEngine.composedWord)
            testEngine.reset()
            composingWord = ""
        }
        textState = textState.copy(selection = TextRange(0))
    }

    fun moveCursorEnd() {
        if (testEngine.isWordActive()) {
            insertTextAtCursor(testEngine.composedWord)
            testEngine.reset()
            composingWord = ""
        }
        textState = textState.copy(selection = TextRange(textState.text.length))
    }

    fun selectAll() {
        textState = textState.copy(selection = TextRange(0, textState.text.length))
    }

    fun paste() {
        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = cb?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val pastedText = clip.getItemAt(0).text?.toString() ?: ""
            insertTextAtCursor(pastedText)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isImeDefault) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("default_ime_banner"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chọn Anykey26 làm bàn phím mặc định để sử dụng.",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    FilledTonalButton(
                        onClick = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        },
                        modifier = Modifier.testTag("btn_choose_default_ime")
                    ) {
                        Text("Chọn")
                    }
                }
            }
        }

        // 2. Interactive Testing Pad
        OutlinedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Khung thử gõ tiếng Việt",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cb?.setPrimaryClip(ClipData.newPlainText("Copied", textState.text))
                                Toast.makeText(context, "Đã sao chép văn bản", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp).testTag("btn_copy_text")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Sao chép",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                textState = TextFieldValue("", TextRange(0))
                                testEngine.reset()
                                composingWord = ""
                            },
                            modifier = Modifier.size(32.dp).testTag("btn_clear_text")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Xóa hết",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Text Display / Edit field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(testFieldHeight)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxSize().testTag("sandbox_text_field")
                    )

                    if (textState.text.isEmpty()) {
                        Text(
                            text = "Chạm vào đây để gõ bằng bàn phím Anykey26...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        )
                    }
                }

                // Sample phrase quick-inserts
                Text(
                    text = "Câu mẫu gõ thử:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Việt Nam",
                        "Xin chào các bạn",
                        "Hôm nay thời tiết đẹp",
                        "Trường học thân thiện"
                    ).forEach { sample ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .clickable {
                                    insertTextAtCursor("$sample ")
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "+ $sample",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Typing Status Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Kiểu gõ: ${settings.typingMethod.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        if (composingWord.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Đang ghép: “$composingWord”",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Text(
                        text = "${textState.text.length} ký tự",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
