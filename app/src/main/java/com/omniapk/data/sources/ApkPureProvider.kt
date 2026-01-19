package com.omniapk.data.sources

import com.omniapk.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import javax.inject.Inject

class ApkPureProvider @Inject constructor() : SourceProvider {
    override val name: String = "APKPure"
    private val baseUrl = "https://apkpure.com"

    override suspend fun searchApp(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        val results = mutableListOf<AppInfo>()
        try {
            val searchUrl = "$baseUrl/search?q=$query"
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()

            // APKPure search results are often in a 'dl' list with class 'search-dl'
            val items = doc.select("dl.search-dl")

            for (item in items) {
                // Icon
                val iconUrl = item.select("dt img").attr("src")
                
                // Title and Link
                val titleLink = item.selectFirst("p.search-title a") ?: item.selectFirst("dd.app-name a")
                
                if (titleLink != null) {
                    val name = titleLink.text()
                    val href = titleLink.attr("href") // e.g., /whatsapp-messenger/com.whatsapp
                    
                    // Package Name is often the last part of the URL or explicitly in a data attribute
                    // URL structure: https://apkpure.com/app-name/package.name
                    val packageName = href.substringAfterLast("/")
                    
                    // Version is often in a specific tag, e.g., 'span.ver' or just assumed 
                    // to be the latest.
                    
                    results.add(
                        AppInfo(
                            packageName = packageName,
                            name = name,
                            versionName = "Latest", // Placeholder
                            versionCode = 0,
                            icon = null, // We need to load this via Coil, but AppInfo expects Drawable. 
                                         // For now leaving null, in real app we'd load url.
                            isSystemApp = false
                        )
                    )
                }
            }
            
        } catch (e: IOException) {
            e.printStackTrace()
        }
        results
    }

    override suspend fun checkUpdate(packageName: String, currentVersionCode: Long, currentVersionName: String): AppInfo? {
         // APKPure search by package name
         val results = searchApp(packageName)
         return results.firstOrNull()
    }

    override suspend fun getDownloadUrl(appInfo: AppInfo): String? {
        return null
    }
}
