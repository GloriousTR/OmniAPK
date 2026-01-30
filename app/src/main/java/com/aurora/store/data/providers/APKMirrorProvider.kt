package com.aurora.store.data.providers

import android.content.Context
import android.util.Log
import com.aurora.store.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.Connection
import javax.inject.Inject
import javax.inject.Singleton

/**
 * APKMirror provider for app updates
 * Uses Jsoup for robust HTML parsing
 * Updated with improved selectors and anti-bot measures
 */
@Singleton
class APKMirrorProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "APKMirror"
        private const val BASE_URL = "https://www.apkmirror.com"
        private const val TIMEOUT = 20000
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
     * Search for apps on APKMirror
     */
    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            val searchUrl = "$BASE_URL/?post_type=app_release&searchtype=apk&s=${query.replace(" ", "+")}"
            Log.d(TAG, "Searching APKMirror: $searchUrl")
            
            val doc = createConnection(searchUrl).get()
            
            val results = mutableListOf<AppInfo>()
            
            // Try multiple selectors for app rows - APKMirror changes structure
            val appRows = doc.select("div.appRow").ifEmpty {
                doc.select("div.listWidget div.appRow").ifEmpty {
                    doc.select("[class*=appRow]").ifEmpty {
                        doc.select("div.content div.row")
                    }
                }
            }
            
            Log.d(TAG, "Found ${appRows.size} app rows in search results")

            appRows.take(20).forEach { row ->
                try {
                    // Try multiple selectors for title
                    val titleElement = row.select("h5.appRowTitle a").first()
                        ?: row.select("a.fontBlack").first()
                        ?: row.select("h5 a").first()
                        ?: row.select("a[href*=/apk/]").first()
                    
                    // Try multiple selectors for icon
                    val iconElement = row.select("img.img-fluid").first()
                        ?: row.select("img.lazyload").first()
                        ?: row.select("img[src*=logo]").first()
                        ?: row.select("img").first()
                    
                    if (titleElement != null) {
                        val name = titleElement.text().trim()
                        val href = titleElement.attr("href")
                        val iconUrl = iconElement?.let { img ->
                            val src = img.attr("data-src").ifEmpty { img.attr("src") }
                            if (src.startsWith("//")) "https:$src" else src
                        } ?: ""
                        
                        // Extract package name from URL
                        val packageName = href.substringAfter("/apk/").trim('/').replace("/", ".")
                        
                        if (name.isNotEmpty() && href.isNotEmpty()) {
                            results.add(
                                AppInfo(
                                    packageName = packageName,
                                    name = name,
                                    versionName = "",
                                    versionCode = 0,
                                    iconUrl = iconUrl,
                                    source = "APKMirror",
                                    downloadUrl = if (href.startsWith("http")) href else "$BASE_URL$href"
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing search row: ${e.message}")
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
     * Primary method - searches by package name and returns all available versions
     */
    suspend fun getAppVersions(packageName: String): List<AppVersion> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting versions for package: $packageName")
        
        try {
            // APKMirror uses developer-name/app-name format, not package names
            // So we always search first to find the correct app page
            return@withContext searchVersionsByPackageName(packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Get versions failed for $packageName: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Parse versions from a document using multiple selector strategies
     * Inspired by https://github.com/tanishqmanuja/apkmirror-downloader
     */
    private fun parseVersionsFromDocument(doc: Document, packageName: String): List<AppVersion> {
        val versions = mutableListOf<AppVersion>()
        
        // Check for CloudFlare protection
        if (doc.text().contains("Enable JavaScript and cookies to continue")) {
            Log.w(TAG, "CloudFlare protection detected for $packageName")
            return emptyList()
        }
        
        // Primary selector from apkmirror-downloader: .listWidget with all_versions anchor
        // Then fall back to other selectors
        val versionTable = doc.select(".listWidget:has(a[name=all_versions])").first()
        
        val rows = if (versionTable != null) {
            // Skip first 2 rows (header) and last row (more link) as per apkmirror-downloader
            val children = versionTable.children()
            if (children.size > 3) {
                children.subList(2, children.size - 1)
            } else {
                children.toList()
            }
        } else {
            // Fallback to other selectors
            doc.select("div.listWidget div.appRow").ifEmpty { 
                doc.select(".table-row").ifEmpty {
                    doc.select(".variants-table .table-row").ifEmpty {
                        doc.select("div.appRowVariantTag").ifEmpty {
                            doc.select("div[class*=appRow]").ifEmpty {
                                doc.select(".widgetRow")
                            }
                        }
                    }
                }
            }.toList()
        }
        
        Log.d(TAG, "Found ${rows.size} version rows for $packageName")
        
        rows.take(30).forEach { row ->
            try {
                // Try selector from apkmirror-downloader first: .table-cell a
                val link = row.select(".table-cell").getOrNull(1)?.select("a")?.first()
                    ?: row.select(".table-cell a").first()
                    ?: row.select("a.fontBlack").first()
                    ?: row.select("a[class*=fontBlack]").first()
                    ?: row.select("h5.appRowTitle a").first()
                    ?: row.select("a[href*=release]").first()
                    ?: row.select("a").first()
                
                // Extract date info
                val dateInfo = row.select(".dateyear_utc").text().ifEmpty { 
                    row.select(".dateyear_role").text().ifEmpty {
                        row.select("[class*=date]").text().ifEmpty {
                            row.select("span.colorLightBlack").text()
                        }
                    }
                }
                
                if (link != null) {
                    val rawText = link.text().trim()
                    val href = link.attr("href")
                    
                    // Extract version name - remove common prefixes/suffixes
                    val versionName = rawText
                        .replace("Download", "")
                        .replace("APK", "")
                        .replace("XAPK", "")
                        .trim()
                    
                    if (versionName.isNotEmpty() && href.isNotEmpty() && !href.contains("javascript", ignoreCase = true)) {
                        val fullUrl = if (href.startsWith("http")) href else "$BASE_URL$href"
                        
                        versions.add(
                            AppVersion(
                                packageName = packageName,
                                versionName = versionName,
                                versionCode = 0,
                                downloadPageUrl = fullUrl,
                                source = "APKMirror",
                                uploadDate = dateInfo
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing version row: ${e.message}")
            }
        }
        
        return versions
    }
    
    /**
     * Search for app and get versions from the app page
     */
    private suspend fun searchVersionsByPackageName(packageName: String): List<AppVersion> {
        try {
            Log.d(TAG, "Searching versions for: $packageName")
            
            // First, search for the app to find its APKMirror page
            val searchResults = searchApps(packageName)
            
            if (searchResults.isEmpty()) {
                Log.w(TAG, "No search results for $packageName")
                return emptyList()
            }
            
            // Find the best match (prefer exact package name match or first result)
            val bestMatch = searchResults.find { 
                it.packageName.contains(packageName.substringAfterLast("."), ignoreCase = true) ||
                packageName.contains(it.packageName.substringAfterLast("."), ignoreCase = true)
            } ?: searchResults.first()
            
            Log.d(TAG, "Best match for $packageName: ${bestMatch.name} -> ${bestMatch.downloadUrl}")
            
            // Build the app page URL
            val appPageUrl = bestMatch.downloadUrl
            
            // First, try to get versions from the app's versions page directly
            val slug = appPageUrl.substringAfter("/apk/").trimEnd('/')
            val versionsPageUrl = "$BASE_URL/uploads/?appcategory=$slug"
            
            Log.d(TAG, "Trying versions page: $versionsPageUrl")
            
            try {
                val versionsDoc = createConnection(versionsPageUrl).get()
                val versions = parseVersionsFromDocument(versionsDoc, packageName)
                
                if (versions.isNotEmpty()) {
                    Log.d(TAG, "Found ${versions.size} versions from versions page for $packageName")
                    return versions
                }
            } catch (e: Exception) {
                Log.w(TAG, "Versions page failed for $packageName, trying app page: ${e.message}")
            }
            
            // Fallback: Try the main app page
            try {
                val appDoc = createConnection(appPageUrl).get()
                val versions = parseVersionsFromDocument(appDoc, packageName)
                
                if (versions.isNotEmpty()) {
                    Log.d(TAG, "Found ${versions.size} versions from app page for $packageName")
                    return versions
                }
            } catch (e: Exception) {
                Log.w(TAG, "App page failed for $packageName: ${e.message}")
            }
            
            Log.w(TAG, "No versions found for $packageName")
            return emptyList()
            
        } catch (e: Exception) {
            Log.e(TAG, "Search versions failed for $packageName: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * Get download URL for a specific version
     * Navigates through APKMirror's multi-step download process
     * Selectors inspired by https://github.com/tanishqmanuja/apkmirror-downloader
     */
    suspend fun getDownloadUrl(versionPageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting download URL from: $versionPageUrl")
            
            var doc = createConnection(versionPageUrl).get()
            
            // Check for CloudFlare protection
            if (doc.text().contains("Enable JavaScript and cookies to continue")) {
                Log.w(TAG, "CloudFlare protection detected")
                return@withContext null
            }
            
            // Step 1: We are on version page, might need to select a variant first
            // Check if there's a variants table (.variants-table)
            val variantsTable = doc.select(".variants-table").first()
            
            var downloadButtonUrl: String? = null
            
            if (variantsTable != null) {
                // Parse variants table and pick the first suitable one
                val variantRows = variantsTable.children().drop(1) // Skip header row
                
                for (row in variantRows) {
                    // From apkmirror-downloader: variant download link is in the 5th cell
                    val variantLink = row.select(".table-cell").getOrNull(4)?.select("a")?.attr("href")
                        ?: row.select(".table-cell a").attr("href")
                        ?: row.select("a").attr("href")
                    
                    if (variantLink.isNotEmpty() && !variantLink.contains("javascript", ignoreCase = true)) {
                        Log.d(TAG, "Found variant link: $variantLink")
                        val variantUrl = if (variantLink.startsWith("http")) variantLink else "$BASE_URL$variantLink"
                        doc = createConnection(variantUrl).get()
                        break
                    }
                }
            }
            
            // Step 2: Find "Download APK" button
            // From apkmirror-downloader: selector is `a.downloadButton`
            downloadButtonUrl = doc.select("a.downloadButton").attr("href").ifEmpty {
                doc.select("a:contains(Download APK)").attr("href").ifEmpty {
                    doc.select("a[href*=-download]").attr("href")
                }
            }
            
            if (downloadButtonUrl.isNullOrEmpty()) {
                Log.w(TAG, "Could not find download button for: $versionPageUrl")
                return@withContext null
            }
            
            // Step 3: Navigate to download button page
            val downloadPageUrl = if (downloadButtonUrl.startsWith("http")) downloadButtonUrl else "$BASE_URL$downloadButtonUrl"
            Log.d(TAG, "Navigating to download page: $downloadPageUrl")
            doc = createConnection(downloadPageUrl).get()
            
            // Step 4: Find the final download link
            // From apkmirror-downloader: selector is `.card-with-tabs a[href]`
            val finalLink = doc.select(".card-with-tabs a[href]").attr("href").ifEmpty {
                doc.select("a.accent_bg.btn[href]").attr("href").ifEmpty {
                    doc.select("a[rel=nofollow][href*=download]").attr("href").ifEmpty {
                        doc.select("a:contains(here)[href]").attr("href").ifEmpty {
                            doc.select("a[href*=.apk]").attr("href")
                        }
                    }
                }
            }
            
            if (finalLink.isNotEmpty()) {
                val downloadUrl = if (finalLink.startsWith("http")) finalLink else "$BASE_URL$finalLink"
                Log.d(TAG, "Found download URL: $downloadUrl")
                return@withContext downloadUrl
            }
            
            Log.w(TAG, "Could not find final download URL for: $versionPageUrl")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Get download URL failed for $versionPageUrl: ${e.message}", e)
            null
        }
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
