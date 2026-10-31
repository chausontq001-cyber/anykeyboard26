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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.model.KeyboardPreferences
import com.example.ime.model.TonePlacement
import com.example.ime.model.TypingMethod
import kotlinx.coroutines.launch

@Composable
fun EngineSettingsScreen(
    preferences: KeyboardPreferences,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settings.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var expandedGroupId by remember { mutableStateOf<String?>("group_telex_vni") }

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
        // Nhóm 1: Kiểu gõ tiếng Việt (Telex / VNI)
        item {
            val isExpanded = expandedGroupId == "group_telex_vni"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_telex_vni")
            ) {
                Column {
                    EngineAccordionHeader(
                        title = "Kiểu gõ tiếng Việt",
                        subtitle = "Telex, VNI hoặc Tắt gõ dấu; Quy tắc đặt dấu thanh",
                        icon = Icons.Default.Spellcheck,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_telex_vni", 0) }
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

                            Text("Phương thức nhập tiếng Việt:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                            // 1. Telex
                            SelectableMethodOption(
                                title = "Telex (Khuyên dùng)",
                                description = "Gõ aa->â, aw->ă, ee->ê, oo->ô, ow->ơ, uw->ư, dd->đ; s/f/r/x/j cho dấu sắc/huyền/hỏi/ngã/nặng.",
                                isSelected = settings.typingMethod == TypingMethod.TELEX,
                                onSelect = { preferences.updateSettings { it.copy(typingMethod = TypingMethod.TELEX) } }
                            )

                            // 2. VNI
                            SelectableMethodOption(
                                title = "VNI (Phím số)",
                                description = "Dùng phím số 1-9 để bỏ dấu: 1-sắc, 2-huyền, 3-hỏi, 4-ngã, 5-nặng, 6-mũ ^, 7-móc ơ/ư, 8-trăng ă, 9-gạch đ.",
                                isSelected = settings.typingMethod == TypingMethod.VNI,
                                onSelect = { preferences.updateSettings { it.copy(typingMethod = TypingMethod.VNI) } }
                            )

                            // 3. Tắt gõ dấu
                            SelectableMethodOption(
                                title = "Tắt gõ dấu (Chỉ gõ tiếng Anh thuần)",
                                description = "Bỏ qua quy tắc ghép từ tiếng Việt, gõ nguyên bản từng ký tự.",
                                isSelected = settings.typingMethod == TypingMethod.OFF,
                                onSelect = { preferences.updateSettings { it.copy(typingMethod = TypingMethod.OFF) } }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Quy tắc đặt dấu thanh
                            Text("Quy tắc đặt dấu thanh:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        preferences.updateSettings { it.copy(tonePlacement = TonePlacement.MODERN) }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.tonePlacement == TonePlacement.MODERN,
                                    onClick = { preferences.updateSettings { it.copy(tonePlacement = TonePlacement.MODERN) } }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Kiểu mới (Chuẩn mới): hòa, thúy, khỏe", fontWeight = FontWeight.Medium)
                                    Text("Đặt dấu thanh trên nguyên âm chính âm vị học", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        preferences.updateSettings { it.copy(tonePlacement = TonePlacement.CLASSIC) }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.tonePlacement == TonePlacement.CLASSIC,
                                    onClick = { preferences.updateSettings { it.copy(tonePlacement = TonePlacement.CLASSIC) } }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Kiểu cũ (Truyền thống): hoà, thuý, khoẻ", fontWeight = FontWeight.Medium)
                                    Text("Đặt dấu thanh theo thói quen chính tả cổ truyền", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Nhóm 2: Gợi ý từ & Tính năng thông minh
        item {
            val isExpanded = expandedGroupId == "group_suggestions"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_suggestions")
            ) {
                Column {
                    EngineAccordionHeader(
                        title = "Gợi ý từ & Tính năng thông minh",
                        subtitle = "Thanh gợi ý từ thông minh, tự động viết hoa",
                        icon = Icons.Default.Lightbulb,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_suggestions", 1) }
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
                                    Text("Hiển thị thanh gợi ý từ", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Đoán từ tiếp theo và từ đang gõ phía trên bàn phím",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.showSuggestions,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(showSuggestions = checked) }
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tự động viết hoa đầu câu", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Tự động bật Shift sau dấu chấm (.), hỏi (?), than (!)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.autoCapitalize,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(autoCapitalize = checked) }
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tính năng thông minh tổng thể", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Kích hoạt xử lý ngôn ngữ tự nhiên và từ điển thông minh",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.smartFeaturesEnabled,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(smartFeaturesEnabled = checked) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Nhóm 3: Phản hồi rung & Âm thanh
        item {
            val isExpanded = expandedGroupId == "group_feedback"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_feedback")
            ) {
                Column {
                    EngineAccordionHeader(
                        title = "Phản hồi rung & Âm thanh",
                        subtitle = "Rung xúc giác khi gõ và âm thanh phím",
                        icon = Icons.Default.Vibration,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_feedback", 2) }
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
                                    Text("Rung khi nhấn phím (Haptic)", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Phản hồi xúc giác nhanh và êm ái khi chạm vào các phím",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.hapticFeedback,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(hapticFeedback = checked) }
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Âm thanh khi gõ phím", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Phát tiếng click nhẹ khi gõ từng ký tự",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.soundFeedback,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(soundFeedback = checked) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Nhóm 4: Cử chỉ trên phím cách (Space)
        item {
            val isExpanded = expandedGroupId == "group_space_gestures"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_space_gestures")
            ) {
                Column {
                    EngineAccordionHeader(
                        title = "Cử chỉ trên phím cách (Space)",
                        subtitle = "Lướt di chuyển con trỏ & giữ 700ms lướt đổi ngôn ngữ",
                        icon = Icons.Default.SpaceBar,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_space_gestures", 3) }
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
                                    Text("Lướt phím cách để di chuyển con trỏ", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Biến phím cách thành bàn rê chuột touchpad điều hướng văn bản",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.swipeSpaceToMoveCursor,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(swipeSpaceToMoveCursor = checked) }
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Giữ 700ms và lướt để chuyển ngôn ngữ", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Bấm giữ phím cách 700ms và lướt để xoay vòng qua các ngôn ngữ đã cài",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.holdSpaceToSwitchLanguage,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(holdSpaceToSwitchLanguage = checked) }
                                    }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "💡 Lưu ý: Bấm đơn 1 lần trên phím cách tạo khoảng cách bình thường. Nếu bạn đã lướt phím cách để di chuyển con trỏ trước 700ms, bàn phím sẽ khóa thao tác đổi ngôn ngữ để tránh vô tình đổi nhầm.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Nhóm 5: Cử chỉ trên phím xóa (Backspace)
        item {
            val isExpanded = expandedGroupId == "group_backspace_gestures"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_backspace_gestures")
            ) {
                Column {
                    EngineAccordionHeader(
                        title = "Cử chỉ trên phím xóa (Backspace)",
                        subtitle = "Bấm xóa 1 ký tự, giữ xóa liên tục, lướt xóa nhanh hàng loạt",
                        icon = Icons.AutoMirrored.Filled.Backspace,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_backspace_gestures", 4) }
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
                                    Text("Lướt phím xóa để xóa nhanh hàng loạt", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Kéo trượt sang trái hoặc phải từ phím xóa để chọn và xóa nhanh",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.swipeBackspaceToDeleteFast,
                                    onCheckedChange = { checked ->
                                        preferences.updateSettings { it.copy(swipeBackspaceToDeleteFast = checked) }
                                    }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "💡 Thao tác đa dạng: Bấm 1 lần xóa 1 ký tự. Nhấn giữ để xóa lặp lại liên tục. Lướt kéo ngang để bôi đen và xóa nhanh hàng loạt từ trong nháy mắt.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    lineHeight = 18.sp
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
private fun SelectableMethodOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EngineAccordionHeader(
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
