package com.aurora.store.data.providers

import android.content.Context
import android.util.Log
import com.aurora.store.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.Connection
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

/**
 * APKPure provider for app updates
 * Uses Jsoup for robust HTML parsing
 * Updated with improved selectors and anti-bot measures
 */
@Singleton
class APKPureProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "APKPure"
        private const val BASE_URL = "https://apkpure.com"
        // Alternative domains if main is blocked
        private const val ALT_URL = "https://apkpure.net"
        private const val TIMEOUT = 20000
        const val SOURCE_NAME = "APKPure"
    }
    
    // Modern browser user agents for better compatibility
    private val userAgents = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0"
    )
    
    private fun getRandomUserAgent(): String = userAgents.random()
    
    /**
     * Create a Jsoup connection with proper headers to avoid bot detection
     */
    private fun createConnection(url: String): Connection {
        return Jsoup.connect(url)
            .userAgent(getRandomUserAgent())
            .timeout(TIMEOUT)
            .followRedirects(true)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Connection", "keep-alive")
            .header("Upgrade-Insecure-Requests", "1")
            .header("Sec-Fetch-Dest", "document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "none")
            .header("Sec-Fetch-User", "?1")
            .referrer("https://www.google.com/")
    }

    /**
     * Search for apps on APKPure
     * Selectors based on https://github.com/kiber-io/apkd
     */
    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            val searchUrl = "$BASE_URL/search?q=${query.replace(" ", "+")}"
            Log.d(TAG, "Searching APKPure: $searchUrl")
            
            val doc = createConnection(searchUrl).get()

            val results = mutableListOf<AppInfo>()
            
            // From apkd: Primary selector is div.first with data-dt-app attribute
            val firstResult = doc.select("div.first").first()
            if (firstResult != null) {
                val dataDtApp = firstResult.attr("data-dt-app")
                val link = firstResult.select("a.first-info").first()
                
                if (dataDtApp.isNotEmpty() && link != null) {
                    val href = link.attr("href")
                    val name = link.attr("title").ifEmpty { link.text().trim() }
                    val icon = firstResult.select("img").first()
                    val iconUrl = icon?.let { img ->
                        val src = img.attr("data-src").ifEmpty { img.attr("src") }
                        if (src.startsWith("//")) "https:$src" else src
                    } ?: ""
                    
                    results.add(
                        AppInfo(
                            packageName = dataDtApp, // Use data-dt-app as package name
                            name = name,
                            versionName = "",
                            versionCode = 0,
                            iconUrl = iconUrl,
                            source = "APKPure",
                            downloadUrl = if (href.startsWith("http")) href else "$BASE_URL$href"
                        )
                    )
                }
            }
            
            // Fallback: Try other selectors for different page versions
            if (results.isEmpty()) {
                val items = doc.select("div.list-wrap div.list-item").ifEmpty {
                    doc.select(".search-res li dl").ifEmpty {
                        doc.select("div.apk-item, .apk-list-item").ifEmpty {
                            doc.select("a.apk-card").ifEmpty {
                                doc.select("[class*=search] [class*=item]")
                            }
                        }
                    }
                }
                
                Log.d(TAG, "Found ${items.size} items in search results (fallback)")

                items.take(20).forEach { item ->
                    try {
                        val link = item.select("a.first-info, a.dd").first()
                            ?: item.select("a.title-link").first()
                            ?: item.select("a[href*=\\/]").first()
                            ?: item.select("a").first()
                        
                        val icon = item.select("img.lazyload").first()
                            ?: item.select("img[data-src]").first()
                            ?: item.select("img").first()
                        
                        val title = item.select(".p1").text().ifEmpty {
                            item.select(".title").text().ifEmpty {
                                item.select("h3").text().ifEmpty {
                                    link?.attr("title") ?: ""
                                }
                            }
                        }
                        
                        if (link != null) {
                            val href = link.attr("href")
                            val iconUrl = icon?.let { img ->
                                val src = img.attr("data-src").ifEmpty { img.attr("src") }
                                if (src.startsWith("//")) "https:$src" else src
                            } ?: ""
                            val name = title.ifEmpty { link.text().trim() }
                            
                            // URL format: /app-name/package.name
                            val packageName = href.substringAfterLast("/").ifEmpty {
                                href.split("/").lastOrNull { it.contains(".") } ?: href.substringAfterLast("/")
                            }
                            
                            if (name.isNotEmpty() && href.isNotEmpty()) {
                                results.add(
                                    AppInfo(
                                        packageName = packageName,
                                        name = name,
                                        versionName = "",
                                        versionCode = 0,
                                        iconUrl = iconUrl,
                                        source = "APKPure",
                                        downloadUrl = if (href.startsWith("http")) href else "$BASE_URL$href"
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing search item: ${e.message}")
                    }
                }
            }
            
            Log.d(TAG, "Search returned ${results.size} results for query: $query")
            results
        } catch (e: Exception) {
            Log.e(TAG, "Search failed for '$query': ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get available versions for a specific app
     */
    suspend fun getAppVersions(packageName: String): List<AppVersion> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting versions for package: $packageName")
        
        try {
            // Search for the package to get the correct slug URL
            val searchResults = searchApps(packageName)
            if (searchResults.isEmpty()) {
                Log.w(TAG, "No search results for $packageName")
                return@withContext emptyList()
            }
            
            // Find the best match
            val bestMatch = searchResults.find {
                it.packageName.equals(packageName, ignoreCase = true) ||
                it.packageName.contains(packageName.substringAfterLast("."), ignoreCase = true)
            } ?: searchResults.first()
            
            Log.d(TAG, "Best match for $packageName: ${bestMatch.name} -> ${bestMatch.downloadUrl}")
            
            val appUrl = bestMatch.downloadUrl
            val versionsUrl = "$appUrl/versions"
            
            Log.d(TAG, "Fetching versions from: $versionsUrl")
            
            val doc = createConnection(versionsUrl).get()
            val versions = parseVersionsFromDocument(doc, packageName)
            
            if (versions.isNotEmpty()) {
                Log.d(TAG, "Found ${versions.size} versions for $packageName")
                return@withContext versions
            }
            
            // Fallback: Try the main app page if versions page didn't work
            Log.d(TAG, "Trying main app page for versions")
            val appDoc = createConnection(appUrl).get()
            val appVersions = parseVersionsFromDocument(appDoc, packageName)
            
            Log.d(TAG, "Found ${appVersions.size} versions from app page for $packageName")
            appVersions
            
        } catch (e: Exception) {
            Log.e(TAG, "Get versions failed for $packageName: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Parse versions from a document using multiple selector strategies
     * Selectors based on https://github.com/kiber-io/apkd
     */
    private fun parseVersionsFromDocument(doc: Document, packageName: String): List<AppVersion> {
        val versions = mutableListOf<AppVersion>()
        
        // From apkd: Primary selector is a.ver_download_link with data attributes
        val verDownloadLinks = doc.select("a.ver_download_link")
        
        if (verDownloadLinks.isNotEmpty()) {
            Log.d(TAG, "Found ${verDownloadLinks.size} ver_download_link items for $packageName")
            
            verDownloadLinks.take(30).forEach { link ->
                try {
                    val apkId = link.attr("data-dt-apkid")
                    // Only process APK files (not XAPK/bundles for now)
                    if (apkId.startsWith("b/APK")) {
                        val versionName = link.attr("data-dt-version")
                        val versionCodeStr = link.attr("data-dt-versioncode")
                        val fileSizeStr = link.attr("data-dt-filesize")
                        
                        // Get update date from span.update-on
                        val updateDateElement = link.select("span.update-on").first()
                        val updateDate = updateDateElement?.text()?.trim() ?: ""
                        
                        val versionCode = versionCodeStr.toIntOrNull() ?: 0
                        val fileSize = fileSizeStr.toLongOrNull() ?: 0L
                        val fileSizeStr2 = if (fileSize > 0) formatFileSize(fileSize) else ""
                        
                        // From apkd: Direct download URL pattern
                        // https://d.apkpure.com/b/APK/{pkg}?versionCode={version_code}
                        val directDownloadUrl = "https://d.apkpure.com/b/APK/$packageName?versionCode=$versionCode"
                        
                        if (versionName.isNotEmpty()) {
                            versions.add(
                                AppVersion(
                                    packageName = packageName,
                                    versionName = versionName,
                                    versionCode = versionCode,
                                    downloadPageUrl = directDownloadUrl, // Use direct download URL!
                                    downloadUrl = directDownloadUrl,
                                    source = "APKPure",
                                    size = fileSizeStr2,
                                    uploadDate = updateDate
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing ver_download_link: ${e.message}")
                }
            }
        }
        
        // Fallback to other selectors if primary didn't work
        if (versions.isEmpty()) {
            val listItems = doc.select("ul.ver-wrap li").ifEmpty {
                doc.select(".ver-item").ifEmpty {
                    doc.select("div.ver").ifEmpty {
                        doc.select("div.version-item").ifEmpty {
                            doc.select("a[href*=download]").ifEmpty {
                                doc.select("div[class*=version], li[class*=version]").ifEmpty {
                                    doc.select(".apk-info-wrap")
                                }
                            }
                        }
                    }
                }
            }
            
            Log.d(TAG, "Found ${listItems.size} version items (fallback) for $packageName")

            listItems.take(30).forEach { item ->
                try {
                    val link = item.select("a[href]").first()
                    
                    val verName = item.select(".ver-item-n").text().ifEmpty {
                        item.select(".ver-info-top").text().ifEmpty {
                            item.select(".version").text().ifEmpty {
                                item.select("span[class*=ver]").text().ifEmpty {
                                    link?.text() ?: ""
                                }
                            }
                        }
                    }
                    
                    val verInfo = item.select(".ver-item-s").text().ifEmpty {
                        item.select(".ver-info-m").text().ifEmpty {
                            item.select(".size").text().ifEmpty {
                                item.select("span[class*=size]").text()
                            }
                        }
                    }
                    
                    if (link != null) {
                        val href = link.attr("href")
                        
                        val finalVersionName = verName
                            .replace("Download", "")
                            .replace("APK", "")
                            .replace("XAPK", "")
                            .trim()
                            .ifEmpty { 
                                href.split("/").lastOrNull { it.matches(Regex(".*\\d+\\.\\d+.*")) } ?: ""
                            }
                        
                        if (href.isNotEmpty() && finalVersionName.isNotEmpty() && !href.contains("javascript", ignoreCase = true)) {
                            val fullUrl = if (href.startsWith("http")) href else "$BASE_URL$href"
                            
                            versions.add(
                                AppVersion(
                                    packageName = packageName,
                                    versionName = finalVersionName,
                                    versionCode = 0,
                                    downloadPageUrl = fullUrl,
                                    source = "APKPure",
                                    size = verInfo
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing version item: ${e.message}")
                }
            }
        }
        
        return versions
    }
    
    /**
     * Format file size from bytes to human readable format
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> String.format("%.2f GB", bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> String.format("%.2f MB", bytes / 1_000_000.0)
            bytes >= 1_000 -> String.format("%.2f KB", bytes / 1_000.0)
            else -> "$bytes B"
        }
    }

    /**
     * Get download URL for a specific version (deprecated - use getDownloadUrlFromPage)
     * @deprecated Use getDownloadUrlFromPage(version.downloadPageUrl) instead.
     * The downloadPageUrl is available in AppVersion objects returned by getAppVersions().
     */
    @Deprecated(
        message = "Use getDownloadUrlFromPage(version.downloadPageUrl) instead. " +
                  "The downloadPageUrl is available in AppVersion objects returned by getAppVersions().",
        replaceWith = ReplaceWith("getDownloadUrlFromPage(version.downloadPageUrl)")
    )
    suspend fun getDownloadUrl(packageName: String, versionName: String): String? = withContext(Dispatchers.IO) {
        // This method is kept for compatibility but returns null
        // The ViewModel should use getDownloadUrlFromPage with the downloadPageUrl from AppVersion
        Log.w(TAG, "Deprecated getDownloadUrl called - use getDownloadUrlFromPage(version.downloadPageUrl) instead")
        null
    }
    
    /**
     * Get download URL from the version page
     * If the URL is already a direct download URL (d.apkpure.com), returns it directly.
     * Otherwise navigates through APKPure's download process to get the actual APK URL.
     */
    suspend fun getDownloadUrlFromPage(pageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting download URL from: $pageUrl")
            
            // From apkd: Direct download URLs use d.apkpure.com domain
            // If URL is already a direct download URL, return it
            if (pageUrl.contains("d.apkpure.com") || pageUrl.contains("download.apkpure.com")) {
                Log.d(TAG, "URL is already a direct download URL: $pageUrl")
                return@withContext pageUrl
            }
            
            val doc = createConnection(pageUrl).get()
            
            // Try multiple selectors for the download button/link
            val downloadLink = doc.select("#download_link").attr("href").ifEmpty {
                doc.select("a.download-start-btn").attr("href").ifEmpty {
                    doc.select("a.da[href]").attr("href").ifEmpty {
                        doc.select("a.btn-download[href]").attr("href").ifEmpty {
                            doc.select("a[href*=download][class*=btn]").attr("href").ifEmpty {
                                doc.select("a[href*=.apk], a[href*=.xapk]").attr("href").ifEmpty {
                                    doc.select("a[data-dt-apkid]").attr("href")
                                }
                            }
                        }
                    }
                }
            }
            
            if (downloadLink.isNotEmpty()) {
                val finalUrl = if (downloadLink.startsWith("http")) downloadLink 
                               else if (downloadLink.startsWith("//")) "https:$downloadLink"
                               else "$BASE_URL$downloadLink"
                Log.d(TAG, "Found download URL: $finalUrl")
                return@withContext finalUrl
            }
            
            // If direct link not found, try to find a download page link and follow it
            val downloadPageLink = doc.select("a[href*=download]").attr("href")
            if (downloadPageLink.isNotEmpty() && !downloadPageLink.contains("javascript", ignoreCase = true)) {
                val downloadPageUrl = if (downloadPageLink.startsWith("http")) downloadPageLink else "$BASE_URL$downloadPageLink"
                Log.d(TAG, "Following download page: $downloadPageUrl")
                
                val downloadDoc = createConnection(downloadPageUrl).get()
                val finalLink = downloadDoc.select("#download_link, a.download-start-btn, a.da").attr("href")
                
                if (finalLink.isNotEmpty()) {
                    val finalUrl = if (finalLink.startsWith("http")) finalLink 
                                   else if (finalLink.startsWith("//")) "https:$finalLink"
                                   else "$BASE_URL$finalLink"
                    Log.d(TAG, "Found download URL from download page: $finalUrl")
                    return@withContext finalUrl
                }
            }
            
            Log.w(TAG, "Could not find download URL for: $pageUrl")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Download link failed for $pageUrl: ${e.message}", e)
            null
        }
    }
}
