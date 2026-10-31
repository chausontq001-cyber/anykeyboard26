package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.model.KeyboardDisplayStyle
import com.example.ime.model.KeyboardPreferences
import com.example.ime.model.KeyboardThemeId
import com.example.ime.model.KeyboardThemes
import com.example.ime.model.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppearanceScreen(
    preferences: KeyboardPreferences,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settings.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var expandedGroupId by remember { mutableStateOf<String?>("group_theme") }

    fun toggleGroup(groupId: String, index: Int) {
        val willOpen = expandedGroupId != groupId
        expandedGroupId = if (willOpen) groupId else null
        if (willOpen) {
            coroutineScope.launch {
                listState.animateScrollToItem(index)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Nhóm 1: Chủ đề & Màu sắc
        item {
            val isExpanded = expandedGroupId == "group_theme"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_theme")
            ) {
                Column {
                    AccordionHeader(
                        title = "Chủ đề & Màu sắc",
                        subtitle = "Chế độ hệ thống Sáng/Tối & các bảng màu đẹp",
                        icon = Icons.Default.Palette,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_theme", 0) }
                    )

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // 1. Chế độ theo hệ thống
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (settings.themeMode == ThemeMode.FOLLOW_SYSTEM) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        }
                                    )
                                    .border(
                                        width = if (settings.themeMode == ThemeMode.FOLLOW_SYSTEM) 2.dp else 1.dp,
                                        color = if (settings.themeMode == ThemeMode.FOLLOW_SYSTEM) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        preferences.updateSettings { it.copy(themeMode = ThemeMode.FOLLOW_SYSTEM) }
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
                                            text = "Tự động đổi màu Tối khi máy bật Dark mode, và màu Sáng khi ở Light mode.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (settings.themeMode == ThemeMode.FOLLOW_SYSTEM) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            // 2. Danh sách chủ đề thủ công
                            Text(
                                text = "Hoặc chọn bộ màu cố định:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                KeyboardThemes.allThemes.forEach { theme ->
                                    val isSelected = settings.themeMode == ThemeMode.MANUAL && settings.themeId == theme.id
                                    OutlinedCard(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                            )
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                preferences.updateSettings {
                                                    it.copy(
                                                        themeMode = ThemeMode.MANUAL,
                                                        themeId = theme.id
                                                    )
                                                }
                                            }
                                            .padding(2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.accentActionColor)
                                            )
                                            Text(
                                                text = theme.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Nhóm 2: Kích thước & Khoảng cách phím
        item {
            val isExpanded = expandedGroupId == "group_sizing"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_sizing")
            ) {
                Column {
                    AccordionHeader(
                        title = "Kích thước & Khoảng cách phím",
                        subtitle = "Chiều cao bàn phím, tỉ lệ phím, khoảng cách hàng, cỡ chữ",
                        icon = Icons.Default.Height,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_sizing", 1) }
                    )

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // 1. Chiều cao bàn phím tổng thể
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Chiều cao bàn phím", fontWeight = FontWeight.Medium)
                                    Text("${settings.keyboardHeightPercent}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = settings.keyboardHeightPercent.toFloat(),
                                    onValueChange = { preferences.updateSettings { s -> s.copy(keyboardHeightPercent = it.toInt()) } },
                                    valueRange = 80f..130f,
                                    steps = 9
                                )
                            }

                            // 2. Tỉ lệ kích thước phím
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Kích thước các phím", fontWeight = FontWeight.Medium)
                                    Text("${settings.keyScalePercent}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = settings.keyScalePercent.toFloat(),
                                    onValueChange = { preferences.updateSettings { s -> s.copy(keyScalePercent = it.toInt()) } },
                                    valueRange = 80f..130f,
                                    steps = 9
                                )
                            }

                            // 3. Khoảng cách giữa các hàng phím
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Khoảng cách hàng phím", fontWeight = FontWeight.Medium)
                                    Text("${settings.rowSpacingDp} dp", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = settings.rowSpacingDp.toFloat(),
                                    onValueChange = { preferences.updateSettings { s -> s.copy(rowSpacingDp = it.toInt()) } },
                                    valueRange = 2f..12f,
                                    steps = 9
                                )
                            }

                            // 4. Cỡ chữ trên phím
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cỡ chữ trên mặt phím", fontWeight = FontWeight.Medium)
                                    Text("${settings.fontScalePercent}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = settings.fontScalePercent.toFloat(),
                                    onValueChange = { preferences.updateSettings { s -> s.copy(fontScalePercent = it.toInt()) } },
                                    valueRange = 85f..130f,
                                    steps = 8
                                )
                            }
                        }
                    }
                }
            }
        }

        // Nhóm 3: Hiệu ứng & Xem trước (YÊU CẦU 1)
        item {
            val isExpanded = expandedGroupId == "group_preview_effects"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_preview_effects")
            ) {
                Column {
                    AccordionHeader(
                        title = "Hiệu ứng & Xem trước phím",
                        subtitle = "Bật/tắt popup phóng to, hàng số chuyên dụng, nhãn phím cách",
                        icon = Icons.Default.SmartButton,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_preview_effects", 2) }
                    )

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // YÊU CẦU 1: Tùy chọn Hiệu ứng Popup khi bấm phím
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("switch_show_key_preview"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Hiệu ứng Popup khi bấm phím",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Hiển thị bong bóng nổi phóng to ký tự trực tiếp trên phím đang nhấn.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.showKeyPreview,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(showKeyPreview = checked) }
                                    }
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth().testTag("switch_show_comma_key"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hiển thị phím phẩy (,)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.showCommaKey,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(showCommaKey = checked) }
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().testTag("switch_show_period_key"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hiển thị phím chấm (.)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.showPeriodKey,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(showPeriodKey = checked) }
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().testTag("switch_show_emoji_key"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hiển thị phím Emoji (😀)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.showEmojiKey,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(showEmojiKey = checked) }
                                    }
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Hàng phím số chuyên dụng
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Hàng phím số (1-0) ở trên cùng", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "Luôn hiển thị dãy số 1234567890 trên bàn phím chữ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.showNumberRow,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(showNumberRow = checked) }
                                    }
                                )
                            }

                            // Hàng số khi xoay ngang
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Hiện hàng số khi xoay ngang", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "Cho phép hàng số xuất hiện trên màn hình ngang",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.showNumberRowInLandscape,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(showNumberRowInLandscape = checked) }
                                    }
                                )
                            }

                            // Nhãn cờ / kiểu gõ trên phím cách
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Hiển thị nhãn trên phím cách (Space)", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "Hiện cờ quốc gia và tên phương thức gõ (Telex/VNI)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.spaceBarVietnameseIndicator,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(spaceBarVietnameseIndicator = checked) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Nhóm 4: Bàn phím nổi & Tách rời
        item {
            val isExpanded = expandedGroupId == "group_floating_split"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_floating_split")
            ) {
                Column {
                    AccordionHeader(
                        title = "Bàn phím nổi & Tách rời",
                        subtitle = "Chế độ gắn đáy, nổi di chuyển tự do, hoặc tách 2 bên",
                        icon = Icons.Default.CallSplit,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_floating_split", 3) }
                    )

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Chế độ hiển thị
                            Text("Chế độ hiển thị bàn phím:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    KeyboardDisplayStyle.DOCKED to "Gắn đáy",
                                    KeyboardDisplayStyle.FLOATING to "Nổi",
                                    KeyboardDisplayStyle.SPLIT to "Tách rời"
                                ).forEach { (style, label) ->
                                    val isSelected = settings.displayStyle == style
                                    OutlinedCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { preferences.updateSettings { it.copy(displayStyle = style) } },
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Tự động tách khi xoay ngang
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tự động tách 2 bên khi xoay ngang", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "Giúp ngón tay cái gõ dễ dàng hơn trên tablet hoặc màn hình ngang",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.autoSplitInLandscape,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(autoSplitInLandscape = checked) }
                                    }
                                )
                            }

                            // Độ thu nhỏ bàn phím nổi
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Độ thu nhỏ của bàn phím nổi", fontWeight = FontWeight.Medium)
                                    Text("${settings.floatingScalePercent}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = settings.floatingScalePercent.toFloat(),
                                    onValueChange = { preferences.updateSettings { s -> s.copy(floatingScalePercent = it.toInt()) } },
                                    valueRange = 65f..100f,
                                    steps = 6
                                )
                            }
                        }
                    }
                }
            }
        }

        // Nhóm 5: Thanh điều hướng ảo (Bottom Chin)
        item {
            val isExpanded = expandedGroupId == "group_virtual_nav"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_virtual_nav")
            ) {
                Column {
                    AccordionHeader(
                        title = "Thanh điều hướng ảo",
                        subtitle = "Thanh tiện ích phía dưới bàn phím với phím di chuyển, dán, ẩn",
                        icon = Icons.Default.ViewSidebar,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_virtual_nav", 4) }
                    )

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Bật thanh điều hướng ảo", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Hiển thị dải công cụ tiện ích sát đáy bàn phím",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.showVirtualNavBar,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(showVirtualNavBar = checked) }
                                    }
                                )
                            }

                            if (settings.showVirtualNavBar) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Chiều cao thanh điều hướng", fontWeight = FontWeight.Medium)
                                        Text("${settings.bottomChinHeightDp} dp", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = settings.bottomChinHeightDp.toFloat(),
                                        onValueChange = { preferences.updateSettings { s -> s.copy(bottomChinHeightDp = it.toInt()) } },
                                        valueRange = 36f..64f,
                                        steps = 6
                                    )
                                }

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                Text("Các nút trên thanh điều hướng", fontWeight = FontWeight.Bold)
                                NavigationBarOptionRow(
                                    title = "Đổi ngôn ngữ",
                                    description = "Chuyển sang ngôn ngữ đã bật tiếp theo",
                                    checked = settings.navBarShowLanguageSwitch,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(navBarShowLanguageSwitch = checked) }
                                    }
                                )
                                NavigationBarOptionRow(
                                    title = "Di chuyển con trỏ",
                                    description = "Hiện nút sang trái và sang phải",
                                    checked = settings.navBarShowCursorArrows,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(navBarShowCursorArrows = checked) }
                                    }
                                )
                                NavigationBarOptionRow(
                                    title = "Đầu/cuối dòng",
                                    description = "Hiện nút nhảy đến đầu và cuối dòng",
                                    checked = settings.navBarShowLineJump,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(navBarShowLineJump = checked) }
                                    }
                                )
                                NavigationBarOptionRow(
                                    title = "Chọn tất cả",
                                    description = "Hiện nút chọn toàn bộ văn bản",
                                    checked = settings.navBarShowSelectAll,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(navBarShowSelectAll = checked) }
                                    }
                                )
                                NavigationBarOptionRow(
                                    title = "Dán",
                                    description = "Hiện nút dán nội dung từ clipboard",
                                    checked = settings.navBarShowPaste,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(navBarShowPaste = checked) }
                                    }
                                )
                                NavigationBarOptionRow(
                                    title = "Ẩn bàn phím",
                                    description = "Hiện nút thu bàn phím ở góc phải",
                                    checked = settings.navBarShowHideKeyboard,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(navBarShowHideKeyboard = checked) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationBarOptionRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun AccordionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
            tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}
