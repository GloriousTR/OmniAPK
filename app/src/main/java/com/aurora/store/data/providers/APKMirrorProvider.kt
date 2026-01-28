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
 * APKMirror provider for app updates
 * Uses Jsoup for robust HTML parsing
 */
@Singleton
class APKMirrorProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val baseUrl = "https://www.apkmirror.com"
    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    /**
     * Search for apps on APKMirror
     */
    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            val searchUrl = "$baseUrl/?post_type=app_release&searchtype=apk&s=${query.replace(" ", "+")}"
            val doc = Jsoup.connect(searchUrl)
                .userAgent(userAgent)
                .timeout(10000)
                .get()

            val results = mutableListOf<AppInfo>()
            val appRows = doc.select("div.appRow")

            appRows.take(20).forEach { row ->
                val titleElement = row.select("h5.appRowTitle a").first()
                val iconElement = row.select("img.img-fluid").first()
                
                if (titleElement != null) {
                    val name = titleElement.text().trim()
                    val href = titleElement.attr("href")
                    val iconUrl = iconElement?.attr("src")?.let { if (it.startsWith("//")) "https:$it" else it } ?: ""
                    
                    // Extract package name from URL strictly
                    // URL format: /apk/developer-name/app-name/
                    // This is tricky on APKMirror as URL doesn't always contain clean package name
                    // We will use the URL part as ID
                    val packageName = href.substringAfter("/apk/").trim('/').replace("/", ".")

                    results.add(
                        AppInfo(
                            packageName = packageName,
                            name = name,
                            versionName = "",
                            versionCode = 0,
                            iconUrl = iconUrl,
                            source = "APKMirror",
                            downloadUrl = "$baseUrl$href"
                        )
                    )
                }
            }
            results
        } catch (e: Exception) {
            Log.e("APKMirror", "Search failed: ${e.message}")
            emptyList()
        }
    }

    /**
     * Get available versions for a specific app
     */
    suspend fun getAppVersions(packageName: String): List<AppVersion> = withContext(Dispatchers.IO) {
        try {
            // Try to construct URL from package name, but APKMirror uses developer-app-name format
            // This might fail if packageName is actually a package id like com.example.app
            // We assume packageName passed here is actually the slug from search result for better success
            val url = if (packageName.contains(".")) {
                 // Fallback search if we have a dot-separated package name
                 return@withContext searchVersionsByPackageName(packageName)
            } else {
                 "$baseUrl/apk/$packageName/"
            }

            val doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(15000)
                .followRedirects(true)
                .get()

            val versions = mutableListOf<AppVersion>()
            
            // Try multiple selectors for version list - APKMirror changes structure frequently
            val rows = doc.select("div.listWidget div.appRow").ifEmpty { 
                doc.select(".table-row").ifEmpty {
                    doc.select("div.appRowVariantTag").ifEmpty {
                        doc.select("div[class*=appRow]")
                    }
                }
            }

            rows.take(20).forEach { row ->
                // Try multiple link selectors
                val link = row.select("a.fontBlack").first()
                    ?: row.select("a[class*=fontBlack]").first()
                    ?: row.select("h5.appRowTitle a").first()
                    ?: row.select("a[href*=download]").first()
                    
                val dateInfo = row.select(".dateyear_utc").text().ifEmpty { 
                    row.select(".dateyear_role").text().ifEmpty {
                        row.select("[class*=date]").text()
                    }
                }
                
                if (link != null) {
                    val versionName = link.text().replace("Download", "").trim()
                    val href = link.attr("href")
                    
                    if (versionName.isNotEmpty() && href.isNotEmpty()) {
                        versions.add(
                            AppVersion(
                                packageName = packageName, // Keep the slug
                                versionName = versionName,
                                versionCode = 0,
                                downloadPageUrl = if (href.startsWith("http")) href else "$baseUrl$href",
                                source = "APKMirror",
                                uploadDate = dateInfo
                            )
                        )
                    }
                }
            }
            
            Log.d("APKMirror", "Found ${versions.size} versions for $packageName")
            versions
        } catch (e: Exception) {
            Log.e("APKMirror", "Get versions failed: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun searchVersionsByPackageName(packageName: String): List<AppVersion> {
        try {
            // Fallback: search for package name to find the app page
            val searchResults = searchApps(packageName)
            if (searchResults.isNotEmpty()) {
                val bestMatch = searchResults.first()
                val slug = bestMatch.downloadUrl.substringAfter("/apk/").trimEnd('/').split("/").take(2).joinToString("/")
                // Try to get versions page directly
                val versionsUrl = "$baseUrl/apk/$slug/versions/"
                
                try {
                    val doc = Jsoup.connect(versionsUrl)
                        .userAgent(userAgent)
                        .timeout(15000)
                        .followRedirects(true)
                        .get()
                        
                    val versions = mutableListOf<AppVersion>()
                    val rows = doc.select("div.listWidget div.appRow").ifEmpty { 
                        doc.select(".table-row").ifEmpty {
                            doc.select("div[class*=appRow]")
                        }
                    }
                    
                    rows.take(20).forEach { row ->
                        val link = row.select("a.fontBlack").first()
                            ?: row.select("a[class*=fontBlack]").first()
                            ?: row.select("h5.appRowTitle a").first()
                            
                        val dateInfo = row.select(".dateyear_utc").text().ifEmpty { 
                            row.select(".dateyear_role").text()
                        }
                        
                        if (link != null) {
                            val versionName = link.text().replace("Download", "").trim()
                            val href = link.attr("href")
                            
                            if (versionName.isNotEmpty() && href.isNotEmpty()) {
                                versions.add(
                                    AppVersion(
                                        packageName = packageName,
                                        versionName = versionName,
                                        versionCode = 0,
                                        downloadPageUrl = if (href.startsWith("http")) href else "$baseUrl$href",
                                        source = "APKMirror",
                                        uploadDate = dateInfo
                                    )
                                )
                            }
                        }
                    }
                    
                    if (versions.isNotEmpty()) {
                        Log.d("APKMirror", "Found ${versions.size} versions via search for $packageName")
                        return versions
                    }
                } catch (e: Exception) {
                    Log.e("APKMirror", "Versions page failed for $packageName: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("APKMirror", "Search versions failed for $packageName: ${e.message}")
        }
        return emptyList()
    }

    /**
     * Get download URL for a specific version
     */
    suspend fun getDownloadUrl(versionPageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            var doc = Jsoup.connect(versionPageUrl).userAgent(userAgent).get()
            
            // Step 1: We are on version page, need to find variant or direct download
            // APKMirror often lists variants (verified, beta, etc.)
            // We'll pick the first available downloadable variant
            var nextLink = doc.select("a:contains(Download APK)").attr("href")
            
            if (nextLink.isEmpty()) {
                 // Check for "SEE AVAILABLE APKS" and follow
                 val variantRow = doc.select(".table-row").firstOrNull { it.select("a").isNotEmpty() }
                 if (variantRow != null) {
                     val variantHref = variantRow.select("a.accent_color").attr("href")
                     if (variantHref.isNotEmpty()) {
                         doc = Jsoup.connect("$baseUrl$variantHref").userAgent(userAgent).get()
                         nextLink = doc.select("a:contains(Download APK)").attr("href")
                     }
                 }
            }
            
            if (nextLink.isNotEmpty()) {
                // Step 2: "Download APK" page
                doc = Jsoup.connect("$baseUrl$nextLink").userAgent(userAgent).get()
                
                // Step 3: Find the final download link or button "Your download will start immediately"
                // Usually it's a link with rel="nofollow" and class "accent_btn"
                val finalLink = doc.select("a.accent_bg.btn").attr("href")
                if (finalLink.isNotEmpty()) return@withContext "$baseUrl$finalLink"
                
                // Fallback: try to find the direct link in the "here" text
                val directLink = doc.select("a:contains(here)").attr("href")
                if (directLink.isNotEmpty()) return@withContext "$baseUrl$directLink"
            }
            
            null
        } catch (e: Exception) {
            Log.e("APKMirror", "Get download URL failed: ${e.message}")
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
