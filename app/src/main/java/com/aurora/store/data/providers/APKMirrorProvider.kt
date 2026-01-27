package com.aurora.store.data.providers

import android.content.Context
import com.aurora.store.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * APKMirror provider for app updates
 * Uses web scraping to get app versions from APKMirror
 * 
 * Note: APKMirror doesn't have an official API, so this uses HTML parsing
 */
@Singleton
class APKMirrorProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    private val baseUrl = "https://www.apkmirror.com"
    private val searchUrl = "$baseUrl/?post_type=app_release&searchtype=apk&s="

    /**
     * Search for apps on APKMirror
     */
    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$searchUrl${query.replace(" ", "+")}")
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val html = response.body?.string() ?: return@withContext emptyList()
            parseSearchResults(html)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get available versions for a specific app
     */
    suspend fun getAppVersions(packageName: String): List<AppVersion> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/apk/${packageName.replace(".", "-")}/")
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val html = response.body?.string() ?: return@withContext emptyList()
            parseVersionList(html, packageName)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get download URL for a specific version
     */
    suspend fun getDownloadUrl(versionPageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            // First request to version page
            val request = Request.Builder()
                .url(versionPageUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val html = response.body?.string() ?: return@withContext null
            parseDownloadLink(html)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseSearchResults(html: String): List<AppInfo> {
        val results = mutableListOf<AppInfo>()
        
        // Simple regex pattern to extract app info from search results
        val appPattern = Regex(
            """<div class="appRow"[^>]*>.*?<a[^>]*href="([^"]*)"[^>]*>.*?<h5[^>]*>([^<]*)</h5>.*?</div>""",
            RegexOption.DOT_MATCHES_ALL
        )
        
        appPattern.findAll(html).take(20).forEach { match ->
            val url = match.groupValues[1]
            val name = match.groupValues[2].trim()
            
            // Extract package name from URL if possible
            val packageName = url.substringAfter("/apk/")
                .substringBefore("/")
                .replace("-", ".")
            
            results.add(
                AppInfo(
                    packageName = packageName,
                    name = name,
                    versionName = "",
                    versionCode = 0,
                    iconUrl = "",
                    source = "APKMirror",
                    downloadUrl = "$baseUrl$url"
                )
            )
        }
        
        return results
    }

    private fun parseVersionList(html: String, packageName: String): List<AppVersion> {
        val versions = mutableListOf<AppVersion>()
        
        // Pattern to extract version info
        val versionPattern = Regex(
            """<a[^>]*href="([^"]*)"[^>]*class="fontBlack"[^>]*>([^<]*)</a>""",
            RegexOption.DOT_MATCHES_ALL
        )
        
        versionPattern.findAll(html).take(10).forEach { match ->
            val url = match.groupValues[1]
            val versionName = match.groupValues[2].trim()
            
            versions.add(
                AppVersion(
                    packageName = packageName,
                    versionName = versionName,
                    versionCode = 0,
                    downloadPageUrl = "$baseUrl$url",
                    source = "APKMirror"
                )
            )
        }
        
        return versions
    }

    private fun parseDownloadLink(html: String): String? {
        // Look for download button link
        val downloadPattern = Regex(
            """<a[^>]*href="([^"]*)"[^>]*class="[^"]*downloadButton[^"]*"[^>]*>""",
            RegexOption.DOT_MATCHES_ALL
        )
        
        val match = downloadPattern.find(html)
        return match?.groupValues?.get(1)?.let { "$baseUrl$it" }
    }

    companion object {
        const val SOURCE_NAME = "APKMirror"
    }
}

/**
 * App version info from APKMirror/APKPure
 */
data class AppVersion(
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val downloadPageUrl: String,
    val downloadUrl: String = "",
    val source: String,
    val size: String = "",
    val uploadDate: String = ""
)
