/*
 * OmniAPK - Alternatif İndirme ViewModel
 * APKMirror ve APKPure'dan uygulama içi indirme işlemleri
 */

package com.aurora.store.viewmodel.alternative

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.store.data.providers.APKMirrorProvider
import com.aurora.store.data.providers.APKPureProvider
import com.aurora.store.data.providers.AppVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class AlternativeDownloadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apkMirrorProvider: APKMirrorProvider,
    private val apkPureProvider: APKPureProvider
) : ViewModel() {

    companion object {
        private const val TAG = "AltDownloadVM"
    }

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    private val _state = MutableStateFlow<AlternativeDownloadState>(AlternativeDownloadState.Idle)
    val state: StateFlow<AlternativeDownloadState> = _state.asStateFlow()

    private val _versions = MutableStateFlow<List<AppVersion>>(emptyList())
    val versions: StateFlow<List<AppVersion>> = _versions.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    /**
     * APKMirror'dan uygulama versiyonlarını yükle
     */
    fun loadAPKMirrorVersions(packageName: String) {
        viewModelScope.launch {
            _state.value = AlternativeDownloadState.Loading
            try {
                val versionList = apkMirrorProvider.getAppVersions(packageName)
                _versions.value = versionList
                _state.value = if (versionList.isEmpty()) {
                    AlternativeDownloadState.Error("Versiyon bulunamadı")
                } else {
                    AlternativeDownloadState.VersionsLoaded("APKMirror")
                }
            } catch (e: Exception) {
                Log.e(TAG, "APKMirror yükleme hatası", e)
                _state.value = AlternativeDownloadState.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }

    /**
     * APKPure'dan uygulama versiyonlarını yükle
     */
    fun loadAPKPureVersions(packageName: String) {
        viewModelScope.launch {
            _state.value = AlternativeDownloadState.Loading
            try {
                val versionList = apkPureProvider.getAppVersions(packageName)
                _versions.value = versionList
                _state.value = if (versionList.isEmpty()) {
                    AlternativeDownloadState.Error("Versiyon bulunamadı")
                } else {
                    AlternativeDownloadState.VersionsLoaded("APKPure")
                }
            } catch (e: Exception) {
                Log.e(TAG, "APKPure yükleme hatası", e)
                _state.value = AlternativeDownloadState.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }

    /**
     * Seçilen versiyonu indir
     */
    fun downloadVersion(version: AppVersion) {
        viewModelScope.launch {
            _state.value = AlternativeDownloadState.Downloading
            _downloadProgress.value = 0

            try {
                val downloadUrl = when (version.source) {
                    "APKMirror" -> apkMirrorProvider.getDownloadUrl(version.downloadPageUrl)
                    "APKPure" -> apkPureProvider.getDownloadUrlFromPage(version.downloadPageUrl)
                    else -> null
                }

                if (downloadUrl == null) {
                    _state.value = AlternativeDownloadState.Error("İndirme bağlantısı bulunamadı")
                    return@launch
                }

                val file = downloadFile(downloadUrl, version.packageName, version.versionName)
                if (file != null) {
                    _state.value = AlternativeDownloadState.Downloaded(file)
                } else {
                    _state.value = AlternativeDownloadState.Error("İndirme başarısız")
                }
            } catch (e: Exception) {
                Log.e(TAG, "İndirme hatası", e)
                _state.value = AlternativeDownloadState.Error(e.message ?: "İndirme hatası")
            }
        }
    }

    private suspend fun downloadFile(
        url: String,
        packageName: String,
        versionName: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val downloadDir = File(context.getExternalFilesDir(null), "alternative_downloads")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            // Dosya uzantısını URL'den belirle
            val extension = when {
                url.contains(".xapk", ignoreCase = true) -> ".xapk"
                url.contains(".apkm", ignoreCase = true) -> ".apkm"
                url.contains(".apks", ignoreCase = true) -> ".apks"
                else -> ".apk"
            }

            val fileName = "${packageName}_${versionName}$extension"
            val file = File(downloadDir, fileName)

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "İndirme HTTP hatası: ${response.code}")
                return@withContext null
            }

            val body = response.body ?: return@withContext null
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (totalBytes > 0) {
                            val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                            _downloadProgress.value = progress
                        }
                    }
                }
            }

            Log.i(TAG, "İndirme tamamlandı: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Dosya indirme hatası", e)
            null
        }
    }

    fun reset() {
        _state.value = AlternativeDownloadState.Idle
        _versions.value = emptyList()
        _downloadProgress.value = 0
    }
}

sealed class AlternativeDownloadState {
    data object Idle : AlternativeDownloadState()
    data object Loading : AlternativeDownloadState()
    data class VersionsLoaded(val source: String) : AlternativeDownloadState()
    data object Downloading : AlternativeDownloadState()
    data class Downloaded(val file: File) : AlternativeDownloadState()
    data class Error(val message: String) : AlternativeDownloadState()
}
