package com.example.ime.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.zIndex
import android.view.inputmethod.EditorInfo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.ime.model.KeyboardAlignment
import com.example.ime.model.KeyboardColorScheme
import com.example.ime.model.KeyboardDisplayStyle
import com.example.ime.model.KeyboardSettings
import com.example.ime.model.KeyboardThemes
import com.example.ime.model.ThemeMode
import com.example.ime.model.TypingMethod
import com.example.ime.model.WorldLanguagesRepository
import kotlin.math.abs
import kotlin.math.roundToInt

enum class KeyboardLayoutMode {
    ALPHA,
    NUMBERS_SYMBOLS,
    MORE_SYMBOLS,
    EMOJI
}

enum class ShiftState {
    LOWER,
    SHIFT_ONCE,
    CAPS_LOCK
}

private val SPLIT_TOP_LEFT_KEYS = listOf("q", "w", "e", "r", "t")
private val SPLIT_TOP_RIGHT_KEYS = listOf("y", "u", "i", "o", "p")
private val SPLIT_MIDDLE_LEFT_KEYS = listOf("a", "s", "d", "f", "g")
private val SPLIT_MIDDLE_RIGHT_KEYS = listOf("h", "j", "k", "l")
private val SPLIT_BOTTOM_LEFT_KEYS = listOf("z", "x", "c", "v")
private val SPLIT_BOTTOM_RIGHT_KEYS = listOf("b", "n", "m")
private val ALPHA_TOP_KEYS = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
private val ALPHA_MIDDLE_KEYS = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
private val ALPHA_BOTTOM_KEYS = listOf("z", "x", "c", "v", "b", "n", "m")
private val SYMBOL_TOP_KEYS = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
private val SYMBOL_MIDDLE_KEYS = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
private val SYMBOL_BOTTOM_KEYS = listOf("*", "\"", "'", ":", ";", "!", "?")
private val MORE_SYMBOL_TOP_KEYS = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")
private val MORE_SYMBOL_MIDDLE_KEYS = listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\")
private val MORE_SYMBOL_BOTTOM_KEYS = listOf("%", "_", "<", ">", "[", "]", "§")

