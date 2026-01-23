package com.omniapk.data.sources

import com.omniapk.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject

/**
 * F-Droid source provider for searching and downloading open-source apps.
 * Uses F-Droid's public API and repository index.
 */
class FDroidProvider @Inject constructor() : SourceProvider {
    override val name: String = "F-Droid"
    
    private val searchUrl = "https://search.f-droid.org/api/v1"
    private val baseUrl = "https://f-droid.org"
    private val repoUrl = "https://f-droid.org/repo"
    
    override suspend fun searchApp(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            // Use F-Droid search API
            val url = URL("$searchUrl/?q=${java.net.URLEncoder.encode(query, "UTF-8")}")
            val response = url.readText()
            val json = JSONObject(response)
            
            val apps = mutableListOf<AppInfo>()
            val results = json.optJSONArray("results") ?: return@withContext emptyList()
            
            for (i in 0 until minOf(results.length(), 20)) {
                val app = results.getJSONObject(i)
                val packageName = app.optString("packageName", "")
                val appName = app.optString("name", "")
                val summary = app.optString("summary", "")
                val iconPath = app.optString("icon", "")
                
                if (packageName.isNotEmpty() && appName.isNotEmpty()) {
                    apps.add(
                        AppInfo(
                            packageName = packageName,
                            name = appName,
                            versionName = "", // Will be fetched on detail view
                            versionCode = 0,
                            icon = null,
                            iconUrl = if (iconPath.isNotEmpty()) "$repoUrl/icons-640/$iconPath" else null,
                            source = name,
                            description = summary
                        )
                    )
                }
            }
            apps
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun checkUpdate(
        packageName: String, 
        currentVersionCode: Long, 
        currentVersionName: String
    ): AppInfo? = withContext(Dispatchers.IO) {
        try {
            // F-Droid package API
            val url = URL("$baseUrl/api/v1/packages/$packageName")
            val response = url.readText()
            val json = JSONObject(response)
            
            val packages = json.optJSONArray("packages") ?: return@withContext null
            if (packages.length() == 0) return@withContext null
            
            // Get latest version
            val latestPackage = packages.getJSONObject(0)
            val latestVersionCode = latestPackage.optLong("versionCode", 0)
            val latestVersionName = latestPackage.optString("versionName", "")
            
            if (latestVersionCode > currentVersionCode) {
                AppInfo(
                    packageName = packageName,
                    name = json.optString("name", packageName),
                    versionName = latestVersionName,
                    versionCode = latestVersionCode,
                    icon = null,
                    source = name
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getDownloadUrl(appInfo: AppInfo): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/v1/packages/${appInfo.packageName}")
            val response = url.readText()
            val json = JSONObject(response)
            
            val packages = json.optJSONArray("packages") ?: return@withContext null
            if (packages.length() == 0) return@withContext null
            
            // Get latest package APK URL
            val latestPackage = packages.getJSONObject(0)
            val apkName = latestPackage.optString("apkName", "")
            
            if (apkName.isNotEmpty()) {
                "$repoUrl/$apkName"
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
