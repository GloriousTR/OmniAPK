package com.omniapk.data.sources

import android.content.Context
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.Category
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.data.providers.DeviceInfoProvider
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.CategoryHelper
import com.aurora.gplayapi.helpers.HomeHelper
import com.aurora.gplayapi.helpers.SearchHelper
import com.omniapk.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Provider using GPlayApi (Aurora Store's library)
 * Supports anonymous and authenticated access
 */
@Singleton
class GooglePlayProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : SourceProvider {
    
    override val name: String = "Google Play"
    
    private var authData: AuthData? = null
    private val httpClient = OkHttpClient()
    
    // Token Dispenser URL (Aurora's public server for anonymous tokens)
    private val tokenDispenserUrl = "https://auroraoss.com/api/auth"
    
    /**
     * Initialize with anonymous authentication
     */
    suspend fun initAnonymous(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Get device properties
            val properties = DeviceInfoProvider.DEFAULT_DEVICE_PROPERTIES
            
            // Request anonymous auth token from dispenser
            val request = Request.Builder()
                .url(tokenDispenserUrl)
                .header("User-Agent", "AuroraStore/4.0")
                .build()
            
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { body ->
                    val json = JSONObject(body)
                    authData = AuthData(
                        email = json.optString("email", ""),
                        authToken = json.optString("authToken", ""),
                        gsfId = json.optString("gsfId", ""),
                        tokenType = json.optInt("tokenType", 0),
                        isAnonymous = true,
                        locale = java.util.Locale.getDefault(),
                        deviceInfoProvider = DeviceInfoProvider(properties, java.util.Locale.getDefault().toString())
                    )
                    true
                } ?: false
            } else {
                android.util.Log.e("GooglePlayProvider", "Token dispenser failed: ${response.code}")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("GooglePlayProvider", "Init failed", e)
            false
        }
    }
    
    /**
     * Check if authenticated
     */
    fun isAuthenticated(): Boolean = authData != null
    
    /**
     * Search for apps
     */
    override suspend fun searchApp(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        if (authData == null) {
            initAnonymous()
        }
        
        authData?.let { auth ->
            try {
                val searchHelper = SearchHelper(auth)
                val results = searchHelper.searchResults(query)
                results.map { it.toAppInfo() }
            } catch (e: Exception) {
                android.util.Log.e("GooglePlayProvider", "Search failed", e)
                emptyList()
            }
        } ?: emptyList()
    }
    
    /**
     * Get app details
     */
    suspend fun getAppDetails(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        if (authData == null) {
            initAnonymous()
        }
        
        authData?.let { auth ->
            try {
                val detailsHelper = AppDetailsHelper(auth)
                val app = detailsHelper.getAppByPackageName(packageName)
                app.toAppInfo()
            } catch (e: Exception) {
                android.util.Log.e("GooglePlayProvider", "Details failed", e)
                null
            }
        }
    }
    
    /**
     * Get home stream (featured apps)
     */
    suspend fun getHomeStream(type: HomeHelper.Type = HomeHelper.Type.HOME): List<StreamCluster> = withContext(Dispatchers.IO) {
        if (authData == null) {
            initAnonymous()
        }
        
        authData?.let { auth ->
            try {
                val homeHelper = HomeHelper(auth)
                homeHelper.getHome(type)
            } catch (e: Exception) {
                android.util.Log.e("GooglePlayProvider", "Home stream failed", e)
                emptyList()
            }
        } ?: emptyList()
    }
    
    /**
     * Get categories
     */
    suspend fun getCategories(type: Category.Type = Category.Type.APPLICATION): List<Category> = withContext(Dispatchers.IO) {
        if (authData == null) {
            initAnonymous()
        }
        
        authData?.let { auth ->
            try {
                val categoryHelper = CategoryHelper(auth)
                categoryHelper.getAllCategories(type)
            } catch (e: Exception) {
                android.util.Log.e("GooglePlayProvider", "Categories failed", e)
                emptyList()
            }
        } ?: emptyList()
    }
    
    /**
     * Get top apps
     */
    suspend fun getTopApps(isGame: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        val type = if (isGame) HomeHelper.Type.GAMES else HomeHelper.Type.HOME
        val clusters = getHomeStream(type)
        
        clusters.flatMap { cluster ->
            cluster.clusterAppList.map { it.toAppInfo() }
        }.take(30)
    }
    
    override suspend fun checkUpdate(packageName: String, currentVersionCode: Long, currentVersionName: String): AppInfo? {
        val details = getAppDetails(packageName) ?: return null
        return if (details.versionCode > currentVersionCode) details else null
    }
    
    override suspend fun getDownloadUrl(appInfo: AppInfo): String? {
        // GPlayApi handles downloads differently - returns file list
        // For now, return null and handle in download manager
        return null
    }
    
    /**
     * Extension to convert GPlayApi App to our AppInfo
     */
    private fun App.toAppInfo(): AppInfo = AppInfo(
        packageName = this.packageName,
        name = this.displayName,
        versionName = this.versionName,
        versionCode = this.versionCode.toLong(),
        icon = null,
        iconUrl = this.iconArtwork.url,
        isSystemApp = false,
        isGame = this.categoryId.contains("GAME", ignoreCase = true),
        source = name,
        description = this.shortDescription,
        screenshots = this.screenshotUrls,
        rating = this.rating.average.toDouble(),
        downloads = this.installs.toString(),
        size = "${this.size / (1024 * 1024)} MB"
    )
}