@Composable
fun ComposeKeyboardView(
    settings: KeyboardSettings,
    composingWord: String,
    suggestions: List<String>,
    hasActiveSelection: () -> Boolean = { false },
    onKeyTyped: (Char) -> Unit,
    onBackspace: () -> Unit,
    onBackspaceDown: () -> Boolean,
    onStartDeleteSelection: () -> Unit,
    onMoveDeleteSelection: (Int) -> Unit,
    onFinishDeleteSelection: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSelectSuggestion: (String) -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onMoveCursorByTouchpad: (Int, Int) -> Unit,
    onMoveCursorStart: () -> Unit,
    onMoveCursorEnd: () -> Unit,
    onSelectAll: () -> Unit,
    onPaste: () -> Unit,
    onSwitchIme: () -> Unit,
    onHideKeyboard: () -> Unit,
    onCycleLanguage: () -> Unit,
    onToggleTypingMethod: () -> Unit,
    onUpdateAlignment: (KeyboardAlignment) -> Unit,
    onOpenSettings: () -> Unit,
    hideVirtualNavigationBar: Boolean = false,
    imeAction: Int = EditorInfo.IME_ACTION_NONE,
    isMultiline: Boolean = false,
    onUpdateDisplayStyle: ((KeyboardDisplayStyle) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember {
        try {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } catch (_: Exception) {
            null
        }
    }

    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = remember(settings.themeMode, settings.themeId, isSystemDark) {
        KeyboardThemes.resolveTheme(settings.themeMode, settings.themeId, isSystemDark)
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isSplit = settings.displayStyle == KeyboardDisplayStyle.SPLIT || (isLandscape && settings.autoSplitInLandscape)
    val isFloating = settings.displayStyle == KeyboardDisplayStyle.FLOATING
    val keyboardSettings = remember(settings, isLandscape) {
        settings.copy(
            showVirtualNavBar = settings.showVirtualNavBar && !isLandscape,
            bottomChinHeightDp = if (isLandscape) 0 else settings.bottomChinHeightDp,
            showNumberRow = settings.showNumberRow &&
                (!isLandscape || settings.showNumberRowInLandscape),
            showSuggestions = settings.showSuggestions && settings.smartFeaturesEnabled,
            hapticFeedback = settings.hapticFeedback && settings.smartFeaturesEnabled,
            swipeSpaceToMoveCursor = settings.swipeSpaceToMoveCursor && settings.smartFeaturesEnabled,
            holdSpaceToSwitchLanguage = settings.holdSpaceToSwitchLanguage && settings.smartFeaturesEnabled,
            swipeBackspaceToDeleteFast = settings.swipeBackspaceToDeleteFast && settings.smartFeaturesEnabled
        )
    }

    var floatingOffsetX by remember { mutableFloatStateOf(0f) }
    var floatingOffsetY by remember { mutableFloatStateOf(0f) }

    var layoutMode by remember { mutableStateOf(KeyboardLayoutMode.ALPHA) }
    var shiftState by remember { mutableStateOf(ShiftState.LOWER) }

    fun triggerHaptic() {
        if (keyboardSettings.hapticFeedback && vibrator != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(12, 120))
                }
            } catch (_: Exception) {}
        }
    }

    val preferredKeyHeight =
        (46 * (settings.keyboardHeightPercent / 100f) * (settings.keyScalePercent / 100f) *
            if (isLandscape) 0.78f else 1f).dp
    val fontScale = (settings.fontScalePercent / 100f) * (settings.keyScalePercent / 100f)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isEmojiLayout = layoutMode == KeyboardLayoutMode.EMOJI
        val keyboardRows = when {
            isEmojiLayout -> 4.4f
            layoutMode == KeyboardLayoutMode.ALPHA && keyboardSettings.showNumberRow -> 4.8f
            else -> 4f
        }
        val reservedHeight =
            (if (keyboardSettings.showSuggestions && keyboardSettings.smartFeaturesEnabled) 42.dp else 0.dp) +
                (if (keyboardSettings.showVirtualNavBar && !hideVirtualNavigationBar) {
                    keyboardSettings.bottomChinHeightDp.dp
                } else {
                    0.dp
                }) +
                (if (isEmojiLayout) 30.dp else 0.dp) +
                (if (isFloating) 40.dp else 0.dp) +
                18.dp
        val availableKeyHeight = if (constraints.hasBoundedHeight) {
            ((maxHeight - reservedHeight) / keyboardRows).coerceAtLeast(24.dp)
        } else {
            preferredKeyHeight
        }
        val baseKeyHeight = min(preferredKeyHeight.value, availableKeyHeight.value).dp
        val responsiveFontScale = fontScale * (baseKeyHeight / preferredKeyHeight)

        // Container for Floating vs Standard/Docked vs Split
        if (isFloating) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLandscape) 0.dp else 12.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(settings.floatingScalePercent / 100f)
                    .offset { IntOffset(floatingOffsetX.roundToInt(), floatingOffsetY.roundToInt()) }
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, colorScheme.accentActionColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = colorScheme.backgroundColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    // Floating Drag Handle Bar on top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(colorScheme.specialKeyColor)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    floatingOffsetX += dragAmount.x
                                    floatingOffsetY += dragAmount.y
                                }
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Bàn phím nổi",
                            fontSize = 11.sp,
                            color = colorScheme.secondaryTextColor,
                            fontWeight = FontWeight.Medium
                        )

                        // Drag pill indicator
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colorScheme.secondaryTextColor.copy(alpha = 0.6f))
                        )

                        // Dock back button
                        Icon(
                            imageVector = Icons.Default.VerticalAlignBottom,
                            contentDescription = "Gắn đáy",
                            tint = colorScheme.secondaryTextColor,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    onUpdateDisplayStyle?.invoke(KeyboardDisplayStyle.DOCKED)
                                }
                        )
                    }

                    // Keyboard body in floating mode
                    KeyboardContentWrapper(
                        isSplit = false,
                        settings = keyboardSettings,
                        composingWord = composingWord,
                        suggestions = suggestions,
                        colorScheme = colorScheme,
                        baseKeyHeight = baseKeyHeight * 0.9f,
                        fontScale = responsiveFontScale * 0.92f,
                        layoutMode = layoutMode,
                        shiftState = shiftState,
                        triggerHaptic = { triggerHaptic() },
                        onKeyTyped = { c ->
                            onKeyTyped(c)
                            if (shiftState == ShiftState.SHIFT_ONCE) {
                                shiftState = ShiftState.LOWER
                            }
                        },
                        onBackspace = onBackspace,
                        onBackspaceDown = onBackspaceDown,
                        onStartDeleteSelection = onStartDeleteSelection,
                        onMoveDeleteSelection = onMoveDeleteSelection,
                        onFinishDeleteSelection = onFinishDeleteSelection,
                        onSpace = onSpace,
                        onEnter = onEnter,
                        onSelectSuggestion = onSelectSuggestion,
                        onMoveCursorLeft = onMoveCursorLeft,
                        onMoveCursorRight = onMoveCursorRight,
                        onMoveCursorByTouchpad = onMoveCursorByTouchpad,
                        onMoveCursorStart = onMoveCursorStart,
                        onMoveCursorEnd = onMoveCursorEnd,
                        onSelectAll = onSelectAll,
                        onPaste = onPaste,
                        onSwitchIme = onSwitchIme,
                        onHideKeyboard = onHideKeyboard,
                        onCycleLanguage = onCycleLanguage,
                        onToggleShift = {
                            triggerHaptic()
                            shiftState = when (shiftState) {
                                ShiftState.LOWER -> ShiftState.SHIFT_ONCE
                                ShiftState.SHIFT_ONCE -> ShiftState.CAPS_LOCK
                                ShiftState.CAPS_LOCK -> ShiftState.LOWER
                            }
                        },
                        onSwitchLayout = { mode ->
                            triggerHaptic()
                            layoutMode = mode
                        },
                        onToggleTypingMethod = onToggleTypingMethod,
                        onUpdateAlignment = onUpdateAlignment,
                        onOpenSettings = onOpenSettings,
                        hideVirtualNavigationBar = hideVirtualNavigationBar,
                        imeAction = imeAction,
                        isMultiline = isMultiline
                    )
                }
            }
        }
    } else {
        // Docked or Split Keyboard
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.backgroundColor)
                .testTag("compose_keyboard_root")
        ) {
            KeyboardContentWrapper(
                isSplit = isSplit,
                settings = keyboardSettings,
                composingWord = composingWord,
                suggestions = suggestions,
                colorScheme = colorScheme,
                baseKeyHeight = baseKeyHeight,
                fontScale = responsiveFontScale,
                layoutMode = layoutMode,
                shiftState = shiftState,
                triggerHaptic = { triggerHaptic() },
                onKeyTyped = { c ->
                    onKeyTyped(c)
                    if (shiftState == ShiftState.SHIFT_ONCE) {
                        shiftState = ShiftState.LOWER
                    }
                },
                onBackspace = onBackspace,
                onBackspaceDown = onBackspaceDown,
                onStartDeleteSelection = onStartDeleteSelection,
                onMoveDeleteSelection = onMoveDeleteSelection,
                onFinishDeleteSelection = onFinishDeleteSelection,
                onSpace = onSpace,
                onEnter = onEnter,
                onSelectSuggestion = onSelectSuggestion,
                onMoveCursorLeft = onMoveCursorLeft,
                onMoveCursorRight = onMoveCursorRight,
                onMoveCursorByTouchpad = onMoveCursorByTouchpad,
                onMoveCursorStart = onMoveCursorStart,
                onMoveCursorEnd = onMoveCursorEnd,
                onSelectAll = onSelectAll,
                onPaste = onPaste,
                onSwitchIme = onSwitchIme,
                onHideKeyboard = onHideKeyboard,
                onCycleLanguage = onCycleLanguage,
                onToggleShift = {
                    triggerHaptic()
                    shiftState = when (shiftState) {
                        ShiftState.LOWER -> ShiftState.SHIFT_ONCE
                        ShiftState.SHIFT_ONCE -> ShiftState.CAPS_LOCK
                        ShiftState.CAPS_LOCK -> ShiftState.LOWER
                    }
                },
                onSwitchLayout = { mode ->
                    triggerHaptic()
                    layoutMode = mode
                },
                onToggleTypingMethod = onToggleTypingMethod,
                onUpdateAlignment = onUpdateAlignment,
                onOpenSettings = onOpenSettings,
                hideVirtualNavigationBar = hideVirtualNavigationBar,
                imeAction = imeAction,
                isMultiline = isMultiline
            )
            }
        }
    }
}

