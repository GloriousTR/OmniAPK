/*
 * OmniAPK - F-Droid Sync Worker
 * Background worker for syncing F-Droid repository indexes
 */

package com.aurora.store.data.work

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.aurora.Constants
import com.aurora.store.R
import com.aurora.store.data.model.FDroidRepo
import com.aurora.store.data.providers.FDroidProvider
import com.aurora.store.data.room.fdroid.FDroidAppDao
import com.aurora.store.data.room.fdroid.FDroidAppEntity
import com.aurora.store.data.room.fdroid.FDroidVersionEntity
import com.aurora.store.viewmodel.FDroidSyncStatus
import com.aurora.store.viewmodel.SyncState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Worker to sync F-Droid repository indexes in the background
 */
@HiltWorker
class FDroidSyncWorker @AssistedInject constructor(
    private val fdroidProvider: FDroidProvider,
    private val fdroidAppDao: FDroidAppDao,
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notificationId = 200
    
    // Optimized OkHttp client with better timeouts
    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "Starting F-Droid sync")
        
        // Update global sync status
        FDroidSyncStatus.updateState(SyncState.Syncing())
        
        try {
            setForeground(createForegroundInfo("Syncing F-Droid repositories..."))
            
            val enabledRepos = fdroidProvider.repos.first().filter { it.enabled }
            Log.i(TAG, "Found ${enabledRepos.size} enabled repos")
            
            if (enabledRepos.isEmpty()) {
                Log.w(TAG, "No enabled repos, nothing to sync")
                FDroidSyncStatus.updateState(SyncState.Idle)
                return@withContext Result.success(workDataOf("apps_synced" to 0))
            }
            
            var totalApps = 0
            val syncTime = System.currentTimeMillis()
            
            enabledRepos.forEachIndexed { index, repo ->
                try {
                    Log.i(TAG, "Syncing ${repo.name}...")
                    
                    // Update global status with detailed progress
                    FDroidSyncStatus.updateSyncingProgress(
                        currentRepo = repo.name,
                        currentRepoIndex = index + 1,
                        totalRepos = enabledRepos.size
                    )
                    
                    setForeground(createForegroundInfo("Syncing ${repo.name} (${index + 1}/${enabledRepos.size})"))
                    
                    // fetchAppsFromRepo now handles chunked insertion internally
                    val apps = fetchAppsFromRepo(repo, syncTime)
                    totalApps += apps.size
                    Log.i(TAG, "Synced ${apps.size} apps from ${repo.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync ${repo.name}", e)
                }
            }
            
            Log.i(TAG, "F-Droid sync complete. Total apps: $totalApps")
            
            // Update global sync status with results atomically
            FDroidSyncStatus.updateSyncResult(SyncState.Success, totalApps, syncTime)
            
            // Notify completion
            notifyComplete(totalApps)
            
            return@withContext Result.success(workDataOf("apps_synced" to totalApps))
        } catch (e: Exception) {
            Log.e(TAG, "F-Droid sync failed", e)
            FDroidSyncStatus.updateState(SyncState.Error(e.message ?: "Unknown error"))
            return@withContext Result.failure()
        }
    }
    
    private suspend fun fetchAppsFromRepo(repo: FDroidRepo, syncTime: Long): List<FDroidAppEntity> {
        val indexUrl = "${repo.address}/index-v1.json"
        Log.d(TAG, "Fetching: $indexUrl")
        
        val request = Request.Builder()
            .url(indexUrl)
            .header("User-Agent", "OmniAPK/1.0")
            .build()
            
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.e(TAG, "HTTP ${response.code} from ${repo.name}")
            return emptyList()
        }
        
        val json = response.body?.string() ?: return emptyList()
        
        // Process in chunks to avoid memory issues
        return parseIndexInChunks(json, repo, syncTime)
    }
    
    /**
     * Parse index in chunks to avoid memory issues and UI freezes
     */
    private suspend fun parseIndexInChunks(json: String, repo: FDroidRepo, syncTime: Long): List<FDroidAppEntity> {
        val allApps = mutableListOf<FDroidAppEntity>()
        
        try {
            val root = JSONObject(json)
            val packages = root.optJSONObject("packages") ?: return emptyList()
            val appsObj = root.optJSONObject("apps")
            
            val packageNames = packages.keys().asSequence().toList()
            val totalPackages = packageNames.size
            Log.d(TAG, "Processing ${totalPackages} packages from ${repo.name}")
            
            // Process packages in chunks
            packageNames.chunked(BATCH_SIZE).forEachIndexed { chunkIndex, chunk ->
                // Yield to prevent blocking the coroutine
                yield()
                
                val chunkApps = mutableListOf<FDroidAppEntity>()
                val chunkVersions = mutableListOf<FDroidVersionEntity>()
                
                chunk.forEach { packageName ->
                    try {
                        val packageArray = packages.optJSONArray(packageName) ?: return@forEach
                        if (packageArray.length() == 0) return@forEach
                        
                        val latestVersion = packageArray.getJSONObject(0)
                        val appMeta = appsObj?.optJSONObject(packageName)
                        
                        // Parse app entity
                        val appEntity = parseAppEntity(
                            packageName, packageArray, appMeta, repo, syncTime
                        )
                        chunkApps.add(appEntity)
                        
                        // Parse versions (in smaller batches)
                        val versions = parseVersions(packageName, packageArray, repo)
                        chunkVersions.addAll(versions)
                        
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing package $packageName: ${e.message}")
                    }
                }
                
                // Insert chunk to database
                if (chunkApps.isNotEmpty()) {
                    fdroidAppDao.insertApps(chunkApps)
                    allApps.addAll(chunkApps)
                    
                    Log.d(TAG, "Inserted chunk ${chunkIndex + 1}: ${chunkApps.size} apps from ${repo.name}")
                }
                
                // Insert versions in smaller batches to avoid memory issues
                if (chunkVersions.isNotEmpty()) {
                    chunkVersions.chunked(VERSION_BATCH_SIZE).forEach { versionBatch ->
                        fdroidAppDao.insertVersions(versionBatch)
                    }
                    Log.d(TAG, "Inserted ${chunkVersions.size} versions for chunk ${chunkIndex + 1}")
                }
                
                // Yield between chunks to allow other operations
                yield()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing index from ${repo.name}", e)
        }
        
        return allApps
    }
    
    /**
     * Parse a single app entity from JSON
     */
    private fun parseAppEntity(
        packageName: String,
        packageArray: org.json.JSONArray,
        appMeta: JSONObject?,
        repo: FDroidRepo,
        syncTime: Long
    ): FDroidAppEntity {
        val latestVersion = packageArray.getJSONObject(0)
        
        // Handle localized strings
        val name = appMeta?.optLocalized("name", packageName) ?: packageName
        val summary = appMeta?.optLocalized("summary", "") ?: ""
        val description = appMeta?.optLocalized("description", "") ?: ""
        val icon = appMeta?.optString("icon", "") ?: ""
        val authorName = appMeta?.optLocalized("authorName", "") ?: ""
        val whatsNew = appMeta?.optLocalized("whatsNew", "") ?: ""
        
        // Anti-features
        val antiFeatures = mutableListOf<String>()
        val antiFeaturesArray = appMeta?.optJSONArray("antiFeatures")
        if (antiFeaturesArray != null) {
            for (i in 0 until antiFeaturesArray.length()) {
                antiFeatures.add(antiFeaturesArray.getString(i))
            }
        }
        
        // Screenshots
        val screenshots = mutableListOf<String>()
        val screenshotsObj = appMeta?.optJSONObject("screenshots")
        val phoneList = screenshotsObj?.optJSONArray("phone") ?: appMeta?.optJSONArray("screenshots")
        if (phoneList != null) {
            for (i in 0 until phoneList.length()) {
                screenshots.add("${repo.address}/${phoneList.getString(i)}")
            }
        }
        
        val license = appMeta?.optString("license", "") ?: ""
        val webSite = appMeta?.optString("webSite", "") ?: ""
        val sourceCode = appMeta?.optString("sourceCode", "") ?: ""
        val lastUpdated = appMeta?.optLong("lastUpdated", 0) ?: 0
        val added = appMeta?.optLong("added", 0) ?: 0
        val suggestedVersionCode = appMeta?.optInt("suggestedVersionCode", 0) ?: 0
        
        // Categories
        val categoriesArray = appMeta?.optJSONArray("categories")
        val categories = mutableListOf<String>()
        if (categoriesArray != null) {
            for (i in 0 until categoriesArray.length()) {
                categories.add(categoriesArray.getString(i))
            }
        }
        
        // Version info
        val versionName = latestVersion.optString("versionName", "")
        val versionCode = latestVersion.optInt("versionCode", 0)
        val apkName = latestVersion.optString("apkName", "")
        val size = latestVersion.optLong("size", 0)
        val minSdkVersion = latestVersion.optInt("minSdkVersion", 0)
        
        val iconUrl = if (icon.isNotEmpty()) "${repo.address}/icons-640/$icon" else ""
        val downloadUrl = if (apkName.isNotEmpty()) "${repo.address}/$apkName" else ""
        
        return FDroidAppEntity(
            packageName = packageName,
            name = name,
            summary = summary,
            description = description,
            versionName = versionName,
            versionCode = versionCode,
            iconUrl = iconUrl,
            downloadUrl = downloadUrl,
            license = license,
            webSite = webSite,
            sourceCode = sourceCode,
            categories = categories,
            size = size,
            minSdkVersion = minSdkVersion,
            lastUpdated = lastUpdated,
            added = added,
            suggestedVersionCode = suggestedVersionCode,
            repoName = repo.name,
            repoAddress = repo.address,
            syncTimestamp = syncTime,
            authorName = authorName,
            screenshots = screenshots,
            antiFeatures = antiFeatures,
            whatsNew = whatsNew
        )
    }
    
    /**
     * Parse all versions for a package
     */
    private fun parseVersions(
        packageName: String,
        packageArray: org.json.JSONArray,
        repo: FDroidRepo
    ): List<FDroidVersionEntity> {
        val versions = mutableListOf<FDroidVersionEntity>()
        
        for (i in 0 until packageArray.length()) {
            try {
                val versionObj = packageArray.getJSONObject(i)
                val vCode = versionObj.optInt("versionCode", 0)
                val vName = versionObj.optString("versionName", "")
                val apkName = versionObj.optString("apkName", "")
                val vSize = versionObj.optLong("size", 0)
                val minSdk = versionObj.optInt("minSdkVersion", 0)
                val targetSdk = versionObj.optInt("targetSdkVersion", 0)
                val vAdded = versionObj.optLong("added", 0)
                val hash = versionObj.optString("hash", "")
                val hashType = versionObj.optString("hashType", "")
                
                if (apkName.isNotEmpty()) {
                    versions.add(
                        FDroidVersionEntity(
                            packageName = packageName,
                            versionName = vName,
                            versionCode = vCode,
                            size = vSize,
                            downloadUrl = "${repo.address}/$apkName",
                            added = vAdded,
                            minSdkVersion = minSdk,
                            targetSdkVersion = targetSdk,
                            hash = hash,
                            hashType = hashType,
                            repoName = repo.name,
                            releaseNotes = "" // Per-version release notes are tricky in index-v1 JSON, usually in localized description
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing version $i for $packageName: ${e.message}")
            }
        }
        
        return versions
    }
    
    
    private fun createForegroundInfo(message: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_UPDATES)
            .setContentTitle("F-Droid Sync")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification_outlined)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()
        return ForegroundInfo(notificationId, notification)
    }
    
    private fun notifyComplete(appCount: Int) {
        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_UPDATES)
            .setContentTitle("F-Droid Sync Complete")
            .setContentText("Synced $appCount apps from F-Droid repositories")
            .setSmallIcon(R.drawable.ic_notification_outlined)
            .setAutoCancel(true)
            .build()
        context.getSystemService<NotificationManager>()?.notify(notificationId, notification)
    }
    
    companion object {
        private const val TAG = "FDroidSyncWorker"
        const val WORK_NAME = "fdroid_sync"
        // Chunk size for batch database operations to prevent memory issues
        private const val BATCH_SIZE = 100 // Process apps in batches of 100
        private const val VERSION_BATCH_SIZE = 50 // Process versions in smaller batches
    }
    
    private fun JSONObject.optLocalized(key: String, default: String): String {
        val value = this.opt(key)
        if (value is JSONObject) {
            // Try English first, then fallback to any available locale
            return value.optString("en-US")
                .takeIf { it.isNotEmpty() }
                ?: value.optString("en")
                .takeIf { it.isNotEmpty() }
                ?: value.keys().next()?.let { value.optString(it) }
                ?: default
        }
        return if (value is String) value else default
    }
}
