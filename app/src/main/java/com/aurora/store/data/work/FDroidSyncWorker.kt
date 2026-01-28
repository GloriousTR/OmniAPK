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
import com.aurora.store.viewmodel.FDroidSyncStatus
import com.aurora.store.viewmodel.SyncState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
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
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "Starting F-Droid sync")
        
        // Update global sync status
        FDroidSyncStatus.updateState(SyncState.Syncing)
        
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
                    setForeground(createForegroundInfo("Syncing ${repo.name} (${index + 1}/${enabledRepos.size})"))
                    
                    val apps = fetchAppsFromRepo(repo, syncTime)
                    if (apps.isNotEmpty()) {
                        fdroidAppDao.insertApps(apps)
                        totalApps += apps.size
                        Log.i(TAG, "Synced ${apps.size} apps from ${repo.name}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync ${repo.name}", e)
                }
            }
            
            Log.i(TAG, "F-Droid sync complete. Total apps: $totalApps")
            
            // Update global sync status with results
            FDroidSyncStatus.updateAppCount(totalApps)
            FDroidSyncStatus.updateLastSyncTime(syncTime)
            FDroidSyncStatus.updateState(SyncState.Success)
            
            // Notify completion
            notifyComplete(totalApps)
            
            return@withContext Result.success(workDataOf("apps_synced" to totalApps))
        } catch (e: Exception) {
            Log.e(TAG, "F-Droid sync failed", e)
            FDroidSyncStatus.updateState(SyncState.Error(e.message ?: "Unknown error"))
            return@withContext Result.failure()
        }
    }
    
    private fun fetchAppsFromRepo(repo: FDroidRepo, syncTime: Long): List<FDroidAppEntity> {
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
        return parseIndex(json, repo, syncTime)
    }
    
    private fun parseIndex(json: String, repo: FDroidRepo, syncTime: Long): List<FDroidAppEntity> {
        val apps = mutableListOf<FDroidAppEntity>()
        try {
            val root = JSONObject(json)
            val packages = root.optJSONObject("packages") ?: return emptyList()
            
            val packageNames = packages.keys()
            while (packageNames.hasNext()) {
                val packageName = packageNames.next()
                val packageArray = packages.optJSONArray(packageName) ?: continue
                if (packageArray.length() == 0) continue
                
                val latestVersion = packageArray.getJSONObject(0)
                val appsObj = root.optJSONObject("apps")
                val appMeta = appsObj?.optJSONObject(packageName)
                
                val name = appMeta?.optString("name", packageName) ?: packageName
                val summary = appMeta?.optString("summary", "") ?: ""
                val description = appMeta?.optString("description", "") ?: ""
                val icon = appMeta?.optString("icon", "") ?: ""
                val license = appMeta?.optString("license", "") ?: ""
                val webSite = appMeta?.optString("webSite", "") ?: ""
                val sourceCode = appMeta?.optString("sourceCode", "") ?: ""
                val lastUpdated = appMeta?.optLong("lastUpdated", 0) ?: 0
                val added = appMeta?.optLong("added", 0) ?: 0
                val suggestedVersionCode = appMeta?.optInt("suggestedVersionCode", 0) ?: 0
                
                val categoriesArray = appMeta?.optJSONArray("categories")
                val categories = mutableListOf<String>()
                if (categoriesArray != null) {
                    for (i in 0 until categoriesArray.length()) {
                        categories.add(categoriesArray.getString(i))
                    }
                }
                
                val versionName = latestVersion.optString("versionName", "")
                val versionCode = latestVersion.optInt("versionCode", 0)
                val apkName = latestVersion.optString("apkName", "")
                val size = latestVersion.optLong("size", 0)
                val minSdkVersion = latestVersion.optInt("minSdkVersion", 0)
                
                val iconUrl = if (icon.isNotEmpty()) "${repo.address}/icons-640/$icon" else ""
                val downloadUrl = if (apkName.isNotEmpty()) "${repo.address}/$apkName" else ""
                
                apps.add(
                    FDroidAppEntity(
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
                        syncTimestamp = syncTime
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing index from ${repo.name}", e)
        }
        return apps
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
    }
}