@Composable
private fun KeyboardContentWrapper(
    isSplit: Boolean,
    settings: KeyboardSettings,
    composingWord: String,
    suggestions: List<String>,
    colorScheme: KeyboardColorScheme,
    baseKeyHeight: Dp,
    fontScale: Float,
    layoutMode: KeyboardLayoutMode,
    shiftState: ShiftState,
    triggerHaptic: () -> Unit,
    onKeyTyped: (Char) -> Unit,
    onBackspace: () -> Unit,
    onBackspaceDown: () -> Boolean,
    onStartDeleteSelection: () -> Unit,
    onMoveDeleteSelection: (Int) -> Unit,
    onFinishDeleteSelection: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSelectSuggestion: (String) -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onMoveCursorByTouchpad: (Int, Int) -> Unit,
    onMoveCursorStart: () -> Unit,
    onMoveCursorEnd: () -> Unit,
    onSelectAll: () -> Unit,
    onPaste: () -> Unit,
    onSwitchIme: () -> Unit,
    onHideKeyboard: () -> Unit,
    onCycleLanguage: () -> Unit,
    onToggleShift: () -> Unit,
    onSwitchLayout: (KeyboardLayoutMode) -> Unit,
    onToggleTypingMethod: () -> Unit,
    onUpdateAlignment: (KeyboardAlignment) -> Unit,
    onOpenSettings: () -> Unit,
    hideVirtualNavigationBar: Boolean,
    imeAction: Int = EditorInfo.IME_ACTION_NONE,
    isMultiline: Boolean = false
) {
    // 1. Suggestion Bar
    if (settings.showSuggestions) {
        SuggestionBarView(
            composingWord = composingWord,
            suggestions = suggestions,
            colorScheme = colorScheme,
            settings = settings,
            onSelectSuggestion = {
                triggerHaptic()
                onSelectSuggestion(it)
            }
        )
    }

    // 2. Main Keyboard Area
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isSplit && settings.alignment == KeyboardAlignment.LEFT) {
            Box(modifier = Modifier.weight(0.85f)) {
                KeyboardMainContent(
                    isSplit = false,
                    layoutMode = layoutMode,
                    shiftState = shiftState,
                    settings = settings,
                    colorScheme = colorScheme,
                    baseKeyHeight = baseKeyHeight,
                    fontScale = fontScale,
                    onKeyTyped = { c ->
                        triggerHaptic()
                        onKeyTyped(c)
                    },
                    onBackspace = {
                        triggerHaptic()
                        onBackspace()
                    },
                    onBackspaceDown = {
                        val handled = onBackspaceDown()
                        if (handled) triggerHaptic()
                        handled
                    },
                    onStartDeleteSelection = {
                        triggerHaptic()
                        onStartDeleteSelection()
                    },
                    onMoveDeleteSelection = onMoveDeleteSelection,
                    onFinishDeleteSelection = onFinishDeleteSelection,
                    onSpace = {
                        triggerHaptic()
                        onSpace()
                    },
                    onEnter = {
                        triggerHaptic()
                        onEnter()
                    },
                    onMoveCursorLeft = onMoveCursorLeft,
                    onMoveCursorRight = onMoveCursorRight,
                    onMoveCursorByTouchpad = onMoveCursorByTouchpad,
                    onCycleLanguage = {
                        triggerHaptic()
                        onCycleLanguage()
                    },
                    onToggleShift = onToggleShift,
                    onSwitchLayout = onSwitchLayout,
                    onToggleTypingMethod = onToggleTypingMethod,
                    imeAction = imeAction,
                    isMultiline = isMultiline
                )
            }

            OneHandedSidebar(
                colorScheme = colorScheme,
                keyHeight = baseKeyHeight,
                onExpand = { onUpdateAlignment(KeyboardAlignment.CENTER) },
                onFlipSide = { onUpdateAlignment(KeyboardAlignment.RIGHT) },
                onOpenSettings = onOpenSettings,
                isRightSidebar = true
            )
        } else if (!isSplit && settings.alignment == KeyboardAlignment.RIGHT) {
            OneHandedSidebar(
                colorScheme = colorScheme,
                keyHeight = baseKeyHeight,
                onExpand = { onUpdateAlignment(KeyboardAlignment.CENTER) },
                onFlipSide = { onUpdateAlignment(KeyboardAlignment.LEFT) },
                onOpenSettings = onOpenSettings,
                isRightSidebar = false
            )

            Box(modifier = Modifier.weight(0.85f)) {
                KeyboardMainContent(
                    isSplit = false,
                    layoutMode = layoutMode,
                    shiftState = shiftState,
                    settings = settings,
                    colorScheme = colorScheme,
                    baseKeyHeight = baseKeyHeight,
                    fontScale = fontScale,
                    onKeyTyped = { c ->
                        triggerHaptic()
                        onKeyTyped(c)
                    },
                    onBackspace = {
                        triggerHaptic()
                        onBackspace()
                    },
                    onBackspaceDown = {
                        val handled = onBackspaceDown()
                        if (handled) triggerHaptic()
                        handled
                    },
                    onStartDeleteSelection = {
                        triggerHaptic()
                        onStartDeleteSelection()
                    },
                    onMoveDeleteSelection = onMoveDeleteSelection,
                    onFinishDeleteSelection = onFinishDeleteSelection,
                    onSpace = {
                        triggerHaptic()
                        onSpace()
                    },
                    onEnter = {
                        triggerHaptic()
                        onEnter()
                    },
                    onMoveCursorLeft = onMoveCursorLeft,
                    onMoveCursorRight = onMoveCursorRight,
                    onMoveCursorByTouchpad = onMoveCursorByTouchpad,
                    onCycleLanguage = {
                        triggerHaptic()
                        onCycleLanguage()
                    },
                    onToggleShift = onToggleShift,
                    onSwitchLayout = onSwitchLayout,
                    onToggleTypingMethod = onToggleTypingMethod,
                    imeAction = imeAction,
                    isMultiline = isMultiline
                )
            }
        } else {
            // Standard Full or Split
            Box(modifier = Modifier.fillMaxWidth()) {
                KeyboardMainContent(
                    isSplit = isSplit,
                    layoutMode = layoutMode,
                    shiftState = shiftState,
                    settings = settings,
                    colorScheme = colorScheme,
                    baseKeyHeight = baseKeyHeight,
                    fontScale = fontScale,
                    onKeyTyped = { c ->
                        triggerHaptic()
                        onKeyTyped(c)
                    },
                    onBackspace = {
                        triggerHaptic()
                        onBackspace()
                    },
                    onBackspaceDown = {
                        val handled = onBackspaceDown()
                        if (handled) triggerHaptic()
                        handled
                    },
                    onStartDeleteSelection = {
                        triggerHaptic()
                        onStartDeleteSelection()
                    },
                    onMoveDeleteSelection = onMoveDeleteSelection,
                    onFinishDeleteSelection = onFinishDeleteSelection,
                    onSpace = {
                        triggerHaptic()
                        onSpace()
                    },
                    onEnter = {
                        triggerHaptic()
                        onEnter()
                    },
                    onMoveCursorLeft = onMoveCursorLeft,
                    onMoveCursorRight = onMoveCursorRight,
                    onMoveCursorByTouchpad = onMoveCursorByTouchpad,
                    onCycleLanguage = {
                        triggerHaptic()
                        onCycleLanguage()
                    },
                    onToggleShift = onToggleShift,
                    onSwitchLayout = onSwitchLayout,
                    onToggleTypingMethod = onToggleTypingMethod,
                    imeAction = imeAction,
                    isMultiline = isMultiline
                )
            }
        }
    }

    // 3. Virtual Navigation Bar
    if (settings.showVirtualNavBar && !hideVirtualNavigationBar) {
        VirtualNavigationBar(
            settings = settings,
            colorScheme = colorScheme,
            onMoveCursorLeft = {
                triggerHaptic()
                onMoveCursorLeft()
            },
            onMoveCursorRight = {
                triggerHaptic()
                onMoveCursorRight()
            },
            onMoveCursorStart = {
                triggerHaptic()
                onMoveCursorStart()
            },
            onMoveCursorEnd = {
                triggerHaptic()
                onMoveCursorEnd()
            },
            onSelectAll = {
                triggerHaptic()
                onSelectAll()
            },
            onPaste = {
                triggerHaptic()
                onPaste()
            },
            onSwitchIme = {
                triggerHaptic()
                onSwitchIme()
            },
            onHideKeyboard = {
                triggerHaptic()
                onHideKeyboard()
            }
        )
    }
}

