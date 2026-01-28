/*
 * OmniAPK - F-Droid API Service
 * Copyright (C) 2024
 */

package com.aurora.store.data.providers

import android.content.Context
import android.util.Log
import com.aurora.store.data.model.FDroidRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F-Droid API provider for fetching apps from F-Droid repositories
 */
@Singleton
class FDroidApiProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fdroidProvider: FDroidProvider
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private var cachedApps: List<FDroidApp> = emptyList()
    private var lastFetchTime: Long = 0
    private val cacheValidityMs = 30 * 60 * 1000L // 30 minutes

    /**
     * Fetch apps from F-Droid repository index
     */
    suspend fun fetchApps(repo: FDroidRepo): List<FDroidApp> = withContext(Dispatchers.IO) {
        try {
            val indexUrl = "${repo.address}/index-v1.json"
            Log.d(TAG, "Fetching index from: $indexUrl")
            
            val request = Request.Builder()
                .url(indexUrl)
                .header("User-Agent", "OmniAPK/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Failed to fetch index from ${repo.name}: ${response.code}")
                return@withContext emptyList()
            }

            val json = response.body?.string() ?: return@withContext emptyList()
            Log.d(TAG, "Received ${json.length} bytes from ${repo.name}")
            parseIndex(json, repo)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching apps from ${repo.name}", e)
            emptyList()
        }
    }

    /**
     * Get enabled repos from FDroidProvider
     */
    private suspend fun getEnabledRepos(): List<FDroidRepo> {
        return fdroidProvider.repos.first().filter { it.enabled }
    }

    /**
     * Check if there are any enabled repos
     */
    suspend fun hasEnabledRepos(): Boolean {
        return getEnabledRepos().isNotEmpty()
    }

    /**
     * Fetch apps from all enabled repos
     */
    suspend fun fetchAllApps(forceRefresh: Boolean = false): List<FDroidApp> = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        if (!forceRefresh && cachedApps.isNotEmpty() && (currentTime - lastFetchTime) < cacheValidityMs) {
            Log.d(TAG, "Returning ${cachedApps.size} cached apps")
            return@withContext cachedApps
        }

        val enabledRepos = getEnabledRepos().take(5) // Limit to 5 repos for performance
        Log.d(TAG, "Fetching from ${enabledRepos.size} enabled repos")
        
        if (enabledRepos.isEmpty()) {
            Log.w(TAG, "No enabled repos found")
            return@withContext emptyList()
        }

        val allApps = mutableListOf<FDroidApp>()
        for (repo in enabledRepos) {
            try {
                Log.d(TAG, "Fetching apps from: ${repo.name}")
                val apps = fetchApps(repo)
                Log.d(TAG, "Got ${apps.size} apps from ${repo.name}")
                allApps.addAll(apps)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from ${repo.name}", e)
            }
        }

        cachedApps = allApps.distinctBy { it.packageName }
        lastFetchTime = currentTime
        Log.d(TAG, "Total unique apps: ${cachedApps.size}")
        cachedApps
    }

    /**
     * Force refresh - clear cache and fetch again
     */
    suspend fun refreshApps(): List<FDroidApp> {
        cachedApps = emptyList()
        lastFetchTime = 0
        return fetchAllApps(forceRefresh = true)
    }

    /**
     * Get recommended/featured apps
     */
    suspend fun getRecommendedApps(): List<FDroidApp> = withContext(Dispatchers.IO) {
        val allApps = fetchAllApps()
        // Return apps with high ratings or specific featured apps
        allApps.filter { it.suggestedVersionCode > 0 }
            .sortedByDescending { it.lastUpdated }
            .take(30)
    }

    /**
     * Get top apps sorted by last update
     */
    suspend fun getTopApps(): List<FDroidApp> = withContext(Dispatchers.IO) {
        val allApps = fetchAllApps()
        allApps.sortedByDescending { it.lastUpdated }.take(50)
    }

    /**
     * Get apps by category
     */
    suspend fun getAppsByCategory(category: String): List<FDroidApp> = withContext(Dispatchers.IO) {
        val allApps = fetchAllApps()
        allApps.filter { it.categories.contains(category) }
    }

    /**
     * Get all available categories
     */
    suspend fun getCategories(): List<String> = withContext(Dispatchers.IO) {
        val allApps = fetchAllApps()
        allApps.flatMap { it.categories }
            .distinct()
            .filter { it.isNotBlank() }
            .sorted()
    }

    /**
     * Search apps
     */
    suspend fun searchApps(query: String): List<FDroidApp> = withContext(Dispatchers.IO) {
        val allApps = fetchAllApps()
        val lowerQuery = query.lowercase()
        allApps.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.summary.lowercase().contains(lowerQuery) ||
            it.packageName.lowercase().contains(lowerQuery)
        }
    }

    private fun parseIndex(json: String, repo: FDroidRepo): List<FDroidApp> {
        val apps = mutableListOf<FDroidApp>()
        try {
            val root = JSONObject(json)
            val packages = root.optJSONObject("packages") ?: return emptyList()

            val packageNames = packages.keys()
            while (packageNames.hasNext()) {
                val packageName = packageNames.next()
                val packageArray = packages.optJSONArray(packageName) ?: continue
                if (packageArray.length() == 0) continue

                val latestVersion = packageArray.getJSONObject(0)

                // Get app metadata
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
                    FDroidApp(
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
                        repoAddress = repo.address
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing index from ${repo.name}", e)
        }
        return apps
    }

    companion object {
        private const val TAG = "FDroidApiProvider"
    }
}

/**
 * F-Droid app data class
 */
data class FDroidApp(
    val packageName: String,
    val name: String,
    val summary: String,
    val description: String = "",
    val versionName: String,
    val versionCode: Int,
    val iconUrl: String,
    val downloadUrl: String,
    val license: String = "",
    val webSite: String = "",
    val sourceCode: String = "",
    val categories: List<String> = emptyList(),
    val size: Long = 0,
    val minSdkVersion: Int = 0,
    val lastUpdated: Long = 0,
    val added: Long = 0,
    val suggestedVersionCode: Int = 0,
    val repoName: String = "",
    val repoAddress: String = ""
)
