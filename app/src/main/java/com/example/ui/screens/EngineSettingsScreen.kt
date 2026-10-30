package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.model.KeyboardPreferences
import com.example.ime.model.TonePlacement
import com.example.ime.model.TypingMethod

private enum class SettingsSection {
    NAVIGATION,
    SMART_FEATURES,
    TONE_PLACEMENT,
    TYPING_METHOD
}

@Composable
fun EngineSettingsScreen(
    preferences: KeyboardPreferences,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settings.collectAsState()
    val listState = rememberLazyListState()
    var expandedSection by remember { mutableStateOf<SettingsSection?>(null) }

    LaunchedEffect(expandedSection) {
        val index = when (expandedSection) {
            SettingsSection.NAVIGATION -> 0
            SettingsSection.SMART_FEATURES -> 1
            SettingsSection.TONE_PLACEMENT -> 2
            SettingsSection.TYPING_METHOD -> 3
            null -> return@LaunchedEffect
        }
        listState.animateScrollToItem(index = index)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AccordionSection(
                title = "Thanh điều hướng ảo đáy",
                subtitle = "Tạo khoảng trống và phím điều hướng con trỏ",
                icon = Icons.Default.Navigation,
                expanded = expandedSection == SettingsSection.NAVIGATION,
                enabled = settings.showVirtualNavBar,
                onExpandedChange = {
                    expandedSection =
                        if (expandedSection == SettingsSection.NAVIGATION) null
                        else SettingsSection.NAVIGATION
                },
                onEnabledChange = { enabled ->
                    preferences.updateSettings { it.copy(showVirtualNavBar = enabled) }
                },
                testTag = "virtual_nav_card"
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text(
                    text = "Chọn các nút hiển thị trên thanh điều hướng:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                NavButtonToggleRow(
                    title = "Mũi tên di chuyển con trỏ (◀ ▶)",
                    checked = settings.navBarShowCursorArrows,
                    enabled = settings.showVirtualNavBar,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(navBarShowCursorArrows = value) }
                    }
                )
                NavButtonToggleRow(
                    title = "Về đầu dòng / cuối dòng (⏮ ⏭)",
                    checked = settings.navBarShowLineJump,
                    enabled = settings.showVirtualNavBar,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(navBarShowLineJump = value) }
                    }
                )
                NavButtonToggleRow(
                    title = "Nút Chọn tất cả (Select All)",
                    checked = settings.navBarShowSelectAll,
                    enabled = settings.showVirtualNavBar,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(navBarShowSelectAll = value) }
                    }
                )
                NavButtonToggleRow(
                    title = "Nút Dán (Paste clipboard)",
                    checked = settings.navBarShowPaste,
                    enabled = settings.showVirtualNavBar,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(navBarShowPaste = value) }
                    }
                )
                NavButtonToggleRow(
                    title = "Nút Đóng bàn phím (Hide keyboard)",
                    checked = settings.navBarShowHideKeyboard,
                    enabled = settings.showVirtualNavBar,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(navBarShowHideKeyboard = value) }
                    }
                )
            }
        }

        item {
            AccordionSection(
                title = "Gợi ý thông minh & phản hồi",
                subtitle = "Gợi ý, phản hồi và thao tác vuốt",
                icon = Icons.Default.AutoFixHigh,
                expanded = expandedSection == SettingsSection.SMART_FEATURES,
                enabled = settings.smartFeaturesEnabled,
                onExpandedChange = {
                    expandedSection =
                        if (expandedSection == SettingsSection.SMART_FEATURES) null
                        else SettingsSection.SMART_FEATURES
                },
                onEnabledChange = { enabled ->
                    preferences.updateSettings { it.copy(smartFeaturesEnabled = enabled) }
                },
                testTag = "smart_features_card"
            ) {
                FeatureToggleRow(
                    title = "Gợi ý từ thông minh",
                    subtitle = "Hiển thị từ hoàn chỉnh và dự đoán từ tiếp theo theo ngữ cảnh tiếng Việt",
                    checked = settings.showSuggestions,
                    enabled = settings.smartFeaturesEnabled,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(showSuggestions = value) }
                    }
                )
                FeatureToggleRow(
                    title = "Hàng phím số (1 2 3... 0)",
                    subtitle = "Hiển thị dãy số cố định trên hàng đầu tiên",
                    checked = settings.showNumberRow,
                    enabled = settings.smartFeaturesEnabled,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(showNumberRow = value) }
                    }
                )
                FeatureToggleRow(
                    title = "Hiện hàng phím số khi xoay ngang",
                    subtitle = "Mặc định ẩn để dành chỗ cho vùng nhập văn bản",
                    checked = settings.showNumberRowInLandscape,
                    enabled = settings.smartFeaturesEnabled,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(showNumberRowInLandscape = value) }
                    }
                )
                FeatureToggleRow(
                    title = "Tự động viết hoa đầu câu",
                    subtitle = "Viết hoa chữ cái đầu tiên sau dấu chấm, chấm hỏi, chấm than",
                    checked = settings.autoCapitalize,
                    enabled = settings.smartFeaturesEnabled,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(autoCapitalize = value) }
                    }
                )
                FeatureToggleRow(
                    title = "Rung phản hồi khi gõ (Haptic feedback)",
                    subtitle = "Rung nhẹ khi chạm vào từng phím",
                    checked = settings.hapticFeedback,
                    enabled = settings.smartFeaturesEnabled,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(hapticFeedback = value) }
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                FeatureToggleRow(
                    title = "Lướt trên phím cách để di chuyển con trỏ",
                    subtitle = "Vuốt ngang hoặc dọc để điều khiển con trỏ",
                    checked = settings.swipeSpaceToMoveCursor,
                    enabled = settings.smartFeaturesEnabled,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(swipeSpaceToMoveCursor = value) }
                    }
                )
                FeatureToggleRow(
                    title = "Giữ 0,7s và lướt để chuyển đổi ngôn ngữ",
                    subtitle = "Giữ phím cách rồi lướt sang trái/phải để đổi ngôn ngữ",
                    checked = settings.holdSpaceToSwitchLanguage,
                    enabled = settings.smartFeaturesEnabled,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(holdSpaceToSwitchLanguage = value) }
                    }
                )
                FeatureToggleRow(
                    title = "Vuốt trên phím xóa để chọn và xóa",
                    subtitle = "Vuốt tiếp để mở rộng vùng chọn; nhả tay để xóa",
                    checked = settings.swipeBackspaceToDeleteFast,
                    enabled = settings.smartFeaturesEnabled,
                    onCheckedChange = { value ->
                        preferences.updateSettings { it.copy(swipeBackspaceToDeleteFast = value) }
                    }
                )
            }
        }

        item {
            AccordionSection(
                title = "Quy tắc đặt dấu thanh",
                subtitle = "Chọn vị trí đặt dấu tiếng Việt",
                icon = Icons.Default.Spellcheck,
                expanded = expandedSection == SettingsSection.TONE_PLACEMENT,
                enabled = settings.tonePlacementEnabled,
                onExpandedChange = {
                    expandedSection =
                        if (expandedSection == SettingsSection.TONE_PLACEMENT) null
                        else SettingsSection.TONE_PLACEMENT
                },
                onEnabledChange = { enabled ->
                    preferences.updateSettings { it.copy(tonePlacementEnabled = enabled) }
                },
                testTag = "tone_placement_card"
            ) {
                TonePlacement.values().forEach { placement ->
                    val selected = settings.tonePlacement == placement
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = settings.tonePlacementEnabled) {
                                preferences.updateSettings { it.copy(tonePlacement = placement) }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = placement.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ví dụ: ${placement.example}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        RadioButton(
                            selected = selected,
                            enabled = settings.tonePlacementEnabled,
                            onClick = {
                                preferences.updateSettings { it.copy(tonePlacement = placement) }
                            }
                        )
                    }
                }
            }
        }

        item {
            val typingEnabled = settings.typingMethod != TypingMethod.OFF
            AccordionSection(
                title = "Kiểu gõ tiếng Việt",
                subtitle = "Chọn Telex, VNI hoặc tắt bộ gõ tiếng Việt",
                icon = Icons.Default.Edit,
                expanded = expandedSection == SettingsSection.TYPING_METHOD,
                enabled = typingEnabled,
                onExpandedChange = {
                    expandedSection =
                        if (expandedSection == SettingsSection.TYPING_METHOD) null
                        else SettingsSection.TYPING_METHOD
                },
                onEnabledChange = { enabled ->
                    preferences.updateSettings {
                        it.copy(typingMethod = if (enabled) TypingMethod.TELEX else TypingMethod.OFF)
                    }
                },
                testTag = "typing_method_card"
            ) {
                TypingMethod.values().forEach { method ->
                    val selected = settings.typingMethod == method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = typingEnabled) {
                                preferences.updateSettings { it.copy(typingMethod = method) }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = method.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = method.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RadioButton(
                            selected = selected,
                            enabled = typingEnabled,
                            onClick = {
                                preferences.updateSettings { it.copy(typingMethod = method) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccordionSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    expanded: Boolean,
    enabled: Boolean,
    onExpandedChange: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandedChange),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Thu gọn" else "Mở rộng",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(24.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .alpha(if (enabled) 1f else 0.5f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun NavButtonToggleRow(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Checkbox(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun FeatureToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}