@Composable
private fun KeyboardMainContent(
    isSplit: Boolean,
    layoutMode: KeyboardLayoutMode,
    shiftState: ShiftState,
    settings: KeyboardSettings,
    colorScheme: KeyboardColorScheme,
    baseKeyHeight: Dp,
    fontScale: Float,
    onKeyTyped: (Char) -> Unit,
    onBackspace: () -> Unit,
    onBackspaceDown: () -> Boolean,
    onStartDeleteSelection: () -> Unit,
    onMoveDeleteSelection: (Int) -> Unit,
    onFinishDeleteSelection: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onMoveCursorByTouchpad: (Int, Int) -> Unit,
    onCycleLanguage: () -> Unit,
    onToggleShift: () -> Unit,
    onSwitchLayout: (KeyboardLayoutMode) -> Unit,
    onToggleTypingMethod: () -> Unit,
    imeAction: Int = EditorInfo.IME_ACTION_NONE,
    isMultiline: Boolean = false
) {
    val (enterIcon, enterDesc) = when {
        isMultiline -> Icons.AutoMirrored.Filled.KeyboardReturn to "Xuống dòng"
        imeAction == EditorInfo.IME_ACTION_SEARCH -> Icons.Default.Search to "Tìm kiếm"
        imeAction == EditorInfo.IME_ACTION_SEND -> Icons.AutoMirrored.Filled.Send to "Gửi"
        imeAction == EditorInfo.IME_ACTION_DONE -> Icons.Default.Done to "Hoàn tất"
        imeAction == EditorInfo.IME_ACTION_GO -> Icons.AutoMirrored.Filled.ArrowForward to "Đi tới"
        else -> Icons.AutoMirrored.Filled.KeyboardReturn to "Enter"
    }

    val isUpper = (shiftState == ShiftState.SHIFT_ONCE || shiftState == ShiftState.CAPS_LOCK)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(settings.rowSpacingDp.dp)
    ) {
        // Optional dedicated number row
        if (settings.showNumberRow && layoutMode == KeyboardLayoutMode.ALPHA) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                "1234567890".forEach { char ->
                    KeyCap(
                        text = char.toString(),
                        colorScheme = colorScheme,
                        height = (baseKeyHeight * 0.8f),
                        fontSize = (15 * fontScale).sp,
                        weight = 1f,
                        onClick = { onKeyTyped(char) }
                    )
                }
            }
        }

        when (layoutMode) {
            KeyboardLayoutMode.ALPHA -> {
                val isUpper = shiftState != ShiftState.LOWER

                if (isSplit) {
                    // SPLIT KEYBOARD LAYOUT (Left and Right thumb clusters with center space)
                    // Row 1: q w e r t  --- GAP ---  y u i o p
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SPLIT_TOP_LEFT_KEYS.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(display.first()) })
                        }
                        Spacer(modifier = Modifier.weight(1.8f))
                        SPLIT_TOP_RIGHT_KEYS.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(display.first()) })
                        }
                    }

                    // Row 2: a s d f g  --- GAP ---  h j k l
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SPLIT_MIDDLE_LEFT_KEYS.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(display.first()) })
                        }
                        Spacer(modifier = Modifier.weight(2.2f))
                        SPLIT_MIDDLE_RIGHT_KEYS.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1.25f, onClick = { onKeyTyped(display.first()) })
                        }
                    }

                    // Row 3: Shift, z x c v  --- GAP ---  b n m, Backspace
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpecialKeyCap(
                            icon = if (shiftState == ShiftState.CAPS_LOCK) Icons.Default.KeyboardCapslock else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (shiftState == ShiftState.CAPS_LOCK) "Caps Lock" else if (shiftState == ShiftState.SHIFT_ONCE) "Shift (1 lần)" else "Shift",
                            colorScheme = colorScheme,
                            isActive = shiftState != ShiftState.LOWER,
                            isAccent = shiftState == ShiftState.CAPS_LOCK,
                            height = baseKeyHeight,
                            weight = 1.3f,
                            onClick = onToggleShift
                        )
                        SPLIT_BOTTOM_LEFT_KEYS.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(display.first()) })
                        }
                        Spacer(modifier = Modifier.weight(1.8f))
                        SPLIT_BOTTOM_RIGHT_KEYS.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(display.first()) })
                        }
                        BackspaceGestureKeyCap(
                            colorScheme = colorScheme,
                            height = baseKeyHeight,
                            weight = 1.3f,
                            enableSwipeDelete = settings.swipeBackspaceToDeleteFast,
                            onClick = onBackspace,
                            onBackspaceDown = onBackspaceDown,
                            onStartSelection = onStartDeleteSelection,
                            onMoveSelection = onMoveDeleteSelection,
                            onFinishSelection = onFinishDeleteSelection
                        )
                    }

                    // Row 4: ?123, Emoji, Left Space  --- GAP (Arrows) ---  Right Space, ., Enter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KeyCap(text = "?123", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (13 * fontScale).sp, isSpecial = true, weight = 1.2f, showPreview = false, onClick = { onSwitchLayout(KeyboardLayoutMode.NUMBERS_SYMBOLS) })
                        SpecialKeyCap(icon = Icons.Default.EmojiEmotions, contentDescription = "Emoji", colorScheme = colorScheme, height = baseKeyHeight, weight = 1f, onClick = { onSwitchLayout(KeyboardLayoutMode.EMOJI) })
                        SpaceGestureKeyCap(settings = settings, colorScheme = colorScheme, height = baseKeyHeight, weight = 2.5f, onClick = onSpace, onMoveCursorByTouchpad = onMoveCursorByTouchpad, onCycleLanguage = onCycleLanguage)

                        // Center Gap with Quick Cursor Arrows in Split mode
                        Row(modifier = Modifier.weight(1.6f), horizontalArrangement = Arrangement.Center) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trái", tint = colorScheme.secondaryTextColor, modifier = Modifier.size(20.dp).clickable { onMoveCursorLeft() })
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Phải", tint = colorScheme.secondaryTextColor, modifier = Modifier.size(20.dp).clickable { onMoveCursorRight() })
                        }

                        SpaceGestureKeyCap(settings = settings, colorScheme = colorScheme, height = baseKeyHeight, weight = 2.5f, onClick = onSpace, onMoveCursorByTouchpad = onMoveCursorByTouchpad, onCycleLanguage = onCycleLanguage)
                        KeyCap(text = ".", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 0.9f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped('.') })
                        SpecialKeyCap(icon = enterIcon, contentDescription = enterDesc, colorScheme = colorScheme, isAccent = true, height = baseKeyHeight, weight = 1.2f, onClick = onEnter)
                    }
                } else {
                    // STANDARD FULL DOCKED KEYBOARD
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        ALPHA_TOP_KEYS.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (19 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(display.first()) })
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        ALPHA_MIDDLE_KEYS.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (19 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(display.first()) })
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        SpecialKeyCap(
                            icon = if (shiftState == ShiftState.CAPS_LOCK) Icons.Default.KeyboardCapslock else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (shiftState == ShiftState.CAPS_LOCK) "Caps Lock" else if (shiftState == ShiftState.SHIFT_ONCE) "Shift (1 lần)" else "Shift",
                            colorScheme = colorScheme,
                            isActive = shiftState != ShiftState.LOWER,
                            isAccent = shiftState == ShiftState.CAPS_LOCK,
                            height = baseKeyHeight,
                            weight = 1.4f,
                            onClick = onToggleShift
                        )
                        ALPHA_BOTTOM_KEYS.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (19 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(display.first()) })
                        }
                        BackspaceGestureKeyCap(colorScheme = colorScheme, height = baseKeyHeight, weight = 1.4f, enableSwipeDelete = settings.swipeBackspaceToDeleteFast, onClick = onBackspace, onBackspaceDown = onBackspaceDown, onStartSelection = onStartDeleteSelection, onMoveSelection = onMoveDeleteSelection, onFinishSelection = onFinishDeleteSelection)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        KeyCap(text = "?123", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (14 * fontScale).sp, isSpecial = true, weight = 1.4f, showPreview = false, onClick = { onSwitchLayout(KeyboardLayoutMode.NUMBERS_SYMBOLS) })
                        SpecialKeyCap(icon = Icons.Default.EmojiEmotions, contentDescription = "Emoji", colorScheme = colorScheme, height = baseKeyHeight, weight = 1f, onClick = { onSwitchLayout(KeyboardLayoutMode.EMOJI) })
                        SpaceGestureKeyCap(settings = settings, colorScheme = colorScheme, height = baseKeyHeight, weight = 4.2f, onClick = onSpace, onMoveCursorByTouchpad = onMoveCursorByTouchpad, onCycleLanguage = onCycleLanguage)
                        KeyCap(text = ".", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped('.') })
                        SpecialKeyCap(icon = enterIcon, contentDescription = enterDesc, colorScheme = colorScheme, isAccent = true, height = baseKeyHeight, weight = 1.4f, onClick = onEnter)
                    }
                }
            }

            KeyboardLayoutMode.NUMBERS_SYMBOLS -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    SYMBOL_TOP_KEYS.forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(s.first()) })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    SYMBOL_MIDDLE_KEYS.forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (17 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(s.first()) })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    KeyCap(text = "=/<", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (13 * fontScale).sp, isSpecial = true, weight = 1.4f, showPreview = false, onClick = { onSwitchLayout(KeyboardLayoutMode.MORE_SYMBOLS) })
                    SYMBOL_BOTTOM_KEYS.forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (17 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(s.first()) })
                    }
                    BackspaceGestureKeyCap(colorScheme = colorScheme, height = baseKeyHeight, weight = 1.4f, enableSwipeDelete = settings.swipeBackspaceToDeleteFast, onClick = onBackspace, onBackspaceDown = onBackspaceDown, onStartSelection = onStartDeleteSelection, onMoveSelection = onMoveDeleteSelection, onFinishSelection = onFinishDeleteSelection)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    KeyCap(text = "ABC", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (14 * fontScale).sp, isSpecial = true, weight = 1.4f, showPreview = false, onClick = { onSwitchLayout(KeyboardLayoutMode.ALPHA) })
                    SpecialKeyCap(icon = Icons.Default.EmojiEmotions, contentDescription = "Emoji", colorScheme = colorScheme, height = baseKeyHeight, weight = 1f, onClick = { onSwitchLayout(KeyboardLayoutMode.EMOJI) })
                    SpaceGestureKeyCap(settings = settings, colorScheme = colorScheme, height = baseKeyHeight, weight = 4.2f, onClick = onSpace, onMoveCursorByTouchpad = onMoveCursorByTouchpad, onCycleLanguage = onCycleLanguage)
                    KeyCap(text = ",", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(',') })
                    SpecialKeyCap(icon = enterIcon, contentDescription = enterDesc, colorScheme = colorScheme, isAccent = true, height = baseKeyHeight, weight = 1.4f, onClick = onEnter)
                }
            }

            KeyboardLayoutMode.MORE_SYMBOLS -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    MORE_SYMBOL_TOP_KEYS.forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (17 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(s.first()) })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    MORE_SYMBOL_MIDDLE_KEYS.forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (17 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(s.first()) })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    KeyCap(text = "?123", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (13 * fontScale).sp, isSpecial = true, weight = 1.4f, showPreview = false, onClick = { onSwitchLayout(KeyboardLayoutMode.NUMBERS_SYMBOLS) })
                    MORE_SYMBOL_BOTTOM_KEYS.forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (17 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped(s.first()) })
                    }
                    BackspaceGestureKeyCap(colorScheme = colorScheme, height = baseKeyHeight, weight = 1.4f, enableSwipeDelete = settings.swipeBackspaceToDeleteFast, onClick = onBackspace, onBackspaceDown = onBackspaceDown, onStartSelection = onStartDeleteSelection, onMoveSelection = onMoveDeleteSelection, onFinishSelection = onFinishDeleteSelection)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    KeyCap(text = "ABC", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (14 * fontScale).sp, isSpecial = true, weight = 1.4f, showPreview = false, onClick = { onSwitchLayout(KeyboardLayoutMode.ALPHA) })
                    SpecialKeyCap(icon = Icons.Default.EmojiEmotions, contentDescription = "Emoji", colorScheme = colorScheme, height = baseKeyHeight, weight = 1f, onClick = { onSwitchLayout(KeyboardLayoutMode.EMOJI) })
                    SpaceGestureKeyCap(settings = settings, colorScheme = colorScheme, height = baseKeyHeight, weight = 4.2f, onClick = onSpace, onMoveCursorByTouchpad = onMoveCursorByTouchpad, onCycleLanguage = onCycleLanguage)
                    KeyCap(text = ".", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, showPreview = settings.showKeyPreview, onClick = { onKeyTyped('.') })
                    SpecialKeyCap(icon = enterIcon, contentDescription = enterDesc, colorScheme = colorScheme, isAccent = true, height = baseKeyHeight, weight = 1.4f, onClick = onEnter)
                }
            }

            KeyboardLayoutMode.EMOJI -> {
                // COMPREHENSIVE FULL UNICODE EMOJI REPOSITORY WITH CATEGORIES
                var selectedCatIndex by remember { mutableIntStateOf(0) }
                val currentCategory = EmojiRepository.categories[selectedCatIndex]
                val catScrollState = rememberScrollState()

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Category Selection Tab Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(catScrollState)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EmojiRepository.categories.forEachIndexed { index, cat ->
                            val isCatSelected = selectedCatIndex == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isCatSelected) colorScheme.accentActionColor.copy(alpha = 0.25f) else Color.Transparent
                                    )
                                    .clickable { selectedCatIndex = index }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = cat.icon, fontSize = 20.sp)
                            }
                        }
                    }

                    // Emojis Grid for selected category
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(baseKeyHeight * 3.4f)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(currentCategory.emojis) { emoji ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        emoji.forEach { c -> onKeyTyped(c) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = (22 * fontScale).sp)
                            }
                        }
                    }

                    // Bottom Bar: Return to ABC, Space, Backspace
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        KeyCap(
                            text = "ABC",
                            colorScheme = colorScheme,
                            height = baseKeyHeight,
                            fontSize = (15 * fontScale).sp,
                            isSpecial = true,
                            weight = 2f,
                            onClick = { onSwitchLayout(KeyboardLayoutMode.ALPHA) }
                        )

                        SpaceGestureKeyCap(
                            settings = settings,
                            colorScheme = colorScheme,
                            height = baseKeyHeight,
                            weight = 5f,
                            onClick = onSpace,
                            onMoveCursorByTouchpad = onMoveCursorByTouchpad,
                            onCycleLanguage = onCycleLanguage
                        )

                        BackspaceGestureKeyCap(
                            colorScheme = colorScheme,
                            height = baseKeyHeight,
                            weight = 2f,
                            enableSwipeDelete = settings.swipeBackspaceToDeleteFast,
                            onClick = onBackspace,
                            onBackspaceDown = onBackspaceDown,
                            onStartSelection = onStartDeleteSelection,
                            onMoveSelection = onMoveDeleteSelection,
                            onFinishSelection = onFinishDeleteSelection
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.KeyCap(
    text: String,
    colorScheme: KeyboardColorScheme,
    height: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    weight: Float,
    isSpecial: Boolean = false,
    showPreview: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Glassmorphism Popup Animation (YÊU CẦU 1): Scale từ 0.8 lên 1.0 kèm fade-in
    val popupScale by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 850f),
        label = "glassPopupScale"
    )
    val popupAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "glassPopupAlpha"
    )

    // Hiệu ứng phím Glassmorphism khi bấm (YÊU CẦU 2)
    val baseKeyColor = if (isSpecial) colorScheme.specialKeyColor else colorScheme.keyBackgroundColor
    val keyBorderColor = if (isPressed) {
        if (colorScheme.isDark) Color.White.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.80f)
    } else {
        colorScheme.borderColor.copy(alpha = 0.35f)
    }

    val popupBubbleShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .weight(weight)
            .height(height)
            .zIndex(if (isPressed || popupAlpha > 0.01f) 100f else 0f),
        contentAlignment = Alignment.Center
    ) {
        // Khối phím bấm
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isPressed) 3.dp else 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    spotColor = if (isPressed) colorScheme.accentActionColor.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(8.dp))
                .background(baseKeyColor)
                .border(
                    width = if (isPressed) 1.dp else 0.5.dp,
                    color = keyBorderColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = colorScheme.accentActionColor.copy(alpha = 0.45f))
                ) { onClick() }
                .testTag("key_$text"),
            contentAlignment = Alignment.Center
        ) {
            // Lớp phủ kính mờ (Translucent Glass Overlay) khi bấm phím
            if (isPressed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (colorScheme.isDark) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.22f),
                                        colorScheme.accentActionColor.copy(alpha = 0.38f)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.60f),
                                        colorScheme.accentActionColor.copy(alpha = 0.28f)
                                    )
                                )
                            }
                        )
                )

                // Vệt phản chiếu ánh sáng kính ở mép trên
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.2.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = if (colorScheme.isDark) 0.6f else 0.85f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Text(
                text = text,
                color = if (isSpecial) colorScheme.secondaryTextColor else colorScheme.primaryTextColor,
                fontSize = fontSize,
                fontWeight = if (isSpecial) FontWeight.SemiBold else FontWeight.Medium
            )
        }

        // The preview is visual-only and does not add a pointer input target.
        if (showPreview && (isPressed || popupAlpha > 0.05f) && !isSpecial) {
            Box(
                modifier = Modifier
                    .offset(y = (-64).dp)
                    .size(width = 56.dp, height = 56.dp)
                    .scale(popupScale)
                    .alpha(popupAlpha)
                    .zIndex(200f)
                    .shadow(
                        elevation = 14.dp,
                        shape = popupBubbleShape,
                        ambientColor = Color.Black.copy(alpha = 0.50f),
                        spotColor = Color.Black.copy(alpha = 0.65f)
                    )
                    .clip(popupBubbleShape)
                    .background(
                        brush = if (colorScheme.isDark) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f), // Mặt kính sáng trên cùng
                                    Color(0xFF262C3A).copy(alpha = 0.65f), // Kính mờ thân popup
                                    Color(0xFF141720).copy(alpha = 0.90f)  // Đáy kính đậm
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    Color(0xFFF0F3F8).copy(alpha = 0.70f),
                                    Color(0xFFD6DBE4).copy(alpha = 0.88f)
                                )
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (colorScheme.isDark) 0.50f else 0.75f),
                        shape = popupBubbleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Vệt phản quang mặt kính mép trên của popup
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(1.5.dp)
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = if (colorScheme.isDark) 0.70f else 0.90f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // KÝ TỰ PHÍM: Lớp trên cùng, độ tương phản cao tuyệt đối, không bị blur
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = if (colorScheme.isDark) Color(0xFFFFFFFF) else Color(0xFF12151B),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.SpecialKeyCap(
    icon: ImageVector,
    contentDescription: String,
    colorScheme: KeyboardColorScheme,
    height: Dp,
    weight: Float,
    isAccent: Boolean = false,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val baseBg = when {
        isAccent -> colorScheme.accentActionColor
        isActive -> colorScheme.accentActionColor.copy(alpha = 0.28f)
        else -> colorScheme.specialKeyColor
    }

    val iconTint = when {
        isAccent -> if (colorScheme.isDark) Color.Black else Color.White
        isActive -> colorScheme.accentActionColor
        else -> colorScheme.secondaryTextColor
    }

    val keyBorderColor = if (isPressed) {
        if (colorScheme.isDark) Color.White.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.80f)
    } else if (isActive) {
        colorScheme.accentActionColor.copy(alpha = 0.6f)
    } else {
        colorScheme.borderColor.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(height)
            .shadow(
                elevation = if (isPressed) 3.dp else 1.dp,
                shape = RoundedCornerShape(8.dp),
                spotColor = if (isPressed) colorScheme.accentActionColor.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(baseBg)
            .border(
                width = if (isPressed || isActive) 1.dp else 0.5.dp,
                color = keyBorderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = colorScheme.accentActionColor.copy(alpha = 0.45f))
            ) { onClick() }
            .testTag("special_key_$contentDescription"),
        contentAlignment = Alignment.Center
    ) {
        // Lớp phủ kính mờ khi ấn phím đặc biệt
        if (isPressed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (colorScheme.isDark) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    colorScheme.accentActionColor.copy(alpha = 0.38f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.55f),
                                    colorScheme.accentActionColor.copy(alpha = 0.28f)
                                )
                            )
                        }
                    )
            )

            // Vệt sáng kính phản chiếu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.2.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (colorScheme.isDark) 0.6f else 0.85f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(21.dp)
            )
            if (contentDescription == "Caps Lock") {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(iconTint)
                )
            }
        }
    }
}

