package com.omniapk.data.sources

import android.content.Context
import com.omniapk.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Provider (Mock implementation)
 * 
 * NOTE: GPlayApi library is temporarily disabled as it's not available on public Maven repos.
 * This provider returns placeholder data from popular apps.
 * 
 * TODO: Integrate real Google Play API when available
 */
@Singleton
class GooglePlayProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : SourceProvider {
    
    override val name: String = "Google Play"
    
    /**
     * Search for apps - returns matching placeholder apps
     */
    override suspend fun searchApp(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        val allApps = getPopularApps() + getPopularGames()
        allApps.filter { 
            it.name.contains(query, ignoreCase = true) ||
            it.packageName.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * Get top apps
     */
    suspend fun getTopApps(isGame: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        if (isGame) getPopularGames() else getPopularApps()
    }
    
    override suspend fun checkUpdate(packageName: String, currentVersionCode: Long, currentVersionName: String): AppInfo? {
        return null
    }
    
    override suspend fun getDownloadUrl(appInfo: AppInfo): String? {
        return null
    }
    
    /**
     * Popular apps placeholder data
     */
    private fun getPopularApps(): List<AppInfo> = listOf(
        AppInfo(
            packageName = "com.google.android.youtube",
            name = "YouTube",
            versionName = "19.0",
            versionCode = 1,
            source = name,
            description = "Watch and subscribe",
            iconUrl = "https://play-lh.googleusercontent.com/lMoItBgdPPVDJsNOVtP26EKHePkwBg-PkuY9NOrc-fumRtTFP4XhpUNk_22syN4Datc"
        ),
        AppInfo(
            packageName = "com.google.android.gm",
            name = "Gmail",
            versionName = "2024.01",
            versionCode = 1,
            source = name,
            description = "Email by Google",
            iconUrl = "https://play-lh.googleusercontent.com/KSuaRLiI_FlDP8cM4MzJ23ML3xwQTG0dkb2Z_rEmkkpGHPnDaYkGx1BmhA2Mv2U8L-3r"
        ),
        AppInfo(
            packageName = "com.google.android.apps.maps",
            name = "Google Maps",
            versionName = "11.100",
            versionCode = 1,
            source = name,
            description = "Navigate your world",
            iconUrl = "https://play-lh.googleusercontent.com/Kf8WTct65hOTKq7AfXs4g_SuUYK2mDaZ7GKFCDrdXNMaJRqTo88Z-kKxJ2bAJBMWkQ"
        ),
        AppInfo(
            packageName = "com.google.android.apps.photos",
            name = "Google Photos",
            versionName = "6.70",
            versionCode = 1,
            source = name,
            description = "Photo & video storage",
            iconUrl = "https://play-lh.googleusercontent.com/ZyWNGIfzUyoajtFcD7NhMksHEZh37f-MkHVGr5Yfr-kGsJI-VJebGJG-1kXXBLCso_E"
        ),
        AppInfo(
            packageName = "com.google.android.apps.translate",
            name = "Google Translate",
            versionName = "8.1",
            versionCode = 1,
            source = name,
            description = "Translate text and speech",
            iconUrl = "https://play-lh.googleusercontent.com/ZrNeuKthBirZN7rrXPN1JmUbaG8ICy3kZSHt-WgSnREsJzo2DOU2hNw1kkZqPGX9vHo"
        )
    )
    
    /**
     * Popular games placeholder data
     */
    private fun getPopularGames(): List<AppInfo> = listOf(
        AppInfo(
            packageName = "com.supercell.clashofclans",
            name = "Clash of Clans",
            versionName = "16.0",
            versionCode = 1,
            source = name,
            description = "Epic combat strategy",
            isGame = true,
            iconUrl = "https://play-lh.googleusercontent.com/LByrur1mTmPeNr0ljI-uAUcct1rzmTve5Esau1SwoAzjBXQUby6uHIfHbF9TAT51mgHm"
        ),
        AppInfo(
            packageName = "com.supercell.brawlstars",
            name = "Brawl Stars",
            versionName = "55.0",
            versionCode = 1,
            source = name,
            description = "Team battles",
            isGame = true,
            iconUrl = "https://play-lh.googleusercontent.com/bnRLwT8yLmLLV17FVgKXmMEr5xYxO2K1oWV7xS6T7KNZtm6vPY7kZ2xNMb2qF8D7vg"
        ),
        AppInfo(
            packageName = "com.kiloo.subwaysurf",
            name = "Subway Surfers",
            versionName = "3.20",
            versionCode = 1,
            source = name,
            description = "Endless runner",
            isGame = true,
            iconUrl = "https://play-lh.googleusercontent.com/dYAlHV2JYMaQ5Y7e8q-cJLJ-GV2xRLuODc7X0y6m6PY2sB5bCLMTGKNxDcdR4MdqJRM"
        ),
        AppInfo(
            packageName = "com.pubg.imobile",
            name = "PUBG MOBILE",
            versionName = "3.0",
            versionCode = 1,
            source = name,
            description = "Battle Royale",
            isGame = true,
            iconUrl = "https://play-lh.googleusercontent.com/JRd-v4zH_BZ7h4E5s3k4btB5E2p9mAdfJSxJ4_Y4UlGajP7S0qTJLM"
        ),
        AppInfo(
            packageName = "com.mojang.minecraftpe",
            name = "Minecraft",
            versionName = "1.20.50",
            versionCode = 1,
            source = name,
            description = "Build anything",
            isGame = true,
            iconUrl = "https://play-lh.googleusercontent.com/VSwHQjcAttxsLE47RuS4PqpC4LT7lCoSjE7Hx5AW_yCxtDvcnsHHvm5CTuL5BPN-uRTP"
        )
    )
}
