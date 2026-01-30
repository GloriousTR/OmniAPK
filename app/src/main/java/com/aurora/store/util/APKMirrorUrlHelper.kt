/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.util

/**
 * Helper class to generate direct APKMirror URLs for popular apps.
 * APKMirror URL format: https://www.apkmirror.com/apk/{publisher}/{app-slug}/
 * 
 * For apps not in the mapping, falls back to search URL.
 */
object APKMirrorUrlHelper {

    /**
     * Mapping of package names to their APKMirror paths.
     * Format: packageName -> "publisher/app-slug"
     */
    private val packageMappings = mapOf(
        // Meta/Facebook Apps
        "com.whatsapp" to "whatsapp-inc/whatsapp-messenger",
        "com.whatsapp.w4b" to "whatsapp-inc/whatsapp-business",
        "com.instagram.android" to "instagram/instagram",
        "com.facebook.katana" to "facebook-2/facebook",
        "com.facebook.orca" to "facebook-2/messenger-text-and-video-chat-for-free",
        "com.facebook.lite" to "facebook-2/facebook-lite",
        
        // Google Apps
        "com.google.android.youtube" to "google-inc/youtube",
        "com.google.android.apps.youtube.music" to "google-inc/youtube-music",
        "com.google.android.gm" to "google-inc/gmail",
        "com.google.android.apps.maps" to "google-inc/maps",
        "com.google.android.apps.photos" to "google-inc/google-photos",
        "com.google.android.apps.docs" to "google-inc/google-drive",
        "com.google.android.apps.translate" to "google-inc/google-translate",
        "com.google.android.apps.messaging" to "google-inc/messages",
        "com.google.android.keep" to "google-inc/google-keep-notes-and-lists",
        "com.google.android.calendar" to "google-inc/google-calendar",
        "com.google.android.googlequicksearchbox" to "google-inc/google",
        "com.android.chrome" to "google-inc/chrome",
        "com.google.android.apps.tachyon" to "google-inc/google-duo",
        
        // Microsoft Apps
        "com.microsoft.teams" to "microsoft-corporation/microsoft-teams",
        "com.microsoft.office.outlook" to "microsoft-corporation/microsoft-outlook",
        "com.microsoft.office.word" to "microsoft-corporation/microsoft-word",
        "com.microsoft.office.excel" to "microsoft-corporation/microsoft-excel",
        "com.microsoft.office.powerpoint" to "microsoft-corporation/microsoft-powerpoint",
        "com.microsoft.skydrive" to "microsoft-corporation/microsoft-onedrive",
        "com.skype.raider" to "skype/skype",
        
        // Social Media
        "com.twitter.android" to "x-corp/x-formerly-twitter",
        "com.zhiliaoapp.musically" to "tiktok-pte-ltd/tiktok",
        "com.snapchat.android" to "snap-inc/snapchat",
        "com.pinterest" to "pinterest/pinterest",
        "com.reddit.frontpage" to "redditinc/reddit",
        "com.linkedin.android" to "linkedin/linkedin",
        "org.telegram.messenger" to "telegram-fz-llc/telegram",
        "org.thunderdog.challegram" to "nicegram/telegram-x",
        "com.discord" to "discord-inc/discord-chat-talk-hangout",
        
        // Messaging
        "com.viber.voip" to "viber-media-s-a-r-l/viber-messenger",
        "com.imo.android.imoim" to "imo-inc/imo",
        "jp.naver.line.android" to "line-corporation/line",
        "com.wire" to "wire/wire",
        
        // Entertainment & Streaming
        "com.spotify.music" to "spotify-ltd/spotify-music-and-podcasts",
        "com.netflix.mediaclient" to "netflix-inc/netflix",
        "com.amazon.avod.thirdpartyclient" to "amazon-mobile-llc/amazon-prime-video",
        "com.disney.disneyplus" to "disney/disney",
        "tv.twitch.android.app" to "twitch-interactive-inc/twitch",
        "com.soundcloud.android" to "soundcloud/soundcloud-play-music-audio-new-songs",
        
        // Shopping & Services
        "com.amazon.mShop.android.shopping" to "amazon-mobile-llc/amazon-shopping",
        "com.alibaba.aliexpresshd" to "alibaba/aliexpress",
        "com.ebay.mobile" to "ebay-mobile/ebay-online-shopping-buy-sell-and-save-money",
        
        // Utilities
        "com.shazam.android" to "shazam-entertainment-limited/shazam",
        "com.truecaller" to "true-software-scandinavia-ab/truecaller-phone-caller-id-spam-blocking-chat",
        "com.dropbox.android" to "dropbox-inc/dropbox",
        "com.evernote" to "evernote-corporation/evernote",
        "com.adobe.reader" to "adobe/adobe-acrobat-reader-pdf-viewer-editor-creator",
        "com.adobe.lrmobile" to "adobe/lightroom",
        
        // Games (Popular)
        "com.supercell.clashofclans" to "supercell/clash-of-clans",
        "com.supercell.clashroyale" to "supercell/clash-royale",
        "com.supercell.brawlstars" to "supercell/brawl-stars",
        "com.king.candycrushsaga" to "king/candy-crush-saga",
        "com.kiloo.subwaysurf" to "sybo-games/subway-surfers",
        "com.imangi.templerun2" to "imangi-studios/temple-run-2",
        "com.mojang.minecraftpe" to "mojang/minecraft",
        "com.rovio.angrybirds" to "rovio-entertainment-corporation/angry-birds-2",
        "com.nianticlabs.pokemongo" to "niantic-inc/pokemon-go",
        
        // VPN & Security
        "com.nordvpn.android" to "nordvpn-s-a/nordvpn-fast-secure-vpn",
        "com.expressvpn.vpn" to "expressvpn/expressvpn-vpn-for-android",
        "com.avast.android.mobilesecurity" to "avast-software/avast-antivirus-app-security",
        "com.kms.free" to "kaspersky-lab/kaspersky-internet-security",
        
        // Browsers
        "org.mozilla.firefox" to "mozilla/firefox-fast-private-browser",
        "com.opera.browser" to "opera/opera-browser-fast-private",
        "com.brave.browser" to "brave-software/brave-browser-fast-adblocker",
        "com.UCMobile.intl" to "ucweb-inc/uc-browser-safe-fast-video-downloader",
        "com.duckduckgo.mobile.android" to "duckduckgo/duckduckgo-privacy-browser",
        
        // Office & Productivity
        "com.todoist" to "doist/todoist-to-do-list-task-list",
        "com.notion.id" to "notion-labs-inc/notion-notes-docs-tasks",
        "com.samsung.android.app.notes" to "samsung-electronics-co-ltd/samsung-notes",
        
        // Banking & Finance
        "com.paypal.android.p2pmobile" to "paypal-inc/paypal",
        
        // Weather
        "com.accuweather.android" to "accuweather-inc/accuweather-weather-alerts-live-forecast-info"
    )

    /**
     * Gets the APKMirror URL for a given package name.
     * 
     * @param packageName The Android package name (e.g., "com.whatsapp")
     * @return Direct app page URL if in mapping, otherwise search URL
     */
    fun getUrl(packageName: String): String {
        val mapping = packageMappings[packageName]
        
        return if (mapping != null) {
            // Direct app page URL
            "https://www.apkmirror.com/apk/$mapping/"
        } else {
            // Fallback to search URL
            "https://www.apkmirror.com/?post_type=app_release&searchtype=app&s=$packageName"
        }
    }

    /**
     * Checks if the package has a direct mapping
     */
    fun hasDirectMapping(packageName: String): Boolean {
        return packageMappings.containsKey(packageName)
    }
}