@Composable
private fun RowScope.BackspaceGestureKeyCap(
    colorScheme: KeyboardColorScheme,
    height: Dp,
    weight: Float,
    enableSwipeDelete: Boolean,
    onClick: () -> Unit,
    onBackspaceDown: () -> Boolean,
    onStartSelection: () -> Unit,
    onMoveSelection: (Int) -> Unit,
    onFinishSelection: () -> Unit
) {
    var isSwiping by remember { mutableStateOf(false) }
    val selectionStepPx = with(LocalDensity.current) { 18.dp.toPx() }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(height)
            .shadow(1.dp, RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(if (isSwiping) colorScheme.accentActionColor.copy(alpha = 0.3f) else colorScheme.specialKeyColor)
            .border(0.5.dp, colorScheme.borderColor.copy(alpha = 0.4f), RoundedCornerShape(7.dp))
            .pointerInput(enableSwipeDelete) {
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = true)
                        val deletedSelectionOnDown = onBackspaceDown()
                        var lastX = down.position.x
                        var totalDragX = 0f
                        var selectionDragPx = 0f
                        var didSelect = false
                        var wasLongPress = false
                        isSwiping = false

                        val repeatJob = if (deletedSelectionOnDown) {
                            null
                        } else {
                            launch {
                                delay(viewConfiguration.longPressTimeoutMillis.toLong())
                                if (!didSelect) {
                                    wasLongPress = true
                                    while (isActive) {
                                        onClick()
                                        delay(55)
                                    }
                                }
                            }
                        }
                        if (deletedSelectionOnDown) down.consume()

                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (deletedSelectionOnDown) {
                                    change.consume()
                                    if (!change.pressed) break
                                    continue
                                }
                                if (!change.pressed) break

                                totalDragX += change.position.x - lastX
                                val dragX = change.position.x - lastX
                                lastX = change.position.x

                                if (abs(totalDragX) > 8f) repeatJob?.cancel()

                                if (enableSwipeDelete && !didSelect && abs(totalDragX) > 25f) {
                                    didSelect = true
                                    isSwiping = true
                                    repeatJob?.cancel()
                                    onStartSelection()
                                    selectionDragPx = totalDragX * 1.7f
                                } else if (didSelect) {
                                    selectionDragPx += dragX * 1.7f
                                }

                                if (didSelect) {
                                    while (selectionDragPx >= selectionStepPx) {
                                        onMoveSelection(1)
                                        selectionDragPx -= selectionStepPx
                                    }
                                    while (selectionDragPx <= -selectionStepPx) {
                                        onMoveSelection(-1)
                                        selectionDragPx += selectionStepPx
                                    }
                                }
                                if (didSelect || abs(totalDragX) > 8f) {
                                    change.consume()
                                }
                            }
                        } finally {
                            repeatJob?.cancel()
                            if (didSelect) onFinishSelection()
                            isSwiping = false
                        }

                        if (!deletedSelectionOnDown && !didSelect && !wasLongPress && abs(totalDragX) < 15f) onClick()
                    }
                }
            }
            .testTag("key_backspace_gesture"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Xóa",
            tint = if (isSwiping) colorScheme.accentActionColor else colorScheme.secondaryTextColor,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun RowScope.SpaceGestureKeyCap(
    settings: KeyboardSettings,
    colorScheme: KeyboardColorScheme,
    height: Dp,
    weight: Float,
    onClick: () -> Unit,
    onMoveCursorByTouchpad: (Int, Int) -> Unit,
    onCycleLanguage: () -> Unit
) {
    val langPack = remember(settings.activeLanguageId) {
        WorldLanguagesRepository.getLanguage(settings.activeLanguageId)
    }

    val displayLabel = remember(langPack, settings.typingMethod) {
        buildString {
            if (langPack != null) {
                append("${langPack.flag} ${langPack.name}")
            } else {
                when (settings.typingMethod) {
                    TypingMethod.TELEX -> append("🇻🇳 Tiếng Việt (Telex)")
                    TypingMethod.VNI -> append("🇻🇳 Tiếng Việt (VNI)")
                    TypingMethod.OFF -> append("English")
                }
            }
        }
    }

    var isLongPressLanguageSwitch by remember { mutableStateOf(false) }
    var hasMovedCursorEarly by remember { mutableStateOf(false) }
    var touchpadActive by remember { mutableStateOf(false) }
    var touchpadPosition by remember { mutableStateOf(Offset.Zero) }
    val cursorStepPx = with(LocalDensity.current) {
        (height * 0.4f).toPx().coerceAtLeast(12.dp.toPx())
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(height)
            .shadow(1.dp, RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(if (isLongPressLanguageSwitch && !hasMovedCursorEarly) colorScheme.accentActionColor.copy(alpha = 0.25f) else colorScheme.keyBackgroundColor)
            .border(0.5.dp, colorScheme.borderColor.copy(alpha = 0.4f), RoundedCornerShape(7.dp))
            .pointerInput(settings.swipeSpaceToMoveCursor, settings.holdSpaceToSwitchLanguage) {
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val pressStartTime = System.currentTimeMillis()
                        var lastX = down.position.x
                        var lastY = down.position.y
                        var totalDragX = 0f
                        var totalDragY = 0f
                        var emittedCursorStepsX = 0
                        var emittedCursorStepsY = 0
                        var didMoveCursor = false
                        var didSwitchLanguage = false
                        var didTouchpadGesture = false
                        var didLongPress = false
                        isLongPressLanguageSwitch = false
                        hasMovedCursorEarly = false

                        val languageSwitchJob = launch {
                            delay(700)
                            if (settings.holdSpaceToSwitchLanguage && !hasMovedCursorEarly) {
                                isLongPressLanguageSwitch = true
                                didLongPress = true
                            }
                        }

                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break

                                val dragX = change.position.x - lastX
                                val dragY = change.position.y - lastY
                                lastX = change.position.x
                                lastY = change.position.y
                                totalDragX += dragX
                                totalDragY += dragY
                                val duration = System.currentTimeMillis() - pressStartTime

                                if (isLongPressLanguageSwitch && !didSwitchLanguage) {
                                    if (abs(totalDragX) > 40f) {
                                        onCycleLanguage()
                                        didSwitchLanguage = true
                                    }
                                } else if (!isLongPressLanguageSwitch && settings.swipeSpaceToMoveCursor) {
                                    touchpadPosition = change.position
                                    if (abs(change.position.x - down.position.x) + abs(change.position.y - down.position.y) > 8f) {
                                        touchpadActive = true
                                        didTouchpadGesture = true
                                    }
                                    val targetStepsX = (totalDragX * 1.4f / cursorStepPx).toInt()
                                    val targetStepsY = (totalDragY * 1.4f / cursorStepPx).toInt()
                                    val stepsX = targetStepsX - emittedCursorStepsX
                                    val stepsY = targetStepsY - emittedCursorStepsY
                                    if (stepsX != 0 || stepsY != 0) {
                                        onMoveCursorByTouchpad(stepsX, stepsY)
                                        emittedCursorStepsX = targetStepsX
                                        emittedCursorStepsY = targetStepsY
                                        didMoveCursor = true
                                    }
                                    if (didMoveCursor && duration < 700) hasMovedCursorEarly = true
                                }

                                if (abs(dragX) > 2f || abs(dragY) > 2f || isLongPressLanguageSwitch) change.consume()
                            }
                        } finally {
                            languageSwitchJob.cancel()
                            isLongPressLanguageSwitch = false
                            hasMovedCursorEarly = false
                            touchpadActive = false
                        }

                        if (!didMoveCursor && !didSwitchLanguage && !didTouchpadGesture && !didLongPress &&
                            abs(totalDragX) < cursorStepPx && abs(totalDragY) < cursorStepPx
                        ) onClick()
                    }
                }
            }
            .testTag("key_space_gesture"),
        contentAlignment = Alignment.Center
    ) {
        if (isLongPressLanguageSwitch && !hasMovedCursorEarly) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = colorScheme.accentActionColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Lướt đổi ngôn ngữ ➜",
                    color = colorScheme.accentActionColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (settings.spaceBarVietnameseIndicator) {
            Text(
                text = displayLabel,
                color = colorScheme.secondaryTextColor.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Icon(
                imageVector = Icons.Default.SpaceBar,
                contentDescription = "Khoảng trắng",
                tint = colorScheme.secondaryTextColor.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
        if (touchpadActive) {
            Canvas(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (touchpadPosition.x - 4.dp.toPx()).roundToInt(),
                            (touchpadPosition.y - 4.dp.toPx()).roundToInt()
                        )
                    }
                    .size(18.dp)
            ) {
                val pointer = Path().apply {
                    moveTo(1f, 1f)
                    lineTo(1f, 16f)
                    lineTo(5f, 12f)
                    lineTo(8f, 18f)
                    lineTo(11f, 16.5f)
                    lineTo(8f, 11f)
                    lineTo(14f, 11f)
                    close()
                }
                drawPath(pointer, Color.White)
                drawPath(pointer, Color.Black, style = Stroke(width = 1.2f))
            }
        }
    }
}

