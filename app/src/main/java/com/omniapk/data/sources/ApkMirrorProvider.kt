package com.omniapk.data.sources

import com.omniapk.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import javax.inject.Inject

class ApkMirrorProvider @Inject constructor() : SourceProvider {
    override val name: String = "APKMirror"
    private val baseUrl = "https://www.apkmirror.com"

    override suspend fun searchApp(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        val results = mutableListOf<AppInfo>()
        try {
            // "searchtype=apk" filters for APKs, excluding bundles if preferred, but usually we want all.
            val searchUrl = "$baseUrl/?post_type=app_release&searchtype=apk&s=$query"
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000)
                .get()

            // APKMirror typically lists results in 'div.appRow' inside 'div.listWidget'
            val rows = doc.select("div.appRow")

            for (row in rows) {
                // Title and Link
                val titleElement = row.selectFirst("h5.appRowTitle a") ?: continue
                val name = titleElement.text()
                val detailsUrl = baseUrl + titleElement.attr("href")
                
                // Icon (often lazy loaded, but usually in an img tag)
                // APKMirror icons are complex, sometimes in a span or div. 
                // Let's try finding an img in the 'table-cell' equivalent.
                // val iconUrl = row.select("img").attr("src") // Placeholder for now

                // Version
                // The title often contains the version, e.g. "WhatsApp Messenger 2.23.1.76"
                // We can try to extract the version from the name.
                // Or look for 'span.infoSlide-value' if accessible.
                
                // Package Name: APKMirror search results often DON'T show package name explicitly 
                // until you go to the details page.
                // However, we need a unique ID. We can use the truncated name or try to guess.
                // For a robust implementation, we might need to visit the detail page, but that's slow.
                // We will use a placeholder package name or extract from title if possible.
                // Let's assume the query is close to the package name or use the sanitized title.
                
                val versionName = name.substringAfterLast(" ", "")
                val sanitizedName = name.substringBeforeLast(" ")

                // Creating a dummy package name because search results don't provide it easily.
                // In a real app, we might need a better strategy or a second request.
                val fakePackageName = "pkg.${name.filter { it.isLetterOrDigit() }.take(20)}"

                results.add(
                    AppInfo(
                        packageName = fakePackageName, // Placeholder
                        name = sanitizedName,
                        versionName = versionName,
                        versionCode = 0, // Unknown from list
                        icon = null,
                        isSystemApp = false
                    )
                )
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        results
    }

    override suspend fun checkUpdate(packageName: String, currentVersionCode: Long, currentVersionName: String): AppInfo? {
        // For APKMirror, we try to search by package name
        val results = searchApp(packageName)
        // If results found, return the first one (most likely match)
        return results.firstOrNull()
    }

    override suspend fun getDownloadUrl(appInfo: AppInfo): String? {
        return null
    }
}
