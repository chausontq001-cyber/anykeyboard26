package com.example.ime.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    OFF,
    ONCE,
    CAPS_LOCK
}

@Composable
fun ComposeKeyboardView(
    settings: KeyboardSettings,
    composingWord: String,
    suggestions: List<String>,
    onKeyTyped: (Char) -> Unit,
    onBackspace: () -> Unit,
    onFastDelete: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSelectSuggestion: (String) -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
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

    var floatingOffsetX by remember { mutableFloatStateOf(0f) }
    var floatingOffsetY by remember { mutableFloatStateOf(0f) }

    var layoutMode by remember { mutableStateOf(KeyboardLayoutMode.ALPHA) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }

    fun triggerHaptic() {
        if (settings.hapticFeedback && vibrator != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(12, 120))
                }
            } catch (_: Exception) {}
        }
    }

    val baseKeyHeight = (46 * (settings.keyboardHeightPercent / 100f) * (settings.keyScalePercent / 100f)).dp
    val fontScale = (settings.fontScalePercent / 100f) * (settings.keyScalePercent / 100f)

    // Container for Floating vs Standard/Docked vs Split
    if (isFloating) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
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
                        settings = settings,
                        composingWord = composingWord,
                        suggestions = suggestions,
                        colorScheme = colorScheme,
                        baseKeyHeight = baseKeyHeight * 0.9f,
                        fontScale = fontScale * 0.92f,
                        layoutMode = layoutMode,
                        shiftState = shiftState,
                        triggerHaptic = { triggerHaptic() },
                        onKeyTyped = onKeyTyped,
                        onBackspace = onBackspace,
                        onFastDelete = onFastDelete,
                        onSpace = onSpace,
                        onEnter = onEnter,
                        onSelectSuggestion = onSelectSuggestion,
                        onMoveCursorLeft = onMoveCursorLeft,
                        onMoveCursorRight = onMoveCursorRight,
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
                                ShiftState.OFF -> ShiftState.ONCE
                                ShiftState.ONCE -> ShiftState.CAPS_LOCK
                                ShiftState.CAPS_LOCK -> ShiftState.OFF
                            }
                        },
                        onSwitchLayout = { mode ->
                            triggerHaptic()
                            layoutMode = mode
                        },
                        onToggleTypingMethod = onToggleTypingMethod,
                        onUpdateAlignment = onUpdateAlignment,
                        onOpenSettings = onOpenSettings
                    )
                }
            }
        }
    } else {
        // Docked or Split Keyboard
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(colorScheme.backgroundColor)
                .testTag("compose_keyboard_root")
        ) {
            KeyboardContentWrapper(
                isSplit = isSplit,
                settings = settings,
                composingWord = composingWord,
                suggestions = suggestions,
                colorScheme = colorScheme,
                baseKeyHeight = baseKeyHeight,
                fontScale = fontScale,
                layoutMode = layoutMode,
                shiftState = shiftState,
                triggerHaptic = { triggerHaptic() },
                onKeyTyped = onKeyTyped,
                onBackspace = onBackspace,
                onFastDelete = onFastDelete,
                onSpace = onSpace,
                onEnter = onEnter,
                onSelectSuggestion = onSelectSuggestion,
                onMoveCursorLeft = onMoveCursorLeft,
                onMoveCursorRight = onMoveCursorRight,
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
                        ShiftState.OFF -> ShiftState.ONCE
                        ShiftState.ONCE -> ShiftState.CAPS_LOCK
                        ShiftState.CAPS_LOCK -> ShiftState.OFF
                    }
                },
                onSwitchLayout = { mode ->
                    triggerHaptic()
                    layoutMode = mode
                },
                onToggleTypingMethod = onToggleTypingMethod,
                onUpdateAlignment = onUpdateAlignment,
                onOpenSettings = onOpenSettings
            )
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
    onFastDelete: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSelectSuggestion: (String) -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
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
    onOpenSettings: () -> Unit
) {
    // 1. Suggestion Bar
    if (settings.showSuggestions || composingWord.isNotEmpty()) {
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
                    onFastDelete = {
                        triggerHaptic()
                        onFastDelete()
                    },
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
                    onCycleLanguage = {
                        triggerHaptic()
                        onCycleLanguage()
                    },
                    onToggleShift = onToggleShift,
                    onSwitchLayout = onSwitchLayout,
                    onToggleTypingMethod = onToggleTypingMethod
                )
            }

            OneHandedSidebar(
                colorScheme = colorScheme,
                onExpand = { onUpdateAlignment(KeyboardAlignment.CENTER) },
                onFlipSide = { onUpdateAlignment(KeyboardAlignment.RIGHT) },
                onOpenSettings = onOpenSettings,
                isRightSidebar = true
            )
        } else if (!isSplit && settings.alignment == KeyboardAlignment.RIGHT) {
            OneHandedSidebar(
                colorScheme = colorScheme,
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
                    onFastDelete = {
                        triggerHaptic()
                        onFastDelete()
                    },
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
                    onCycleLanguage = {
                        triggerHaptic()
                        onCycleLanguage()
                    },
                    onToggleShift = onToggleShift,
                    onSwitchLayout = onSwitchLayout,
                    onToggleTypingMethod = onToggleTypingMethod
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
                    onFastDelete = {
                        triggerHaptic()
                        onFastDelete()
                    },
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
                    onCycleLanguage = {
                        triggerHaptic()
                        onCycleLanguage()
                    },
                    onToggleShift = onToggleShift,
                    onSwitchLayout = onSwitchLayout,
                    onToggleTypingMethod = onToggleTypingMethod
                )
            }
        }
    }

    // 3. Virtual Navigation Bar
    if (settings.showVirtualNavBar) {
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
    onFastDelete: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onCycleLanguage: () -> Unit,
    onToggleShift: () -> Unit,
    onSwitchLayout: (KeyboardLayoutMode) -> Unit,
    onToggleTypingMethod: () -> Unit
) {
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
                val isUpper = shiftState != ShiftState.OFF

                if (isSplit) {
                    // SPLIT KEYBOARD LAYOUT (Left and Right thumb clusters with center space)
                    // Row 1: q w e r t  --- GAP ---  y u i o p
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("q", "w", "e", "r", "t").forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(display.first()) })
                        }
                        Spacer(modifier = Modifier.weight(1.8f))
                        listOf("y", "u", "i", "o", "p").forEach { k ->
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
                        listOf("a", "s", "d", "f", "g").forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(display.first()) })
                        }
                        Spacer(modifier = Modifier.weight(2.2f))
                        listOf("h", "j", "k", "l").forEach { k ->
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
                            contentDescription = "Shift",
                            colorScheme = colorScheme,
                            isActive = shiftState != ShiftState.OFF,
                            height = baseKeyHeight,
                            weight = 1.3f,
                            onClick = onToggleShift
                        )
                        listOf("z", "x", "c", "v").forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(display.first()) })
                        }
                        Spacer(modifier = Modifier.weight(1.8f))
                        listOf("b", "n", "m").forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(display.first()) })
                        }
                        BackspaceGestureKeyCap(
                            colorScheme = colorScheme,
                            height = baseKeyHeight,
                            weight = 1.3f,
                            enableSwipeDelete = settings.swipeBackspaceToDeleteFast,
                            onClick = onBackspace,
                            onFastDelete = onFastDelete
                        )
                    }

                    // Row 4: ?123, Emoji, Left Space  --- GAP (Arrows) ---  Right Space, ., Enter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KeyCap(text = "?123", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (13 * fontScale).sp, isSpecial = true, weight = 1.2f, onClick = { onSwitchLayout(KeyboardLayoutMode.NUMBERS_SYMBOLS) })
                        SpecialKeyCap(icon = Icons.Default.EmojiEmotions, contentDescription = "Emoji", colorScheme = colorScheme, height = baseKeyHeight, weight = 1f, onClick = { onSwitchLayout(KeyboardLayoutMode.EMOJI) })
                        SpaceGestureKeyCap(settings = settings, colorScheme = colorScheme, height = baseKeyHeight, weight = 2.5f, onClick = onSpace, onMoveCursorLeft = onMoveCursorLeft, onMoveCursorRight = onMoveCursorRight, onCycleLanguage = onCycleLanguage)

                        // Center Gap with Quick Cursor Arrows in Split mode
                        Row(modifier = Modifier.weight(1.6f), horizontalArrangement = Arrangement.Center) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trái", tint = colorScheme.secondaryTextColor, modifier = Modifier.size(20.dp).clickable { onMoveCursorLeft() })
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Phải", tint = colorScheme.secondaryTextColor, modifier = Modifier.size(20.dp).clickable { onMoveCursorRight() })
                        }

                        SpaceGestureKeyCap(settings = settings, colorScheme = colorScheme, height = baseKeyHeight, weight = 2.5f, onClick = onSpace, onMoveCursorLeft = onMoveCursorLeft, onMoveCursorRight = onMoveCursorRight, onCycleLanguage = onCycleLanguage)
                        KeyCap(text = ".", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 0.9f, onClick = { onKeyTyped('.') })
                        SpecialKeyCap(icon = Icons.AutoMirrored.Filled.KeyboardReturn, contentDescription = "Nhập", colorScheme = colorScheme, isAccent = true, height = baseKeyHeight, weight = 1.2f, onClick = onEnter)
                    }
                } else {
                    // STANDARD FULL DOCKED KEYBOARD
                    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        row1.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (19 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(display.first()) })
                        }
                    }

                    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        row2.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (19 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(display.first()) })
                        }
                    }

                    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        SpecialKeyCap(
                            icon = if (shiftState == ShiftState.CAPS_LOCK) Icons.Default.KeyboardCapslock else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Shift",
                            colorScheme = colorScheme,
                            isActive = shiftState != ShiftState.OFF,
                            height = baseKeyHeight,
                            weight = 1.4f,
                            onClick = onToggleShift
                        )
                        row3.forEach { k ->
                            val display = if (isUpper) k.uppercase() else k
                            KeyCap(text = display, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (19 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(display.first()) })
                        }
                        BackspaceGestureKeyCap(colorScheme = colorScheme, height = baseKeyHeight, weight = 1.4f, enableSwipeDelete = settings.swipeBackspaceToDeleteFast, onClick = onBackspace, onFastDelete = onFastDelete)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        KeyCap(text = "?123", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (14 * fontScale).sp, isSpecial = true, weight = 1.4f, onClick = { onSwitchLayout(KeyboardLayoutMode.NUMBERS_SYMBOLS) })
                        SpecialKeyCap(icon = Icons.Default.EmojiEmotions, contentDescription = "Emoji", colorScheme = colorScheme, height = baseKeyHeight, weight = 1f, onClick = { onSwitchLayout(KeyboardLayoutMode.EMOJI) })
                        SpaceGestureKeyCap(settings = settings, colorScheme = colorScheme, height = baseKeyHeight, weight = 4.2f, onClick = onSpace, onMoveCursorLeft = onMoveCursorLeft, onMoveCursorRight = onMoveCursorRight, onCycleLanguage = onCycleLanguage)
                        KeyCap(text = ".", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped('.') })
                        SpecialKeyCap(icon = Icons.AutoMirrored.Filled.KeyboardReturn, contentDescription = "Nhập", colorScheme = colorScheme, isAccent = true, height = baseKeyHeight, weight = 1.4f, onClick = onEnter)
                    }
                }
            }

            KeyboardLayoutMode.NUMBERS_SYMBOLS -> {
                val symRow1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    symRow1.forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(s.first()) })
                    }
                }

                val symRow2 = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    symRow2.forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (17 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(s.first()) })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    KeyCap(text = "=/<", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (13 * fontScale).sp, isSpecial = true, weight = 1.4f, onClick = { onSwitchLayout(KeyboardLayoutMode.MORE_SYMBOLS) })
                    listOf("*", "\"", "'", ":", ";", "!", "?").forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (17 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(s.first()) })
                    }
                    BackspaceGestureKeyCap(colorScheme = colorScheme, height = baseKeyHeight, weight = 1.4f, enableSwipeDelete = settings.swipeBackspaceToDeleteFast, onClick = onBackspace, onFastDelete = onFastDelete)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    KeyCap(text = "ABC", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (14 * fontScale).sp, isSpecial = true, weight = 1.4f, onClick = { onSwitchLayout(KeyboardLayoutMode.ALPHA) })
                    SpecialKeyCap(icon = Icons.Default.EmojiEmotions, contentDescription = "Emoji", colorScheme = colorScheme, height = baseKeyHeight, weight = 1f, onClick = { onSwitchLayout(KeyboardLayoutMode.EMOJI) })
                    SpaceGestureKeyCap(settings = settings, colorScheme = colorScheme, height = baseKeyHeight, weight = 4.2f, onClick = onSpace, onMoveCursorLeft = onMoveCursorLeft, onMoveCursorRight = onMoveCursorRight, onCycleLanguage = onCycleLanguage)
                    KeyCap(text = ",", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(',') })
                    SpecialKeyCap(icon = Icons.AutoMirrored.Filled.KeyboardReturn, contentDescription = "Nhập", colorScheme = colorScheme, isAccent = true, height = baseKeyHeight, weight = 1.4f, onClick = onEnter)
                }
            }

            KeyboardLayoutMode.MORE_SYMBOLS -> {
                val more1 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    more1.forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (17 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(s.first()) })
                    }
                }

                val more2 = listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    more2.forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (17 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(s.first()) })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    KeyCap(text = "?123", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (13 * fontScale).sp, isSpecial = true, weight = 1.4f, onClick = { onSwitchLayout(KeyboardLayoutMode.NUMBERS_SYMBOLS) })
                    listOf("%", "_", "<", ">", "[", "]", "§").forEach { s ->
                        KeyCap(text = s, colorScheme = colorScheme, height = baseKeyHeight, fontSize = (17 * fontScale).sp, weight = 1f, onClick = { onKeyTyped(s.first()) })
                    }
                    BackspaceGestureKeyCap(colorScheme = colorScheme, height = baseKeyHeight, weight = 1.4f, enableSwipeDelete = settings.swipeBackspaceToDeleteFast, onClick = onBackspace, onFastDelete = onFastDelete)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    KeyCap(text = "ABC", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (14 * fontScale).sp, isSpecial = true, weight = 1.4f, onClick = { onSwitchLayout(KeyboardLayoutMode.ALPHA) })
                    SpecialKeyCap(icon = Icons.Default.EmojiEmotions, contentDescription = "Emoji", colorScheme = colorScheme, height = baseKeyHeight, weight = 1f, onClick = { onSwitchLayout(KeyboardLayoutMode.EMOJI) })
                    SpaceGestureKeyCap(settings = settings, colorScheme = colorScheme, height = baseKeyHeight, weight = 4.2f, onClick = onSpace, onMoveCursorLeft = onMoveCursorLeft, onMoveCursorRight = onMoveCursorRight, onCycleLanguage = onCycleLanguage)
                    KeyCap(text = ".", colorScheme = colorScheme, height = baseKeyHeight, fontSize = (18 * fontScale).sp, weight = 1f, onClick = { onKeyTyped('.') })
                    SpecialKeyCap(icon = Icons.AutoMirrored.Filled.KeyboardReturn, contentDescription = "Nhập", colorScheme = colorScheme, isAccent = true, height = baseKeyHeight, weight = 1.4f, onClick = onEnter)
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
                        columns = GridCells.Fixed(8),
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        emoji.forEach { c -> onKeyTyped(c) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 22.sp)
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
                            onMoveCursorLeft = onMoveCursorLeft,
                            onMoveCursorRight = onMoveCursorRight,
                            onCycleLanguage = onCycleLanguage
                        )

                        BackspaceGestureKeyCap(
                            colorScheme = colorScheme,
                            height = baseKeyHeight,
                            weight = 2f,
                            enableSwipeDelete = settings.swipeBackspaceToDeleteFast,
                            onClick = onBackspace,
                            onFastDelete = onFastDelete
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
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bg = when {
        isPressed -> colorScheme.accentActionColor.copy(alpha = 0.35f)
        isSpecial -> colorScheme.specialKeyColor
        else -> colorScheme.keyBackgroundColor
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(height)
            .shadow(if (isPressed) 0.dp else 1.dp, RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .border(0.5.dp, colorScheme.borderColor.copy(alpha = 0.4f), RoundedCornerShape(7.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = colorScheme.accentActionColor)
            ) { onClick() }
            .testTag("key_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSpecial) colorScheme.secondaryTextColor else colorScheme.primaryTextColor,
            fontSize = fontSize,
            fontWeight = if (isSpecial) FontWeight.SemiBold else FontWeight.Medium
        )
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

    val bg = when {
        isPressed -> colorScheme.accentActionColor.copy(alpha = 0.4f)
        isAccent -> colorScheme.accentActionColor
        isActive -> colorScheme.accentActionColor.copy(alpha = 0.25f)
        else -> colorScheme.specialKeyColor
    }

    val iconTint = when {
        isAccent -> if (colorScheme.isDark) Color.Black else Color.White
        isActive -> colorScheme.accentActionColor
        else -> colorScheme.secondaryTextColor
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(height)
            .shadow(if (isPressed) 0.dp else 1.dp, RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .border(0.5.dp, colorScheme.borderColor.copy(alpha = 0.4f), RoundedCornerShape(7.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = colorScheme.accentActionColor)
            ) { onClick() }
            .testTag("special_key_$contentDescription"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun RowScope.BackspaceGestureKeyCap(
    colorScheme: KeyboardColorScheme,
    height: Dp,
    weight: Float,
    enableSwipeDelete: Boolean,
    onClick: () -> Unit,
    onFastDelete: () -> Unit
) {
    var isSwiping by remember { mutableStateOf(false) }

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
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var lastX = down.position.x
                        var totalDragX = 0f
                        var didSwipeDelete = false
                        var wasLongPress = false
                        isSwiping = false

                        val repeatJob = launch {
                            delay(viewConfiguration.longPressTimeoutMillis)
                            if (!didSwipeDelete) {
                                wasLongPress = true
                                while (isActive) {
                                    onClick()
                                    delay(55)
                                }
                            }
                        }

                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break

                                totalDragX += change.position.x - lastX
                                lastX = change.position.x

                                if (enableSwipeDelete && !didSwipeDelete && totalDragX > 25f) {
                                    didSwipeDelete = true
                                    isSwiping = true
                                    repeatJob.cancel()
                                    onFastDelete()
                                }
                                if (didSwipeDelete || abs(totalDragX) > 8f) {
                                    change.consume()
                                }
                            }
                        } finally {
                            repeatJob.cancel()
                            isSwiping = false
                        }

                        if (!didSwipeDelete && !wasLongPress && abs(totalDragX) < 15f) onClick()
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
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onCycleLanguage: () -> Unit
) {
    val langPack = remember(settings.activeLanguageId) {
        WorldLanguagesRepository.getLanguage(settings.activeLanguageId)
    }

    val displayLabel = buildString {
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

    var isLongPressLanguageSwitch by remember { mutableStateOf(false) }
    var hasMovedCursorEarly by remember { mutableStateOf(false) }

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
                        var totalDragX = 0f
                        var cursorStepDrag = 0f
                        var didMoveCursor = false
                        var didSwitchLanguage = false
                        isLongPressLanguageSwitch = false
                        hasMovedCursorEarly = false

                        val languageSwitchJob = launch {
                            delay(700)
                            if (settings.holdSpaceToSwitchLanguage && !hasMovedCursorEarly) {
                                isLongPressLanguageSwitch = true
                            }
                        }

                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break

                                val dragX = change.position.x - lastX
                                lastX = change.position.x
                                totalDragX += dragX
                                val duration = System.currentTimeMillis() - pressStartTime

                                if (isLongPressLanguageSwitch && !didSwitchLanguage) {
                                    if (abs(totalDragX) > 40f) {
                                        onCycleLanguage()
                                        didSwitchLanguage = true
                                    }
                                } else if (!isLongPressLanguageSwitch && settings.swipeSpaceToMoveCursor) {
                                    cursorStepDrag += dragX
                                    if (cursorStepDrag > 18f) {
                                        onMoveCursorRight()
                                        cursorStepDrag = 0f
                                        didMoveCursor = true
                                        if (duration < 700) hasMovedCursorEarly = true
                                    } else if (cursorStepDrag < -18f) {
                                        onMoveCursorLeft()
                                        cursorStepDrag = 0f
                                        didMoveCursor = true
                                        if (duration < 700) hasMovedCursorEarly = true
                                    }
                                }

                                if (abs(dragX) > 2f || isLongPressLanguageSwitch) change.consume()
                            }
                        } finally {
                            languageSwitchJob.cancel()
                            isLongPressLanguageSwitch = false
                            hasMovedCursorEarly = false
                        }

                        if (!didMoveCursor && !didSwitchLanguage && abs(totalDragX) < 18f) onClick()
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
    }
}

@Composable
private fun RowScope.OneHandedSidebar(
    colorScheme: KeyboardColorScheme,
    onExpand: () -> Unit,
    onFlipSide: () -> Unit,
    onOpenSettings: () -> Unit,
    isRightSidebar: Boolean
) {
    Column(
        modifier = Modifier
            .weight(0.15f)
            .fillMaxHeight()
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
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
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(38.dp)
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
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(38.dp)
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
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
