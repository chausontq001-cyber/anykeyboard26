package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    // Download progress map for simulated real-time downloads
    val downloadingProgress = remember { mutableStateMapOf<String, Float>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("languages_banner_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Ngôn ngữ & Phương thức nhập",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Tải xuống các gói ngôn ngữ và kiểu gõ trên toàn thế giới. Bật các ngôn ngữ bạn muốn sử dụng để chuyển đổi nhanh trên phím cách.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(WorldLanguagesRepository.allLanguages) { lang ->
                val isDownloaded = settings.downloadedLanguages.contains(lang.id)
                val isEnabled = settings.enabledLanguages.contains(lang.id)
                val isCurrentActive = settings.activeLanguageId == lang.id
                val progress = downloadingProgress[lang.id]

                LanguagePackItem(
                    lang = lang,
                    isDownloaded = isDownloaded,
                    isEnabled = isEnabled,
                    isCurrentActive = isCurrentActive,
                    downloadProgress = progress,
                    onDownload = {
                        scope.launch {
                            downloadingProgress[lang.id] = 0.1f
                            delay(350)
                            downloadingProgress[lang.id] = 0.5f
                            delay(400)
                            downloadingProgress[lang.id] = 0.85f
                            delay(300)
                            downloadingProgress.remove(lang.id)

                            val newDownloaded = settings.downloadedLanguages + lang.id
                            val newEnabled = settings.enabledLanguages + lang.id
                            preferences.updateSettings {
                                it.copy(
                                    downloadedLanguages = newDownloaded,
                                    enabledLanguages = newEnabled
                                )
                            }
                            Toast.makeText(context, "Đã tải xong gói ngôn ngữ ${lang.name}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onToggleEnabled = { enabled ->
                        val newEnabled = if (enabled) {
                            settings.enabledLanguages + lang.id
                        } else {
                            if (settings.enabledLanguages.size > 1) {
                                settings.enabledLanguages - lang.id
                            } else {
                                Toast.makeText(context, "Phải giữ ít nhất 1 ngôn ngữ hoạt động", Toast.LENGTH_SHORT).show()
                                settings.enabledLanguages
                            }
                        }
                        preferences.updateSettings { it.copy(enabledLanguages = newEnabled) }
                    },
                    onSelectAsActive = {
                        preferences.updateSettings {
                            val typingMethod = when (lang.id) {
                                "vi_telex" -> TypingMethod.TELEX
                                "vi_vni" -> TypingMethod.VNI
                                else -> TypingMethod.OFF
                            }
                            it.copy(
                                activeLanguageId = lang.id,
                                typingMethod = typingMethod
                            )
                        }
                        Toast.makeText(context, "Đã chọn ${lang.name}", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = {
                        if (lang.id != "vi_telex" && lang.id != "vi_vni") {
                            val newDownloaded = settings.downloadedLanguages - lang.id
                            val newEnabled = settings.enabledLanguages - lang.id
                            val newActive = if (settings.activeLanguageId == lang.id) "vi_telex" else settings.activeLanguageId
                            preferences.updateSettings {
                                it.copy(
                                    downloadedLanguages = newDownloaded,
                                    enabledLanguages = newEnabled,
                                    activeLanguageId = newActive
                                )
                            }
                            Toast.makeText(context, "Đã gỡ gói ngôn ngữ ${lang.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun LanguagePackItem(
    lang: LanguagePack,
    isDownloaded: Boolean,
    isEnabled: Boolean,
    isCurrentActive: Boolean,
    downloadProgress: Float?,
    onDownload: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectAsActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrentActive) 1.5.dp else 1.dp,
                color = if (isCurrentActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("lang_card_${lang.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = lang.flag, fontSize = 28.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = lang.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCurrentActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Đang dùng",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${lang.nativeName} • ${lang.inputMethod} • ${lang.sizeMb}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isDownloaded) {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggleEnabled,
                        modifier = Modifier.testTag("switch_lang_${lang.id}")
                    )
                } else if (downloadProgress != null) {
                    CircularProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    OutlinedButton(
                        onClick = onDownload,
                        modifier = Modifier.testTag("btn_download_${lang.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tải về", fontSize = 12.sp)
                    }
                }
            }

            if (downloadProgress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isDownloaded && isEnabled && !isCurrentActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onSelectAsActive,
                        modifier = Modifier.height(32.dp).testTag("btn_activate_${lang.id}")
                    ) {
                        Text("Dùng ngôn ngữ này", fontSize = 11.sp)
                    }

                    if (lang.id != "vi_telex" && lang.id != "vi_vni") {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Gỡ cài đặt",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
