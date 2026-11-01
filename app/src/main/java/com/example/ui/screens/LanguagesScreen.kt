package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.model.KeyboardPreferences
import com.example.ime.model.LanguagePack
import com.example.ime.model.WorldLanguagesRepository
import kotlinx.coroutines.launch

@Composable
fun LanguagesScreen(
    preferences: KeyboardPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by preferences.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var expandedGroupId by remember { mutableStateOf<String?>("group_active_lang") }

    fun toggleGroup(groupId: String, index: Int) {
        val willOpen = expandedGroupId != groupId
        expandedGroupId = if (willOpen) groupId else null
        if (willOpen) {
            scope.launch {
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
        // Nhóm 1: Ngôn ngữ đang hoạt động
        item {
            val isExpanded = expandedGroupId == "group_active_lang"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_active_lang")
            ) {
                Column {
                    LanguageAccordionHeader(
                        title = "Ngôn ngữ đang hoạt động",
                        subtitle = "Chọn ngôn ngữ và phương thức gõ chính",
                        icon = Icons.Default.CheckCircle,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_active_lang", 0) }
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

                            Text("Chọn ngôn ngữ đang gõ:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                            settings.enabledLanguages
                                .filter { WorldLanguagesRepository.isSupported(it) }
                                .forEach { langId ->
                                val pack = WorldLanguagesRepository.getLanguage(langId)
                                if (pack != null) {
                                    val isCurrent = settings.activeLanguageId == langId
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            )
                                            .border(
                                                width = if (isCurrent) 2.dp else 1.dp,
                                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                preferences.updateSettings {
                                                    it.copy(
                                                        activeLanguageId = langId,
                                                        typingMethod = WorldLanguagesRepository.typingMethodFor(langId)
                                                    )
                                                }
                                                Toast.makeText(context, "Đã chọn ${pack.name}", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(pack.flag, fontSize = 24.sp)
                                                Column {
                                                    Text(pack.name, fontWeight = FontWeight.Bold)
                                                    Text("${pack.nativeName} • ${pack.inputMethod}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            if (isCurrent) {
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
                            }
                        }
                    }
                }
            }
        }

        // Nhóm 2: Ngôn ngữ đã kích hoạt
        item {
            val isExpanded = expandedGroupId == "group_installed_lang"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_installed_lang")
            ) {
                Column {
                    LanguageAccordionHeader(
                        title = "Ngôn ngữ đã kích hoạt",
                        subtitle = "Bật/tắt bộ gõ tích hợp trong danh sách đổi bằng phím cách",
                        icon = Icons.Default.Language,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_installed_lang", 1) }
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

                            Text("Các bộ gõ có sẵn trong ứng dụng:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                            settings.downloadedLanguages
                                .filter { WorldLanguagesRepository.isSupported(it) }
                                .forEach { langId ->
                                val pack = WorldLanguagesRepository.getLanguage(langId)
                                if (pack != null) {
                                    val isEnabled = settings.enabledLanguages.contains(langId)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(pack.flag, fontSize = 22.sp)
                                            Column {
                                                Text(pack.name, fontWeight = FontWeight.SemiBold)
                                                Text(pack.inputMethod, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Switch(
                                                checked = isEnabled,
                                                onCheckedChange = { checked ->
                                                    val newSet = settings.enabledLanguages.toMutableSet()
                                                    var fallbackLanguageId: String? = null
                                                    if (checked) {
                                                        newSet.add(langId)
                                                    } else {
                                                        if (newSet.size > 1) {
                                                            newSet.remove(langId)
                                                            if (settings.activeLanguageId == langId) {
                                                                fallbackLanguageId = newSet.first()
                                                            }
                                                        } else {
                                                            Toast.makeText(context, "Phải giữ ít nhất 1 ngôn ngữ", Toast.LENGTH_SHORT).show()
                                                            return@Switch
                                                        }
                                                    }
                                                    preferences.updateSettings { current ->
                                                        current.copy(
                                                            enabledLanguages = newSet,
                                                            activeLanguageId = fallbackLanguageId
                                                                ?: current.activeLanguageId,
                                                            typingMethod = fallbackLanguageId?.let { languageId ->
                                                                WorldLanguagesRepository.typingMethodFor(languageId)
                                                            } ?: current.typingMethod
                                                        )
                                                    }
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
        }

        // Nhóm 3: Kho ngôn ngữ thế giới
        item {
            val isExpanded = expandedGroupId == "group_world_store"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accordion_group_world_store")
            ) {
                Column {
                    LanguageAccordionHeader(
                        title = "Kho ngôn ngữ thế giới",
                        subtitle = "Các bộ gõ tích hợp sẵn và những ngôn ngữ chưa được hỗ trợ",
                        icon = Icons.Default.Public,
                        isExpanded = isExpanded,
                        onClick = { toggleGroup("group_world_store", 2) }
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            WorldLanguagesRepository.allLanguages.forEach { pack ->
                                val isSupported = WorldLanguagesRepository.isSupported(pack.id)
                                val isDownloaded = settings.downloadedLanguages.contains(pack.id)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(pack.flag, fontSize = 22.sp)
                                        Column {
                                            Text(pack.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${pack.nativeName} • ${pack.inputMethod}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (!isSupported) {
                                        Text(
                                            "Chưa hỗ trợ",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else if (isDownloaded) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "Có sẵn",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        FilledTonalButton(
                                            onClick = {
                                                preferences.updateSettings {
                                                    it.copy(
                                                        downloadedLanguages = it.downloadedLanguages + pack.id,
                                                        enabledLanguages = it.enabledLanguages + pack.id
                                                    )
                                                }
                                                Toast.makeText(context, "Đã bật ${pack.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Bật", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageAccordionHeader(
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
