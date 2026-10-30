package com.example.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.model.KeyboardColorScheme
import com.example.ime.model.KeyboardSettings

@Composable
fun VirtualNavigationBar(
    settings: KeyboardSettings,
    colorScheme: KeyboardColorScheme,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onMoveCursorStart: () -> Unit,
    onMoveCursorEnd: () -> Unit,
    onSelectAll: () -> Unit,
    onPaste: () -> Unit,
    onSwitchIme: () -> Unit,
    onHideKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!settings.showVirtualNavBar) return

    val heightDp = settings.bottomChinHeightDp.dp
    val buttonSize = (heightDp - 4.dp).coerceIn(24.dp, 40.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .background(colorScheme.navBarBgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (settings.navBarShowLineJump) {
                NavBarIconButton(
                    icon = Icons.Default.FirstPage,
                    contentDescription = "Đầu dòng",
                    colorScheme = colorScheme,
                    testTag = "nav_btn_first_page",
                    buttonSize = buttonSize,
                    onClick = onMoveCursorStart
                )
            }

            if (settings.navBarShowCursorArrows) {
                NavBarIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Sang trái",
                    colorScheme = colorScheme,
                    testTag = "nav_btn_cursor_left",
                    buttonSize = buttonSize,
                    onClick = onMoveCursorLeft
                )
            }

            // Quick select all or paste button
            if (settings.navBarShowSelectAll) {
                NavBarIconButton(
                    icon = Icons.Default.SelectAll,
                    contentDescription = "Chọn tất cả",
                    colorScheme = colorScheme,
                    testTag = "nav_btn_select_all",
                    buttonSize = buttonSize,
                    onClick = onSelectAll
                )
            }

            if (settings.navBarShowPaste) {
                NavBarIconButton(
                    icon = Icons.Default.ContentPaste,
                    contentDescription = "Dán",
                    colorScheme = colorScheme,
                    testTag = "nav_btn_paste",
                    buttonSize = buttonSize,
                    onClick = onPaste
                )
            }

            if (settings.navBarShowCursorArrows) {
                NavBarIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Sang phải",
                    colorScheme = colorScheme,
                    testTag = "nav_btn_cursor_right",
                    buttonSize = buttonSize,
                    onClick = onMoveCursorRight
                )
            }

            if (settings.navBarShowLineJump) {
                NavBarIconButton(
                    icon = Icons.AutoMirrored.Filled.LastPage,
                    contentDescription = "Cuối dòng",
                    colorScheme = colorScheme,
                    testTag = "nav_btn_last_page",
                    buttonSize = buttonSize,
                    onClick = onMoveCursorEnd
                )
            }

            NavBarIconButton(
                icon = Icons.Default.Language,
                contentDescription = "Chuyển bàn phím",
                colorScheme = colorScheme,
                testTag = "nav_btn_switch_ime",
                buttonSize = buttonSize,
                onClick = onSwitchIme
            )

            if (settings.navBarShowHideKeyboard) {
                NavBarIconButton(
                    icon = Icons.Default.KeyboardHide,
                    contentDescription = "Thu bàn phím",
                    colorScheme = colorScheme,
                    testTag = "nav_btn_hide_keyboard",
                    buttonSize = buttonSize,
                    onClick = onHideKeyboard
                )
            }
        }
    }
}

@Composable
private fun NavBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    colorScheme: KeyboardColorScheme,
    testTag: String,
    buttonSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colorScheme.accentActionColor)
            ) { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colorScheme.navBarIconColor,
            modifier = Modifier.size(buttonSize * 0.5f)
        )
    }
}
