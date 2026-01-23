package com.omniapk.data.model

/**
 * App category model for Aurora Store-like UI
 */
data class AppCategory(
    val id: String,
    val name: String,
    val icon: String = "",
    val type: CategoryType = CategoryType.APP
)

enum class CategoryType {
    APP,
    GAME
}

/**
 * Default categories matching Aurora Store
 */
object Categories {
    
    val APP_CATEGORIES = listOf(
        AppCategory("shopping", "Alışveriş", "🛒"),
        AppCategory("android_auto", "Android Auto", "🚗"),
        AppCategory("tools", "Araçlar", "🔧"),
        AppCategory("dating", "Arkadaşlık", "💕"),
        AppCategory("parenting", "Ebeveynlik", "👶"),
        AppCategory("education", "Eğitim", "📚"),
        AppCategory("entertainment", "Eğlence", "🎭"),
        AppCategory("events", "Etkinlikler", "📅"),
        AppCategory("home", "Ev", "🏠"),
        AppCategory("finance", "Finans", "💰"),
        AppCategory("photography", "Fotoğrafçılık", "📷"),
        AppCategory("beauty", "Güzellik", "💄"),
        AppCategory("news", "Haberler ve Dergiler", "📰"),
        AppCategory("communication", "Haberleşme", "💬"),
        AppCategory("maps", "Haritalar ve Navigasyon", "🗺️"),
        AppCategory("weather", "Hava Durumu", "🌤️"),
        AppCategory("business", "İş", "💼"),
        AppCategory("comics", "Karikatür", "🎨"),
        AppCategory("personalization", "Kişiselleştirme", "✨"),
        AppCategory("books", "Kitaplar ve Referans", "📖"),
        AppCategory("libraries", "Kitaplıklar ve Kısa Sunum", "📚"),
        AppCategory("music", "Müzik ve Ses", "🎵"),
        AppCategory("auto", "Otomobil ve Araçlar", "🚙"),
        AppCategory("productivity", "Verimlilik", "⚡"),
        AppCategory("social", "Sosyal ağ", "👥"),
        AppCategory("health", "Sağlık ve Fitness", "💪"),
        AppCategory("food", "Yiyecek ve İçecek", "🍔"),
        AppCategory("travel", "Seyahat ve Yerel", "✈️"),
        AppCategory("video", "Video Oynatıcılar", "🎬"),
        AppCategory("medical", "Tıbbi", "🏥")
    )
    
    val GAME_CATEGORIES = listOf(
        AppCategory("action", "Aksiyon", "⚔️", CategoryType.GAME),
        AppCategory("adventure", "Macera", "🗺️", CategoryType.GAME),
        AppCategory("arcade", "Arcade", "🕹️", CategoryType.GAME),
        AppCategory("board", "Masa Oyunları", "🎲", CategoryType.GAME),
        AppCategory("card", "Kart", "🃏", CategoryType.GAME),
        AppCategory("casino", "Kumarhane", "🎰", CategoryType.GAME),
        AppCategory("casual", "Gündelik", "🎮", CategoryType.GAME),
        AppCategory("educational", "Eğitici", "🎓", CategoryType.GAME),
        AppCategory("music_game", "Müzik", "🎸", CategoryType.GAME),
        AppCategory("puzzle", "Bulmaca", "🧩", CategoryType.GAME),
        AppCategory("racing", "Yarış", "🏎️", CategoryType.GAME),
        AppCategory("role_playing", "Rol Yapma", "🧙", CategoryType.GAME),
        AppCategory("simulation", "Simülasyon", "🏗️", CategoryType.GAME),
        AppCategory("sports", "Spor", "⚽", CategoryType.GAME),
        AppCategory("strategy", "Strateji", "♟️", CategoryType.GAME),
        AppCategory("trivia", "Trivia", "❓", CategoryType.GAME),
        AppCategory("word", "Kelime", "📝", CategoryType.GAME)
    )
    
    // Featured sections for "Senin için" tab
    val APP_FEATURED_SECTIONS = listOf(
        "Sosyal ağ",
        "Popüler uygulamalar",
        "İletişim",
        "İşletme araçları",
        "Verimlilik"
    )
    
    val GAME_FEATURED_SECTIONS = listOf(
        "Herkes oynuyor",
        "Oyunlarda ön kayıt",
        "Türkiye'de geliştirilmiştir",
        "Çevrimdışı oyunlar",
        "Popüler oyunlar"
    )
    
    // Filter options for "Üst sıralar" tab
    val TOP_CHART_FILTERS = listOf(
        "En iyi ücretsiz",
        "En yüksek hasılat",
        "Trend",
        "En yüksek ücretli"
    )
}
