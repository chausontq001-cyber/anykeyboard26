package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AlignHorizontalLeft
import androidx.compose.material.icons.filled.AlignHorizontalRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.model.KeyboardAlignment
import com.example.ime.model.KeyboardColorScheme
import com.example.ime.model.KeyboardPreferences
import com.example.ime.model.KeyboardThemeId
import com.example.ime.model.KeyboardThemes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppearanceScreen(
    preferences: KeyboardPreferences,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settings.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Themes & Colors
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().testTag("theme_selection_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Chủ đề & Màu sắc bàn phím",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 1. Follow System vs Manual Theme Mode
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // System Mode Option
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (settings.themeMode == com.example.ime.model.ThemeMode.FOLLOW_SYSTEM) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                }
                            )
                            .border(
                                width = if (settings.themeMode == com.example.ime.model.ThemeMode.FOLLOW_SYSTEM) 2.dp else 1.dp,
                                color = if (settings.themeMode == com.example.ime.model.ThemeMode.FOLLOW_SYSTEM) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                preferences.updateSettings { it.copy(themeMode = com.example.ime.model.ThemeMode.FOLLOW_SYSTEM) }
                            }
                            .padding(14.dp)
                            .testTag("theme_mode_system")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🌙 Theo chế độ hệ thống Android",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tự động đổi màu Tối khi máy bật Dark mode, và màu Sáng khi máy ở Light mode.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            androidx.compose.material3.RadioButton(
                                selected = settings.themeMode == com.example.ime.model.ThemeMode.FOLLOW_SYSTEM,
                                onClick = {
                                    preferences.updateSettings { it.copy(themeMode = com.example.ime.model.ThemeMode.FOLLOW_SYSTEM) }
                                }
                            )
                        }
                    }

                    // Manual Theme Option
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (settings.themeMode == com.example.ime.model.ThemeMode.MANUAL) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                }
                            )
                            .border(
                                width = if (settings.themeMode == com.example.ime.model.ThemeMode.MANUAL) 2.dp else 1.dp,
                                color = if (settings.themeMode == com.example.ime.model.ThemeMode.MANUAL) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                preferences.updateSettings { it.copy(themeMode = com.example.ime.model.ThemeMode.MANUAL) }
                            }
                            .padding(14.dp)
                            .testTag("theme_mode_manual")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎨 Chọn chủ đề màu thủ công",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cố định màu sắc bàn phím theo một trong 7 bộ phối màu đặc sắc bên dưới.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            androidx.compose.material3.RadioButton(
                                selected = settings.themeMode == com.example.ime.model.ThemeMode.MANUAL,
                                onClick = {
                                    preferences.updateSettings { it.copy(themeMode = com.example.ime.model.ThemeMode.MANUAL) }
                                }
                            )
                        }
                    }
                }

                // Themes Grid / List (highlighted when manual)
                Text(
                    text = "Bảng màu sẵn có:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                KeyboardThemes.allThemes.forEach { theme ->
                    val isSelected = settings.themeId == theme.id && settings.themeMode == com.example.ime.model.ThemeMode.MANUAL

                    ThemePreviewItem(
                        theme = theme,
                        isSelected = isSelected,
                        onClick = {
                            preferences.updateSettings {
                                it.copy(
                                    themeId = theme.id,
                                    themeMode = com.example.ime.model.ThemeMode.MANUAL
                                )
                            }
                        }
                    )
                }
            }
        }

        // Section 2: Size & Dimensions
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().testTag("size_adjustment_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Height,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Điều chỉnh kích thước bàn phím",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 1. Keyboard Height Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Chiều cao bàn phím:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${settings.keyboardHeightPercent}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = settings.keyboardHeightPercent.toFloat(),
                        onValueChange = { newVal ->
                            preferences.updateSettings { it.copy(keyboardHeightPercent = newVal.toInt()) }
                        },
                        valueRange = 80f..130f,
                        steps = 9,
                        modifier = Modifier.testTag("slider_keyboard_height")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nhỏ (80%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Chuẩn (100%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Cực cao (130%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // 2. Key Size Scale Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Kích cỡ các phím (Key Size):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${settings.keyScalePercent}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = settings.keyScalePercent.toFloat(),
                        onValueChange = { newVal ->
                            preferences.updateSettings { it.copy(keyScalePercent = newVal.toInt()) }
                        },
                        valueRange = 80f..130f,
                        steps = 9,
                        modifier = Modifier.testTag("slider_key_scale")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nhỏ (80%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Chuẩn (100%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Lớn (130%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // 3. Row Spacing Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Khoảng cách hàng phím (Row Spacing):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${settings.rowSpacingDp} dp",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = settings.rowSpacingDp.toFloat(),
                        onValueChange = { newVal ->
                            preferences.updateSettings { it.copy(rowSpacingDp = newVal.toInt()) }
                        },
                        valueRange = 2f..12f,
                        steps = 9,
                        modifier = Modifier.testTag("slider_row_spacing")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Khít (2dp)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tiêu chuẩn (5dp)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Thoáng (12dp)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // 4. Key Font Scale Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cỡ chữ trên phím (Font scale):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${settings.fontScalePercent}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = settings.fontScalePercent.toFloat(),
                        onValueChange = { newVal ->
                            preferences.updateSettings { it.copy(fontScalePercent = newVal.toInt()) }
                        },
                        valueRange = 85f..130f,
                        steps = 8,
                        modifier = Modifier.testTag("slider_font_scale")
                    )

                    // Font preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Xem trước cỡ chữ: A a Ă ă Â â Đ đ E e Ê ê Ô ô Ơ ơ Ư ư",
                            fontSize = (14 * (settings.fontScalePercent / 100f)).sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 3. Virtual Navigation Bar (Bottom Chin) Height
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Khoảng trống thanh điều hướng đáy:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${settings.bottomChinHeightDp} dp",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = settings.bottomChinHeightDp.toFloat(),
                        onValueChange = { newVal ->
                            preferences.updateSettings { it.copy(bottomChinHeightDp = newVal.toInt()) }
                        },
                        valueRange = 36f..64f,
                        steps = 6,
                        modifier = Modifier.testTag("slider_chin_height")
                    )

                    Text(
                        text = "Khoảng trống nâng bàn phím cao hơn mép dưới, giúp thao tác ngón cái chính xác và không bị cấn thanh cử chỉ Android.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 3: Alignment & Position on Screen
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().testTag("alignment_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewSidebar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Vị trí hiển thị trên màn hình",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Điều chỉnh vị trí bàn phím để gõ thuận tiện bằng một tay hoặc toàn màn hình:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left
                    PositionChoiceCard(
                        title = "Một tay trái",
                        icon = Icons.Default.AlignHorizontalLeft,
                        isSelected = settings.alignment == KeyboardAlignment.LEFT,
                        modifier = Modifier.weight(1f).testTag("pos_btn_left"),
                        onClick = {
                            preferences.updateSettings { it.copy(alignment = KeyboardAlignment.LEFT) }
                        }
                    )

                    // Center
                    PositionChoiceCard(
                        title = "Căn giữa",
                        icon = Icons.Default.AlignHorizontalCenter,
                        isSelected = settings.alignment == KeyboardAlignment.CENTER,
                        modifier = Modifier.weight(1f).testTag("pos_btn_center"),
                        onClick = {
                            preferences.updateSettings { it.copy(alignment = KeyboardAlignment.CENTER) }
                        }
                    )

                    // Right
                    PositionChoiceCard(
                        title = "Một tay phải",
                        icon = Icons.Default.AlignHorizontalRight,
                        isSelected = settings.alignment == KeyboardAlignment.RIGHT,
                        modifier = Modifier.weight(1f).testTag("pos_btn_right"),
                        onClick = {
                            preferences.updateSettings { it.copy(alignment = KeyboardAlignment.RIGHT) }
                        }
                    )
                }
            }
        }

        // Section 4: Floating & Split Keyboard (Bàn phím nổi & Bàn phím tách rời)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().testTag("display_style_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartButton,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Bàn phím nổi & Bàn phím tách rời",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Tùy biến chế độ hiển thị trên màn hình dọc (Portrait) và màn hình ngang (Landscape):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 3 Display Style Choices
                com.example.ime.model.KeyboardDisplayStyle.values().forEach { style ->
                    val isSelected = settings.displayStyle == style
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                preferences.updateSettings { it.copy(displayStyle = style) }
                            }
                            .padding(12.dp)
                            .testTag("style_${style.name}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (style) {
                                        com.example.ime.model.KeyboardDisplayStyle.DOCKED -> "📌 Gắn đáy chuẩn (Toàn màn hình)"
                                        com.example.ime.model.KeyboardDisplayStyle.FLOATING -> "🪟 Bàn phím nổi (Floating)"
                                        com.example.ime.model.KeyboardDisplayStyle.SPLIT -> "✂️ Bàn phím tách rời (Split keyboard)"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = style.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            androidx.compose.material3.RadioButton(
                                selected = isSelected,
                                onClick = {
                                    preferences.updateSettings { it.copy(displayStyle = style) }
                                }
                            )
                        }
                    }
                }

                // Auto-split toggle for Landscape mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Tự động tách đôi khi xoay ngang (Landscape)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Khi xoay điện thoại nằm ngang, bàn phím tự động tách 2 bên để 2 ngón tay cái gõ nhanh và thuận tiện nhất.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.autoSplitInLandscape,
                        onCheckedChange = { c ->
                            preferences.updateSettings { it.copy(autoSplitInLandscape = c) }
                        },
                        modifier = Modifier.testTag("switch_auto_split_landscape")
                    )
                }

                // Floating keyboard size slider if Floating is enabled
                if (settings.displayStyle == com.example.ime.model.KeyboardDisplayStyle.FLOATING) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kích cỡ cửa sổ bàn phím nổi:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${settings.floatingScalePercent}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = settings.floatingScalePercent.toFloat(),
                            onValueChange = { newVal ->
                                preferences.updateSettings { it.copy(floatingScalePercent = newVal.toInt()) }
                            },
                            valueRange = 70f..95f,
                            steps = 5,
                            modifier = Modifier.testTag("slider_floating_scale")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewItem(
    theme: KeyboardColorScheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("theme_item_${theme.id.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color palette circles preview
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.backgroundColor)
                        .border(1.dp, theme.borderColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(theme.keyBackgroundColor)
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(theme.accentActionColor)
                        )
                    }
                }

                Column {
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                        text = if (theme.isDark) "Giao diện tối (Dark)" else "Giao diện sáng (Light)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Đã chọn",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun PositionChoiceCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
