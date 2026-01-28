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
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Global cache for alternative download versions.
 * This cache is shared across all AlternativeDownloadViewModel instances.
 */
object AlternativeVersionCache {
    private const val TAG = "AltVersionCache"
    private const val CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    
    data class CachedVersions(
        val apkMirrorVersions: List<AppVersion> = emptyList(),
        val apkPureVersions: List<AppVersion> = emptyList(),
        val timestamp: Long = System.currentTimeMillis(),
        val isLoading: Boolean = false
    )
    
    private val cache = ConcurrentHashMap<String, CachedVersions>()
    
    private val _cacheState = MutableStateFlow<Map<String, CachedVersions>>(emptyMap())
    val cacheState: StateFlow<Map<String, CachedVersions>> = _cacheState.asStateFlow()
    
    fun getCached(packageName: String): CachedVersions? {
        val cached = cache[packageName] ?: return null
        // Check if cache is still valid
        if (System.currentTimeMillis() - cached.timestamp > CACHE_DURATION_MS) {
            cache.remove(packageName)
            return null
        }
        return cached
    }
    
    fun setLoading(packageName: String) {
        val existing = cache[packageName] ?: CachedVersions()
        cache[packageName] = existing.copy(isLoading = true)
        _cacheState.value = cache.toMap()
    }
    
    fun updateAPKMirrorVersions(packageName: String, versions: List<AppVersion>) {
        val existing = cache[packageName] ?: CachedVersions()
        cache[packageName] = existing.copy(
            apkMirrorVersions = versions,
            timestamp = System.currentTimeMillis(),
            isLoading = false
        )
        _cacheState.value = cache.toMap()
        Log.d(TAG, "Cached ${versions.size} APKMirror versions for $packageName")
    }
    
    fun updateAPKPureVersions(packageName: String, versions: List<AppVersion>) {
        val existing = cache[packageName] ?: CachedVersions()
        cache[packageName] = existing.copy(
            apkPureVersions = versions,
            timestamp = System.currentTimeMillis(),
            isLoading = false
        )
        _cacheState.value = cache.toMap()
        Log.d(TAG, "Cached ${versions.size} APKPure versions for $packageName")
    }
    
    fun setLoadingComplete(packageName: String) {
        val existing = cache[packageName] ?: return
        cache[packageName] = existing.copy(isLoading = false)
        _cacheState.value = cache.toMap()
    }
    
    fun clear() {
        cache.clear()
        _cacheState.value = emptyMap()
    }
}

@HiltViewModel
class AlternativeDownloadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apkMirrorProvider: APKMirrorProvider,
    private val apkPureProvider: APKPureProvider
) : ViewModel() {

    companion object {
        private const val TAG = "AltDownloadVM"
        private const val ITEMS_PER_PAGE = 10
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
    
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()
    
    private var allVersions: List<AppVersion> = emptyList()
    
    /**
     * Arka planda APKMirror ve APKPure versiyonlarını önceden yükle ve cache'e al
     */
    fun preloadVersions(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if already cached
            val cached = AlternativeVersionCache.getCached(packageName)
            if (cached != null && !cached.isLoading && 
                (cached.apkMirrorVersions.isNotEmpty() || cached.apkPureVersions.isNotEmpty())) {
                Log.d(TAG, "Versions already cached for $packageName")
                return@launch
            }
            
            AlternativeVersionCache.setLoading(packageName)
            Log.d(TAG, "Preloading versions for $packageName")
            
            // Load APKMirror versions in background
            try {
                val mirrorVersions = apkMirrorProvider.getAppVersions(packageName)
                AlternativeVersionCache.updateAPKMirrorVersions(packageName, mirrorVersions)
            } catch (e: Exception) {
                Log.e(TAG, "APKMirror preload failed for $packageName", e)
            }
            
            // Load APKPure versions in background
            try {
                val pureVersions = apkPureProvider.getAppVersions(packageName)
                AlternativeVersionCache.updateAPKPureVersions(packageName, pureVersions)
            } catch (e: Exception) {
                Log.e(TAG, "APKPure preload failed for $packageName", e)
            }
            
            AlternativeVersionCache.setLoadingComplete(packageName)
        }
    }

    /**
     * APKMirror'dan uygulama versiyonlarını yükle (cache destekli)
     */
    fun loadAPKMirrorVersions(packageName: String) {
        viewModelScope.launch {
            _state.value = AlternativeDownloadState.Loading
            _currentPage.value = 1
            
            try {
                // Check cache first
                val cached = AlternativeVersionCache.getCached(packageName)
                if (cached != null && cached.apkMirrorVersions.isNotEmpty()) {
                    Log.d(TAG, "Using cached APKMirror versions for $packageName")
                    allVersions = cached.apkMirrorVersions
                    updatePaginatedVersions()
                    _state.value = AlternativeDownloadState.VersionsLoaded("APKMirror")
                    return@launch
                }
                
                // Fetch from provider
                val versionList = withContext(Dispatchers.IO) {
                    apkMirrorProvider.getAppVersions(packageName)
                }
                
                // Update cache
                AlternativeVersionCache.updateAPKMirrorVersions(packageName, versionList)
                
                allVersions = versionList
                updatePaginatedVersions()
                
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
     * APKPure'dan uygulama versiyonlarını yükle (cache destekli)
     */
    fun loadAPKPureVersions(packageName: String) {
        viewModelScope.launch {
            _state.value = AlternativeDownloadState.Loading
            _currentPage.value = 1
            
            try {
                // Check cache first
                val cached = AlternativeVersionCache.getCached(packageName)
                if (cached != null && cached.apkPureVersions.isNotEmpty()) {
                    Log.d(TAG, "Using cached APKPure versions for $packageName")
                    allVersions = cached.apkPureVersions
                    updatePaginatedVersions()
                    _state.value = AlternativeDownloadState.VersionsLoaded("APKPure")
                    return@launch
                }
                
                // Fetch from provider
                val versionList = withContext(Dispatchers.IO) {
                    apkPureProvider.getAppVersions(packageName)
                }
                
                // Update cache
                AlternativeVersionCache.updateAPKPureVersions(packageName, versionList)
                
                allVersions = versionList
                updatePaginatedVersions()
                
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
     * Sayfa değiştir
     */
    fun goToPage(page: Int) {
        if (page in 1.._totalPages.value) {
            _currentPage.value = page
            updatePaginatedVersions()
        }
    }
    
    private fun updatePaginatedVersions() {
        val totalItems = allVersions.size
        _totalPages.value = (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE
        
        val startIndex = (_currentPage.value - 1) * ITEMS_PER_PAGE
        val endIndex = minOf(startIndex + ITEMS_PER_PAGE, totalItems)
        
        _versions.value = if (startIndex < totalItems) {
            allVersions.subList(startIndex, endIndex)
        } else {
            emptyList()
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
                    "APKMirror" -> withContext(Dispatchers.IO) {
                        apkMirrorProvider.getDownloadUrl(version.downloadPageUrl)
                    }
                    "APKPure" -> withContext(Dispatchers.IO) {
                        apkPureProvider.getDownloadUrlFromPage(version.downloadPageUrl)
                    }
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
        _currentPage.value = 1
        _totalPages.value = 1
        allVersions = emptyList()
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
