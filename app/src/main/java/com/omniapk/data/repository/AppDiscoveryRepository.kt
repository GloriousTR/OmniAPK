package com.omniapk.data.repository

import com.omniapk.data.model.AppCategory
import com.omniapk.data.model.AppInfo
import com.omniapk.data.model.Categories
import com.omniapk.data.model.FeaturedSection
import com.omniapk.data.model.TopChart
import com.omniapk.data.sources.FDroidProvider
import com.omniapk.data.sources.GooglePlayProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for app discovery (Aurora Store-like)
 * Combines Google Play API + F-Droid + APKMirror/APKPure
 */
@Singleton
class AppDiscoveryRepository @Inject constructor(
    private val fDroidProvider: FDroidProvider,
    private val googlePlayProvider: GooglePlayProvider
) {
    
    // Cache for loaded apps
    private var cachedFDroidApps: List<AppInfo>? = null
    private var cachedGooglePlayApps: List<AppInfo>? = null
    private var cachedPopularApps: List<AppInfo>? = null
    
    /**
     * Get featured sections for "Senin için" tab
     */
    suspend fun getFeaturedSections(isGame: Boolean): List<FeaturedSection> = withContext(Dispatchers.IO) {
        val allApps = getAllApps()
        val sectionTitles = if (isGame) Categories.GAME_FEATURED_SECTIONS else Categories.APP_FEATURED_SECTIONS
        
        sectionTitles.map { title ->
            FeaturedSection(
                title = title,
                apps = getAppsForSection(title, allApps, isGame).take(10)
            )
        }
    }
    
    /**
     * Get top charts for "Üst sıralar" tab
     */
    suspend fun getTopCharts(filter: String, isGame: Boolean): TopChart = withContext(Dispatchers.IO) {
        val allApps = getAllApps()
        val filteredApps = when (filter) {
            "En iyi ücretsiz" -> allApps.shuffled()
            "En yüksek hasılat" -> allApps.sortedByDescending { it.name.length }
            "Trend" -> allApps.shuffled()
            "En yüksek ücretli" -> allApps.take(20)
            else -> allApps
        }
        
        TopChart(
            filter = filter,
            apps = filteredApps.take(50)
        )
    }
    
    /**
     * Get categories list
     */
    fun getCategories(isGame: Boolean): List<AppCategory> {
        return if (isGame) Categories.GAME_CATEGORIES else Categories.APP_CATEGORIES
    }
    
    /**
     * Get apps by category
     */
    suspend fun getAppsByCategory(categoryId: String): List<AppInfo> = withContext(Dispatchers.IO) {
        val allApps = getAllApps()
        // For now, return shuffled subset. Later can filter by actual category.
        allApps.shuffled().take(30)
    }
    
    /**
     * Search apps across all sources (Google Play + F-Droid + local)
     */
    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        val results = mutableListOf<AppInfo>()
        
        // 1. Search Google Play first
        try {
            val googleResults = googlePlayProvider.searchApp(query)
            results.addAll(googleResults)
        } catch (e: Exception) {
            android.util.Log.e("AppDiscoveryRepo", "Google Play search failed", e)
        }
        
        // 2. Search local cache (F-Droid + popular apps)
        val allApps = getAllApps()
        val localResults = allApps.filter { 
            it.name.contains(query, ignoreCase = true) ||
            it.packageName.contains(query, ignoreCase = true)
        }
        results.addAll(localResults)
        
        // 3. Remove duplicates by package name
        val uniqueResults = results.distinctBy { it.packageName }
        
        if (uniqueResults.isEmpty() && query.length > 2) {
            // Fallback: Simulate "Not found"
            listOf(
                AppInfo(
                    packageName = "com.omniapk.search.${query.lowercase().replace(" ", "")}",
                    name = query,
                    versionName = "Latest",
                    versionCode = 1,
                    source = "Web Search",
                    description = "Try searching on APKMirror or APKPure",
                    icon = null
                )
            )
        } else {
            uniqueResults
        }
    }
    
    private suspend fun getAllApps(): List<AppInfo> {
        // Fetch from F-Droid
        if (cachedFDroidApps == null) {
            cachedFDroidApps = fDroidProvider.getPopularApps()
        }
        
        // Fetch from Google Play
        if (cachedGooglePlayApps == null) {
            try {
                cachedGooglePlayApps = googlePlayProvider.getTopApps(false)
            } catch (e: Exception) {
                android.util.Log.e("AppDiscoveryRepo", "Google Play fetch failed", e)
                cachedGooglePlayApps = emptyList()
            }
        }
        
        // Combine all sources + popular apps
        val popularApps = getPopularApps()
        return (cachedGooglePlayApps ?: emptyList()) + (cachedFDroidApps ?: emptyList()) + popularApps
    }
    
    private fun getAppsForSection(title: String, allApps: List<AppInfo>, isGame: Boolean): List<AppInfo> {
        // Map section titles to app filtering logic
        return when (title) {
            "Sosyal ağ" -> allApps.filter { 
                it.name.contains("chat", true) || it.name.contains("social", true) ||
                it.name.contains("messenger", true)
            }.ifEmpty { allApps.shuffled() }
            
            "Popüler uygulamalar" -> allApps.shuffled()
            "İletişim" -> allApps.filter { it.name.contains("message", true) || it.name.contains("call", true) }
                .ifEmpty { allApps.shuffled() }
            "İşletme araçları" -> allApps.filter { it.name.contains("office", true) || it.name.contains("pdf", true) }
                .ifEmpty { allApps.shuffled() }
            "Verimlilik" -> allApps.filter { it.name.contains("note", true) || it.name.contains("task", true) }
                .ifEmpty { allApps.shuffled() }
            
            // Game sections
            "Herkes oynuyor" -> allApps.shuffled()
            "Oyunlarda ön kayıt" -> allApps.take(10)
            "Türkiye'de geliştirilmiştir" -> allApps.shuffled().take(10)
            "Çevrimdışı oyunlar" -> allApps.shuffled()
            "Popüler oyunlar" -> allApps.shuffled()
            
            else -> allApps.shuffled()
        }
    }
    
    /**
     * Get popular apps (placeholder for APKMirror/APKPure integration)
     */
    private fun getPopularApps(): List<AppInfo> {
        if (cachedPopularApps != null) return cachedPopularApps!!
        
        // Popular apps with metadata (will be fetched from API later)
        cachedPopularApps = listOf(
            AppInfo(
                packageName = "com.openai.chatgpt",
                name = "ChatGPT",
                versionName = "1.2024.001",
                versionCode = 1,
                source = "APKMirror",
                description = "OpenAI's ChatGPT - AI assistant"
            ),
            AppInfo(
                packageName = "com.instagram.android",
                name = "Instagram",
                versionName = "300.0.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Photo & video sharing social network"
            ),
            AppInfo(
                packageName = "com.whatsapp",
                name = "WhatsApp Messenger",
                versionName = "2.24.1.1",
                versionCode = 1,
                source = "APKMirror",
                description = "Simple. Reliable. Private messaging."
            ),
            AppInfo(
                packageName = "com.zhiliaoapp.musically",
                name = "TikTok",
                versionName = "33.0.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Videos, Music & Live Streams"
            ),
            AppInfo(
                packageName = "com.google.android.apps.bard",
                name = "Google Gemini",
                versionName = "1.0.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Google's AI assistant"
            ),
            AppInfo(
                packageName = "com.trendyol.android",
                name = "Trendyol - Online Alışveriş",
                versionName = "6.0.0",
                versionCode = 1,
                source = "APKPure",
                description = "Türkiye'nin en büyük e-ticaret platformu"
            ),
            AppInfo(
                packageName = "com.twitter.android",
                name = "X (Eski Twitter)",
                versionName = "10.0.0",
                versionCode = 1,
                source = "APKMirror",
                description = "See what's happening in the world"
            ),
            AppInfo(
                packageName = "com.spotify.music",
                name = "Spotify",
                versionName = "8.9.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Music and Podcasts"
            ),
            AppInfo(
                packageName = "com.discord",
                name = "Discord",
                versionName = "200.0.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Talk, Chat & Hang Out"
            ),
            AppInfo(
                packageName = "org.telegram.messenger",
                name = "Telegram",
                versionName = "10.5.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Fast. Secure. Powerful."
            ),
            AppInfo(
                packageName = "com.facebook.katana",
                name = "Facebook",
                versionName = "400.0.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Connect with friends and the world"
            ),
            AppInfo(
                packageName = "com.netflix.mediaclient",
                name = "Netflix",
                versionName = "8.100.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Watch TV shows & movies"
            ),
            AppInfo(
                packageName = "com.yemeksepeti.android",
                name = "Yemeksepeti",
                versionName = "7.0.0",
                versionCode = 1,
                source = "APKPure",
                description = "Online yemek siparişi"
            ),
            AppInfo(
                packageName = "com.getir.android",
                name = "Getir",
                versionName = "5.0.0",
                versionCode = 1,
                source = "APKPure",
                description = "Dakikalar içinde teslimat"
            ),
            AppInfo(
                packageName = "tr.gov.turkiye.edevlet.kapisi",
                name = "e-Devlet Kapısı",
                versionName = "3.0.0",
                versionCode = 1,
                source = "APKPure",
                description = "T.C. Cumhurbaşkanlığı Dijital Dönüşüm Ofisi"
            )
        )
        
        return cachedPopularApps!!
    }
    
    /**
     * Get popular games
     */
    fun getPopularGames(): List<AppInfo> {
        return listOf(
            AppInfo(
                packageName = "com.pubg.imobile",
                name = "PUBG MOBILE",
                versionName = "3.0.0",
                versionCode = 1,
                source = "APKPure",
                description = "Battle Royale game",
                isGame = true
            ),
            AppInfo(
                packageName = "com.supercell.brawlstars",
                name = "Brawl Stars",
                versionName = "55.0.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Fast-paced multiplayer battles",
                isGame = true
            ),
            AppInfo(
                packageName = "com.kiloo.subwaysurf",
                name = "Subway Surfers",
                versionName = "3.20.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Endless runner game",
                isGame = true
            ),
            AppInfo(
                packageName = "com.king.candycrushsaga",
                name = "Candy Crush Saga",
                versionName = "1.250.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Match 3 puzzle game",
                isGame = true
            ),
            AppInfo(
                packageName = "com.ea.game.pvzfree_row",
                name = "EA SPORTS FC Mobile Futbol",
                versionName = "22.0.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Football simulation game",
                isGame = true
            ),
            AppInfo(
                packageName = "com.mojang.minecraftpe",
                name = "Minecraft",
                versionName = "1.20.50",
                versionCode = 1,
                source = "APKPure",
                description = "Build anything you can imagine",
                isGame = true
            ),
            AppInfo(
                packageName = "com.roblox.client",
                name = "Roblox",
                versionName = "2.600.0",
                versionCode = 1,
                source = "APKMirror",
                description = "Ultimate virtual universe",
                isGame = true
            ),
            AppInfo(
                packageName = "com.activision.callofduty.shooter",
                name = "Call of Duty: Mobile",
                versionName = "1.0.40",
                versionCode = 1,
                source = "APKMirror",
                description = "FPS shooter game",
                isGame = true
            )
        )
    }
    
    fun clearCache() {
        cachedFDroidApps = null
        cachedPopularApps = null
    }
}
