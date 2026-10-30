package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Vibration
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.model.KeyboardPreferences
import com.example.ime.model.TonePlacement
import com.example.ime.model.TypingMethod

@Composable
fun EngineSettingsScreen(
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
        // 1. Typing Methods (Telex vs VNI)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("typing_method_card")
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
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Kiểu gõ tiếng Việt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                TypingMethod.values().forEach { method ->
                    val isSelected = settings.typingMethod == method
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                preferences.updateSettings { it.copy(typingMethod = method) }
                            }
                            .padding(12.dp)
                            .testTag("method_${method.name}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                selected = isSelected,
                                onClick = {
                                    preferences.updateSettings { it.copy(typingMethod = method) }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 2. Tone Placement Rules (Modern vs Classic)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("tone_placement_card")
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
                        imageVector = Icons.Default.Spellcheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Quy tắc đặt dấu thanh",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                TonePlacement.values().forEach { placement ->
                    val isSelected = settings.tonePlacement == placement
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                preferences.updateSettings { it.copy(tonePlacement = placement) }
                            }
                            .padding(12.dp)
                            .testTag("tone_${placement.name}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                selected = isSelected,
                                onClick = {
                                    preferences.updateSettings { it.copy(tonePlacement = placement) }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. Virtual Navigation Bar (Thanh điều hướng ảo dưới đáy bàn phím)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("virtual_nav_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Thanh điều hướng ảo đáy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tạo khoảng trống và phím điều hướng con trỏ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = settings.showVirtualNavBar,
                        onCheckedChange = { checked ->
                            preferences.updateSettings { it.copy(showVirtualNavBar = checked) }
                        },
                        modifier = Modifier.testTag("switch_show_virtual_nav")
                    )
                }

                if (settings.showVirtualNavBar) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "Chọn các nút hiển thị trên thanh điều hướng:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )

                    NavButtonToggleRow(
                        title = "Mũi tên di chuyển con trỏ (◀ ▶)",
                        checked = settings.navBarShowCursorArrows,
                        onCheckedChange = { c -> preferences.updateSettings { it.copy(navBarShowCursorArrows = c) } }
                    )

                    NavButtonToggleRow(
                        title = "Về đầu dòng / cuối dòng (⏮ ⏭)",
                        checked = settings.navBarShowLineJump,
                        onCheckedChange = { c -> preferences.updateSettings { it.copy(navBarShowLineJump = c) } }
                    )

                    NavButtonToggleRow(
                        title = "Nút Chọn tất cả (Select All)",
                        checked = settings.navBarShowSelectAll,
                        onCheckedChange = { c -> preferences.updateSettings { it.copy(navBarShowSelectAll = c) } }
                    )

                    NavButtonToggleRow(
                        title = "Nút Dán (Paste clipboard)",
                        checked = settings.navBarShowPaste,
                        onCheckedChange = { c -> preferences.updateSettings { it.copy(navBarShowPaste = c) } }
                    )

                    NavButtonToggleRow(
                        title = "Nút Đóng bàn phím (Hide keyboard)",
                        checked = settings.navBarShowHideKeyboard,
                        onCheckedChange = { c -> preferences.updateSettings { it.copy(navBarShowHideKeyboard = c) } }
                    )
                }
            }
        }

        // 4. Smart Suggestions & Typing Features
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("smart_features_card")
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
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Gợi ý thông minh & Phản hồi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Show suggestions
                FeatureToggleRow(
                    title = "Gợi ý từ thông minh",
                    subtitle = "Hiển thị từ hoàn chỉnh và dự đoán từ tiếp theo theo ngữ cảnh tiếng Việt",
                    checked = settings.showSuggestions,
                    onCheckedChange = { c -> preferences.updateSettings { it.copy(showSuggestions = c) } }
                )

                // Dedicated Number Row
                FeatureToggleRow(
                    title = "Hàng phím số (1 2 3... 0)",
                    subtitle = "Hiển thị dãy số cố định trên hàng đầu tiên",
                    checked = settings.showNumberRow,
                    onCheckedChange = { c -> preferences.updateSettings { it.copy(showNumberRow = c) } }
                )

                // Auto capitalize
                FeatureToggleRow(
                    title = "Tự động viết hoa đầu câu",
                    subtitle = "Viết hoa chữ cái đầu tiên sau dấu chấm, chấm hỏi, chấm than",
                    checked = settings.autoCapitalize,
                    onCheckedChange = { c -> preferences.updateSettings { it.copy(autoCapitalize = c) } }
                )

                // Haptic feedback
                FeatureToggleRow(
                    title = "Rung phản hồi khi gõ (Haptic feedback)",
                    subtitle = "Rung nhẹ khi chạm vào từng phím",
                    checked = settings.hapticFeedback,
                    onCheckedChange = { c -> preferences.updateSettings { it.copy(hapticFeedback = c) } }
                )
            }
        }

        // 5. Swipe Gestures on Spacebar and Backspace
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("gesture_features_card")
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
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Cử chỉ vuốt phím thông minh",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 1. Swipe space to move cursor
                FeatureToggleRow(
                    title = "Lướt trên phím cách để di chuyển con trỏ",
                    subtitle = "Vuốt theo hai chiều để điều khiển con trỏ; biểu tượng con trỏ xuất hiện khi đang vuốt",
                    checked = settings.swipeSpaceToMoveCursor,
                    onCheckedChange = { c -> preferences.updateSettings { it.copy(swipeSpaceToMoveCursor = c) } }
                )

                // 2. Hold 0.7s and swipe to switch language
                FeatureToggleRow(
                    title = "Giữ 0,7s và lướt để chuyển đổi ngôn ngữ",
                    subtitle = "Nhấn giữ phím cách 700ms rồi lướt sang trái/phải để đổi ngôn ngữ. Nếu đã lướt phím cách để di chuyển con trỏ trước thời hạn 700ms thì sẽ không kích hoạt lướt đổi ngôn ngữ.",
                    checked = settings.holdSpaceToSwitchLanguage,
                    onCheckedChange = { c -> preferences.updateSettings { it.copy(holdSpaceToSwitchLanguage = c) } }
                )

                // 3. Swipe from backspace to select text for deletion
                FeatureToggleRow(
                    title = "Vuốt phải trên phím xóa để chọn và xóa",
                    subtitle = "Vuốt tiếp để mở rộng vùng chọn; nhả tay để xóa",
                    checked = settings.swipeBackspaceToDeleteFast,
                    onCheckedChange = { c -> preferences.updateSettings { it.copy(swipeBackspaceToDeleteFast = c) } }
                )
            }
        }
    }
}

@Composable
private fun NavButtonToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FeatureToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
