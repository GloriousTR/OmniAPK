package com.aurora.store.data.providers

import android.content.Context
import com.aurora.store.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * APKPure provider for app updates
 * Uses web scraping to get app versions from APKPure
 * 
 * Note: APKPure doesn't have an official API, so this uses HTML parsing
 */
@Singleton
class APKPureProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    private val baseUrl = "https://apkpure.com"
    private val searchUrl = "$baseUrl/search?q="

    /**
     * Search for apps on APKPure
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
                .url("$baseUrl/$packageName/versions")
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
    suspend fun getDownloadUrl(packageName: String, versionName: String): String? = withContext(Dispatchers.IO) {
        try {
            // APKPure download URL pattern
            val downloadPageUrl = "$baseUrl/$packageName/$packageName-$versionName-download"
            
            val request = Request.Builder()
                .url(downloadPageUrl)
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
        
        // Pattern to extract app info from search results
        val appPattern = Regex(
            """<a[^>]*class="[^"]*first-info[^"]*"[^>]*href="([^"]*)"[^>]*>.*?<p[^>]*class="[^"]*p1[^"]*"[^>]*>([^<]*)</p>.*?</a>""",
            RegexOption.DOT_MATCHES_ALL
        )
        
        appPattern.findAll(html).take(20).forEach { match ->
            val url = match.groupValues[1]
            val name = match.groupValues[2].trim()
            
            // Extract package name from URL
            val packageName = url.substringAfterLast("/")
            
            results.add(
                AppInfo(
                    packageName = packageName,
                    name = name,
                    versionName = "",
                    versionCode = 0,
                    iconUrl = "",
                    source = "APKPure",
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
            """<a[^>]*href="([^"]*)"[^>]*>.*?<span[^>]*class="[^"]*ver-item-n[^"]*"[^>]*>([^<]*)</span>""",
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
                    source = "APKPure"
                )
            )
        }
        
        return versions
    }

    private fun parseDownloadLink(html: String): String? {
        // Look for download button link
        val downloadPattern = Regex(
            """<a[^>]*id="[^"]*download_link[^"]*"[^>]*href="([^"]*)"[^>]*>""",
            RegexOption.DOT_MATCHES_ALL
        )
        
        val match = downloadPattern.find(html)
        return match?.groupValues?.get(1)
    }

    companion object {
        const val SOURCE_NAME = "APKPure"
    }
}
