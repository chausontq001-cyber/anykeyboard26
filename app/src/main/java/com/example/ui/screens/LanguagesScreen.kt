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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.ime.model.TypingMethod
import com.example.ime.model.WorldLanguagesRepository
import kotlinx.coroutines.delay
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

    // Download progress map
    val downloadingProgress = remember { mutableStateMapOf<String, Float>() }

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

                            settings.enabledLanguages.forEach { langId ->
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
                                                    val method = when (langId) {
                                                        "vi_telex" -> TypingMethod.TELEX
                                                        "vi_vni" -> TypingMethod.VNI
                                                        else -> TypingMethod.OFF
                                                    }
                                                    it.copy(activeLanguageId = langId, typingMethod = method)
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
                        subtitle = "Bật/tắt ngôn ngữ trong danh sách đổi nhanh bằng phím cách",
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

                            Text("Các gói đã tải về máy:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                            settings.downloadedLanguages.forEach { langId ->
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
                                                    if (checked) {
                                                        newSet.add(langId)
                                                    } else {
                                                        if (newSet.size > 1) {
                                                            newSet.remove(langId)
                                                            if (settings.activeLanguageId == langId) {
                                                                val fallback = newSet.first()
                                                                preferences.updateSettings { it.copy(activeLanguageId = fallback) }
                                                            }
                                                        } else {
                                                            Toast.makeText(context, "Phải giữ ít nhất 1 ngôn ngữ", Toast.LENGTH_SHORT).show()
                                                            return@Switch
                                                        }
                                                    }
                                                    preferences.updateSettings { it.copy(enabledLanguages = newSet) }
                                                }
                                            )

                                            if (langId != "vi_telex" && langId != "en_us") {
                                                IconButton(
                                                    onClick = {
                                                        val newDownloaded = settings.downloadedLanguages.toMutableSet().apply { remove(langId) }
                                                        val newEnabled = settings.enabledLanguages.toMutableSet().apply { remove(langId) }
                                                        preferences.updateSettings {
                                                            it.copy(
                                                                downloadedLanguages = newDownloaded,
                                                                enabledLanguages = if (newEnabled.isEmpty()) setOf("vi_telex") else newEnabled,
                                                                activeLanguageId = if (it.activeLanguageId == langId) "vi_telex" else it.activeLanguageId
                                                            )
                                                        }
                                                        Toast.makeText(context, "Đã xóa ${pack.name}", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Xóa",
                                                        tint = MaterialTheme.colorScheme.error,
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
                        subtitle = "Tải xuống các gói ngôn ngữ và phương thức nhập toàn cầu",
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
                                val isDownloaded = settings.downloadedLanguages.contains(pack.id)
                                val progress = downloadingProgress[pack.id]

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
                                            Text("${pack.nativeName} • ${pack.sizeMb}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    if (progress != null) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            CircularProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Text("${(progress * 100).toInt()}%", fontSize = 10.sp)
                                        }
                                    } else if (isDownloaded) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Đã tải", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        FilledTonalButton(
                                            onClick = {
                                                scope.launch {
                                                    downloadingProgress[pack.id] = 0.1f
                                                    delay(250)
                                                    downloadingProgress[pack.id] = 0.5f
                                                    delay(250)
                                                    downloadingProgress[pack.id] = 0.9f
                                                    delay(200)
                                                    downloadingProgress.remove(pack.id)
                                                    val newDownloaded = settings.downloadedLanguages + pack.id
                                                    val newEnabled = settings.enabledLanguages + pack.id
                                                    preferences.updateSettings {
                                                        it.copy(
                                                            downloadedLanguages = newDownloaded,
                                                            enabledLanguages = newEnabled
                                                        )
                                                    }
                                                    Toast.makeText(context, "Đã tải xong ${pack.name}", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Tải", fontSize = 12.sp)
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
