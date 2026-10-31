package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.ime.sync.BackupData
import com.example.ime.sync.GoogleDriveManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountSyncScreen(
    preferences: KeyboardPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val driveManager = remember { GoogleDriveManager(context) }

    var isSignedIn by remember { mutableStateOf(driveManager.isSignedIn()) }
    var userEmail by remember { mutableStateOf(driveManager.getUserEmail()) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var lastSyncTime by remember { mutableStateOf<String?>(null) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        val signInResult = driveManager.handleSignInResult(result.data)
        signInResult.fold(
            onSuccess = { account ->
                isSignedIn = true
                userEmail = account.email ?: account.displayName
                statusMessage = "Đăng nhập Google thành công!"
                Toast.makeText(context, "Chào mừng ${account.displayName ?: account.email}", Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                statusMessage = "Đăng nhập thất bại: ${error.localizedMessage}"
                Toast.makeText(context, "Lỗi: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card giới thiệu tính năng
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Đồng bộ Google Drive",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lưu trữ cài đặt phím và từ vựng cá nhân trực tiếp lên thư mục ẩn appDataFolder của tài khoản Google.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Trạng thái tài khoản
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tài khoản Google",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (isSignedIn) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userEmail ?: "Người dùng Google",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Đã kết nối Google Drive",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    driveManager.signOut()
                                    isSignedIn = false
                                    userEmail = null
                                    statusMessage = "Đã đăng xuất"
                                    Toast.makeText(context, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.height(36.dp),
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Đăng xuất", fontSize = 12.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Chưa kết nối tài khoản. Vui lòng đăng nhập để sao lưu và khôi phục cấu hình bàn phím của bạn bất cứ lúc nào.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            isLoading = true
                            statusMessage = "Đang kết nối Google..."
                            val intent = driveManager.getSignInIntent()
                            signInLauncher.launch(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_google_signin"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đăng nhập bằng Google", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Bảng điều khiển Sao lưu & Khôi phục (chỉ hiển thị khi đã đăng nhập)
        if (isSignedIn) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Thao tác Đồng bộ hóa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (lastSyncTime != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Lần đồng bộ gần nhất: $lastSyncTime",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Nút Sao lưu ngay
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                statusMessage = "Đang chuẩn bị và sao lưu lên Drive..."
                                try {
                                    val settingsMap = preferences.exportSettingsMap()
                                    val backup = BackupData(
                                        settingsMap = settingsMap,
                                        learnedWords = emptyList(),
                                        timestamp = System.currentTimeMillis()
                                    )
                                    val jsonString = backup.toJson()
                                    val result = driveManager.backupData(jsonString)

                                    result.fold(
                                        onSuccess = { fileId ->
                                            val formatter = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
                                            lastSyncTime = formatter.format(Date())
                                            statusMessage = "Sao lưu thành công! (ID: ${fileId.take(8)}...)"
                                            Toast.makeText(context, "Đã sao lưu lên Google Drive thành công!", Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = { error ->
                                            statusMessage = "Lỗi sao lưu: ${error.localizedMessage}"
                                            Toast.makeText(context, "Lỗi sao lưu: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                } catch (e: Exception) {
                                    statusMessage = "Lỗi: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_backup_now"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang xử lý...", fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sao lưu ngay lên Drive", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Nút Khôi phục dữ liệu
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                statusMessage = "Đang tải dữ liệu từ Google Drive..."
                                try {
                                    val result = driveManager.restoreData()
                                    result.fold(
                                        onSuccess = { jsonContent ->
                                            if (jsonContent.isNullOrBlank()) {
                                                statusMessage = "Chưa tìm thấy bản sao lưu nào trong appDataFolder."
                                                Toast.makeText(context, "Không có bản sao lưu nào trên Drive", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val backup = BackupData.fromJson(jsonContent)
                                                if (backup != null && backup.settingsMap.isNotEmpty()) {
                                                    preferences.importSettingsMap(backup.settingsMap)
                                                    val formatter = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
                                                    lastSyncTime = formatter.format(Date(backup.timestamp))
                                                    statusMessage = "Đã khôi phục thành công cấu hình bàn phím!"
                                                    Toast.makeText(context, "Khôi phục dữ liệu thành công!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    statusMessage = "File sao lưu không hợp lệ."
                                                    Toast.makeText(context, "Dữ liệu sao lưu không tương thích", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        onFailure = { error ->
                                            statusMessage = "Lỗi khôi phục: ${error.localizedMessage}"
                                            Toast.makeText(context, "Lỗi khôi phục: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                } catch (e: Exception) {
                                    statusMessage = "Lỗi: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_restore_now"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Khôi phục dữ liệu từ Drive", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Thông báo trạng thái nếu có
        AnimatedVisibility(visible = statusMessage != null) {
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = statusMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Chính sách bảo mật appDataFolder
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Bảo mật & Quyền riêng tư",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Text(
                        text = "Ứng dụng sử dụng scope 'drive.appdata' lưu trữ trong thư mục ẩn của Google Drive. Tập tin này hoàn toàn vô hình với các ứng dụng khác và không can thiệp vào tài liệu riêng tư của bạn.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
