package com.omniapk.data.sources

import com.omniapk.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder
import javax.inject.Inject

/**
 * F-Droid source provider for searching and downloading open-source apps.
 * Uses web scraping since F-Droid's API is not publicly documented.
 */
class FDroidProvider @Inject constructor() : SourceProvider {
    override val name: String = "F-Droid"
    
    private val baseUrl = "https://f-droid.org"
    private val searchUrl = "https://search.f-droid.org"
    
    override suspend fun searchApp(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$searchUrl/?q=$encodedQuery&lang=en"
            
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get()
            
            val apps = mutableListOf<AppInfo>()
            val results = doc.select("a.package-header")
            
            for (result in results.take(15)) {
                try {
                    val href = result.attr("href")
                    val packageName = href.substringAfterLast("/packages/").substringBefore("/")
                    val nameElement = result.selectFirst(".package-name")
                    val summaryElement = result.selectFirst(".package-summary")
                    val iconElement = result.selectFirst("img.package-icon")
                    
                    val appName = nameElement?.text() ?: continue
                    val summary = summaryElement?.text() ?: ""
                    val iconUrl = iconElement?.attr("src") ?: ""
                    
                    if (packageName.isNotEmpty()) {
                        apps.add(
                            AppInfo(
                                packageName = packageName,
                                name = appName,
                                versionName = "",
                                versionCode = 0,
                                icon = null,
                                iconUrl = iconUrl,
                                source = name,
                                description = summary
                            )
                        )
                    }
                } catch (e: Exception) {
                    continue
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
            val url = "$baseUrl/en/packages/$packageName/"
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get()
            
            val versionElement = doc.selectFirst(".package-version-header .package-version")
            val latestVersion = versionElement?.text()?.trim() ?: return@withContext null
            
            // Simple version comparison
            if (latestVersion != currentVersionName) {
                val nameElement = doc.selectFirst(".package-name")
                AppInfo(
                    packageName = packageName,
                    name = nameElement?.text() ?: packageName,
                    versionName = latestVersion,
                    versionCode = 0,
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
            val url = "$baseUrl/en/packages/${appInfo.packageName}/"
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get()
            
            // Find the first APK download link
            val downloadLink = doc.selectFirst("a.package-version-download[href*='.apk']")
            downloadLink?.attr("href")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get popular/featured apps from F-Droid
     */
    suspend fun getPopularApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("$baseUrl/en/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get()
            
            val apps = mutableListOf<AppInfo>()
            val latestApps = doc.select(".package-header")
            
            for (app in latestApps.take(10)) {
                try {
                    val href = app.attr("href")
                    val packageName = href.substringAfterLast("/packages/").substringBefore("/")
                    val nameElement = app.selectFirst(".package-name")
                    val iconElement = app.selectFirst("img")
                    
                    val appName = nameElement?.text() ?: continue
                    val iconUrl = iconElement?.attr("src") ?: ""
                    
                    if (packageName.isNotEmpty()) {
                        apps.add(
                            AppInfo(
                                packageName = packageName,
                                name = appName,
                                versionName = "",
                                versionCode = 0,
                                icon = null,
                                iconUrl = if (iconUrl.startsWith("/")) "$baseUrl$iconUrl" else iconUrl,
                                source = name
                            )
                        )
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            apps
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