@Composable
private fun RowScope.OneHandedSidebar(
    colorScheme: KeyboardColorScheme,
    keyHeight: Dp,
    onExpand: () -> Unit,
    onFlipSide: () -> Unit,
    onOpenSettings: () -> Unit,
    isRightSidebar: Boolean
) {
    val buttonSize = keyHeight.coerceAtLeast(24.dp)
    Column(
        modifier = Modifier
            .weight(0.15f)
            .fillMaxHeight()
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((keyHeight * 0.15f).coerceAtMost(8.dp))
    ) {
        Box(
            modifier = Modifier
            .size(buttonSize)
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.specialKeyColor)
                .clickable { onExpand() }
                .testTag("onehand_expand"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Toàn màn hình",
                tint = colorScheme.secondaryTextColor,
                modifier = Modifier.size(buttonSize * 0.52f)
            )
        }

        Box(
            modifier = Modifier
            .size(buttonSize)
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.specialKeyColor)
                .clickable { onFlipSide() }
                .testTag("onehand_flip"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRightSidebar) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                contentDescription = "Đổi tay",
                tint = colorScheme.secondaryTextColor,
                modifier = Modifier.size(buttonSize * 0.52f)
            )
        }

        Box(
            modifier = Modifier
            .size(buttonSize)
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.specialKeyColor)
                .clickable { onOpenSettings() }
                .testTag("onehand_settings"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Cài đặt",
                tint = colorScheme.secondaryTextColor,
                modifier = Modifier.size(buttonSize * 0.52f)
            )
        }
    }
}
