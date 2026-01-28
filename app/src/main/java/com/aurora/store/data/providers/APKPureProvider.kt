package com.aurora.store.data.providers

import android.content.Context
import android.util.Log
import com.aurora.store.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

/**
 * APKPure provider for app updates
 * Uses Jsoup for robust HTML parsing
 */
@Singleton
class APKPureProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val baseUrl = "https://apkpure.com"
    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    /**
     * Search for apps on APKPure
     */
    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            val searchUrl = "$baseUrl/search?q=${query.replace(" ", "+")}"
            val doc = Jsoup.connect(searchUrl)
                .userAgent(userAgent)
                .timeout(10000)
                .get()

            val results = mutableListOf<AppInfo>()
            // APKPure search results structure
            val items = doc.select(".search-res li dl, .first")

            items.take(20).forEach { item ->
                val link = item.select("a.first-info, a.dd").first()
                val icon = item.select("img").first()
                val title = item.select(".p1").text()
                
                if (link != null) {
                    val href = link.attr("href")
                    val iconUrl = icon?.attr("src") ?: ""
                    val name = if (title.isNotEmpty()) title else link.attr("title")
                    
                    // URL format: /app-name/package.name
                    val packageName = href.substringAfterLast("/")

                    results.add(
                        AppInfo(
                            packageName = packageName,
                            name = name,
                            versionName = "",
                            versionCode = 0,
                            iconUrl = iconUrl,
                            source = "APKPure",
                            downloadUrl = "$baseUrl$href"
                        )
                    )
                }
            }
            results
        } catch (e: Exception) {
            Log.e("APKPure", "Search failed: ${e.message}")
            emptyList()
        }
    }

    /**
     * Get available versions for a specific app
     */
    suspend fun getAppVersions(packageName: String): List<AppVersion> = withContext(Dispatchers.IO) {
        try {
            // Check if we need to search first to find the correct URL slug
            // But assume we have package name, APKPure often supports /package/versions
            // Or /app-slug/versions. Let's try direct construction construction first.
            
            // Strategy: Search for the package to get the correct slug URL
            // because APKPure uses /nice-app-name/com.package.name format
            val searchResults = searchApps(packageName)
            if (searchResults.isEmpty()) {
                Log.d("APKPure", "No search results for $packageName")
                return@withContext emptyList()
            }
            
            val appUrl = searchResults.first().downloadUrl // e.g., https://apkpure.com/instagram/com.instagram.android
            val versionsUrl = "$appUrl/versions"
            
            Log.d("APKPure", "Fetching versions from: $versionsUrl")
            
            val doc = Jsoup.connect(versionsUrl)
                .userAgent(userAgent)
                .timeout(15000)
                .followRedirects(true)
                .get()

            val versions = mutableListOf<AppVersion>()
            
            // Try multiple selectors for versions list - APKPure structure varies
            val listItems = doc.select("ul.ver-wrap li").ifEmpty {
                doc.select(".ver-item").ifEmpty {
                    doc.select("div.ver").ifEmpty {
                        doc.select("a[href*=download]").ifEmpty {
                            // Try finding version rows by common patterns
                            doc.select("div[class*=version], li[class*=version]")
                        }
                    }
                }
            }
            
            Log.d("APKPure", "Found ${listItems.size} version items")

            listItems.take(20).forEach { item ->
                val link = item.select("a[href]").first()
                
                // Try multiple version name selectors
                val verName = item.select(".ver-item-n").text().ifEmpty {
                    item.select(".ver-info-top").text().ifEmpty {
                        item.select("span[class*=ver]").text().ifEmpty {
                            link?.text() ?: ""
                        }
                    }
                }
                
                // Try multiple size selectors
                val verInfo = item.select(".ver-item-s").text().ifEmpty {
                    item.select(".ver-info-m").text().ifEmpty {
                        item.select("span[class*=size]").text()
                    }
                }
                
                if (link != null) {
                    val href = link.attr("href")
                    val finalVersionName = verName.replace("Download", "").trim().ifEmpty { 
                        // Try to extract version from href
                        href.split("/").lastOrNull { it.matches(Regex(".*\\d.*")) } ?: "Unknown"
                    }
                    
                    if (href.isNotEmpty() && finalVersionName != "Unknown") {
                        versions.add(
                            AppVersion(
                                packageName = packageName,
                                versionName = finalVersionName,
                                versionCode = 0,
                                downloadPageUrl = if (href.startsWith("http")) href else "$baseUrl$href",
                                source = "APKPure",
                                size = verInfo
                            )
                        )
                    }
                }
            }
            
            Log.d("APKPure", "Parsed ${versions.size} versions for $packageName")
            versions
        } catch (e: Exception) {
            Log.e("APKPure", "Get versions failed: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get download URL for a specific version
     */
    suspend fun getDownloadUrl(packageName: String, versionName: String): String? = withContext(Dispatchers.IO) {
        try {
            // Note: versionName parameter here might be missleading if we use the object passed from list
            // We should use the downloadPageUrl from the AppVersion object
            // But the interface signature is fixed: (packageName, versionName)
            // This is a design flaw in the ViewModel calling this.
            // However, we can re-implement this method to search or use a known pattern.
            
            // BETTER APPROACH: Since we can't change the signature easily without breaking other things,
            // let's try to construct the download URL if possible.
            // But `AppVersion` passed to ViewModel has `downloadPageUrl`.
            // The ViewModel calls `apkPureProvider.getDownloadUrl(version.packageName, version.versionName)`
            // BUT it should call a method that takes the URL.
            
            // I will overlook the signature for a moment. The ViewModel `AlternativeDownloadViewModel` calls:
            // apkPureProvider.getDownloadUrl(version.packageName, version.versionName)
            // It DOES NOT pass the url. This is bad.
            // I should update the ViewModel to pass the URL or update this method to use the URL if I can find it.
            // But I effectively can't find the URL just from name.
            
            // Let's assume the ViewModel calls this with the intention of "Download this specific version".
            // Since we updated `loadAPKPureVersions` to return `AppVersion` with `downloadPageUrl`,
            // we should really update the ViewModel to use that URL.
            
            // For now, I will implement a "best effort" search for the download link
            // Or I will update ViewModel to pass the URL. Updating ViewModel is safer.
            
            null // Placeholder, will fix ViewModel to call a new method `getDownloadUrl(url)`
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // New method to be called from ViewModel
    suspend fun getDownloadUrlFromPage(pageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(pageUrl)
                .userAgent(userAgent)
                .timeout(10000)
                .get()
                
            // Find download button
            // Usually #download_link or .download-start-btn
            val downloadLink = doc.select("#download_link").attr("href")
            if (downloadLink.isNotEmpty()) return@withContext downloadLink
            
            val altLink = doc.select("a.download-start-btn").attr("href")
            if (altLink.isNotEmpty()) return@withContext altLink
            
            null
        } catch (e: Exception) {
            Log.e("APKPure", "Download link failed: ${e.message}")
            null
        }
    }

    companion object {
        const val SOURCE_NAME = "APKPure"
    }
}
