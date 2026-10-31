package com.example.ime.sync

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Quản lý xác thực Google và thao tác sao lưu/khôi phục dữ liệu lên thư mục ẩn appDataFolder của Google Drive.
 * Tuyệt đối không dùng Firebase; giao tiếp trực tiếp qua Google Drive API v3.
 */
class GoogleDriveManager(private val context: Context) {

    private val applicationContext = context.applicationContext

    private var driveService: Drive? = null
    var currentAccount: GoogleSignInAccount? = null
        private set

    init {
        // Kiểm tra tài khoản đã đăng nhập trước đó
        val lastAccount = GoogleSignIn.getLastSignedInAccount(applicationContext)
        if (lastAccount != null && GoogleSignIn.hasPermissions(lastAccount, Scope(DriveScopes.DRIVE_APPDATA))) {
            currentAccount = lastAccount
            initDriveService(lastAccount)
        }
    }

    private fun initDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            applicationContext,
            listOf(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccount = account.account
        }

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Anykey26 Keyboard").build()
    }

    fun isSignedIn(): Boolean = currentAccount != null && driveService != null

    fun getUserEmail(): String? = currentAccount?.email ?: currentAccount?.displayName

    /**
     * Tạo GoogleSignInClient với scope DRIVE_APPDATA
     */
    fun getSignInIntent(serverClientId: String? = null): Intent {
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))

        if (!serverClientId.isNullOrBlank()) {
            gsoBuilder.requestIdToken(serverClientId)
        }

        val gso = gsoBuilder.build()
        val client: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    /**
     * Xử lý kết quả Intent đăng nhập từ GoogleSignIn
     */
    fun handleSignInResult(intent: Intent?): Result<GoogleSignInAccount> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(Exception::class.java)
            if (account != null) {
                currentAccount = account
                initDriveService(account)
                Result.success(account)
            } else {
                Result.failure(Exception("Không nhận được thông tin tài khoản Google"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi đăng nhập Google: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Đăng nhập sử dụng Credential Manager
     */
    suspend fun signInWithCredentialManager(
        activity: Activity,
        serverClientId: String? = null
    ): Result<GoogleSignInAccount> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(activity)
            val request = GetCredentialRequest.Builder().build()
            val response: GetCredentialResponse = credentialManager.getCredential(activity, request)
            Log.d(TAG, "Credential received: ${response.credential.type}")

            // Đồng bộ qua GoogleSignIn client
            val account = GoogleSignIn.getLastSignedInAccount(activity)
            if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_APPDATA))) {
                currentAccount = account
                initDriveService(account)
                Result.success(account)
            } else {
                Result.failure(Exception("Cần cấp quyền truy cập Drive appDataFolder"))
            }
        } catch (e: GetCredentialException) {
            Log.w(TAG, "CredentialManager error: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi xác thực: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Đăng xuất khỏi Google và hủy liên kết dịch vụ Drive
     */
    suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val client = GoogleSignIn.getClient(applicationContext, gso)
        client.signOut()
        currentAccount = null
        driveService = null
    }

    /**
     * Sao lưu dữ liệu JSON vào thư mục ẩn appDataFolder trên Google Drive.
     * Nếu file đã tồn tại -> Update (ghi đè).
     * Nếu chưa có -> Create mới.
     */
    suspend fun backupData(
        jsonString: String,
        fileName: String = DEFAULT_BACKUP_FILE_NAME
    ): Result<String> = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext Result.failure(
            IllegalStateException("Chưa đăng nhập Google Drive")
        )

        try {
            // 1. Tìm file đã có trong appDataFolder
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$fileName' and trashed = false")
                .setFields("files(id, name, modifiedTime)")
                .execute()

            val existingFile = fileList.files?.firstOrNull()
            val mediaContent = ByteArrayContent("application/json", jsonString.toByteArray(Charsets.UTF_8))

            val savedFileId = if (existingFile != null) {
                // Ghi đè file cũ
                Log.d(TAG, "Cập nhật file sao lưu hiện có ID: ${existingFile.id}")
                val updated = service.files().update(existingFile.id, null, mediaContent)
                    .setFields("id, name, modifiedTime")
                    .execute()
                updated.id
            } else {
                // Tạo mới file trong appDataFolder
                Log.d(TAG, "Tạo file sao lưu mới trong appDataFolder: $fileName")
                val metadata = File().apply {
                    name = fileName
                    parents = listOf("appDataFolder")
                }
                val created = service.files().create(metadata, mediaContent)
                    .setFields("id, name, modifiedTime")
                    .execute()
                created.id
            }

            Result.success(savedFileId)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi sao lưu lên Drive: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Tải nội dung file sao lưu từ thư mục ẩn appDataFolder của Drive về dưới dạng chuỗi JSON.
     */
    suspend fun restoreData(
        fileName: String = DEFAULT_BACKUP_FILE_NAME
    ): Result<String?> = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext Result.failure(
            IllegalStateException("Chưa đăng nhập Google Drive")
        )

        try {
            // Tìm file trong appDataFolder
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$fileName' and trashed = false")
                .setFields("files(id, name, modifiedTime, size)")
                .execute()

            val existingFile = fileList.files?.firstOrNull()
            if (existingFile == null) {
                return@withContext Result.success(null) // Chưa có bản sao lưu nào
            }

            // Tải nội dung file
            val outputStream = ByteArrayOutputStream()
            service.files().get(existingFile.id)
                .executeMediaAndDownloadTo(outputStream)

            val jsonContent = outputStream.toString("UTF-8")
            Result.success(jsonContent)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khôi phục từ Drive: ${e.message}", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "GoogleDriveManager"
        const val DEFAULT_BACKUP_FILE_NAME = "anykey26_backup.json"
        const val RC_SIGN_IN = 9001
    }
}
