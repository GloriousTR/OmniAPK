/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.data.model.webtoapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Comprehensive model representing a web app configuration for WebToApp feature.
 * Based on shiahonb777/web-to-app project features.
 * This allows users to create standalone apps from websites with rich customization.
 */
@Parcelize
@Serializable
data class WebApp(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val url: String,
    val packageName: String = "com.webtoapp.${name.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")}",
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val iconUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),

    // =====================================================================
    // CORE WEBVIEW SETTINGS
    // =====================================================================
    val enableJavaScript: Boolean = true,
    val enableDomStorage: Boolean = true,
    val enableFileAccess: Boolean = false,
    val enableGeolocation: Boolean = false,
    val enableZoom: Boolean = true,
    val enableBuiltInZoomControls: Boolean = false,
    val displayZoomControls: Boolean = false,

    // =====================================================================
    // DISPLAY & APPEARANCE
    // =====================================================================
    val enableFullscreen: Boolean = false,
    val enableDesktopMode: Boolean = false,
    val enableDarkMode: Boolean = false,
    val forceDarkMode: Boolean = false,
    val screenOrientation: ScreenOrientation = ScreenOrientation.UNSPECIFIED,
    val keepScreenOn: Boolean = false,
    val hideStatusBar: Boolean = false,
    val hideNavigationBar: Boolean = false,
    val immersiveMode: Boolean = false,

    // =====================================================================
    // PRIVACY & SECURITY
    // =====================================================================
    val enableAdBlock: Boolean = true,
    val enablePopupBlock: Boolean = true,
    val enableAntiTracking: Boolean = false,
    val enableFingerprintSpoofing: Boolean = false,
    val enableCookieManager: Boolean = true,
    val thirdPartyCookies: Boolean = false,
    val clearCacheOnExit: Boolean = false,
    val clearCookiesOnExit: Boolean = false,
    val enableIncognitoMode: Boolean = false,

    // =====================================================================
    // NETWORK & USER AGENT
    // =====================================================================
    val userAgent: UserAgentType = UserAgentType.DEFAULT,
    val customUserAgent: String? = null,
    val enableProxy: Boolean = false,
    val proxyHost: String? = null,
    val proxyPort: Int? = null,

    // =====================================================================
    // NAVIGATION & BEHAVIOR
    // =====================================================================
    val enablePullToRefresh: Boolean = true,
    val enableSwipeNavigation: Boolean = false,
    val enableBackButtonExit: Boolean = true,
    val confirmExit: Boolean = false,
    val openExternalLinksInBrowser: Boolean = true,
    val allowedDomains: List<String> = emptyList(),
    val blockedDomains: List<String> = emptyList(),

    // =====================================================================
    // DOWNLOAD & MEDIA
    // =====================================================================
    val enableDownloads: Boolean = true,
    val enableMediaPlayback: Boolean = true,
    val enableVideoFullscreen: Boolean = true,
    val muteAudio: Boolean = false,
    val autoplayMedia: Boolean = false,

    // =====================================================================
    // SPLASH SCREEN
    // =====================================================================
    val enableSplashScreen: Boolean = false,
    val splashImageUri: String? = null,
    val splashVideoUri: String? = null,
    val splashDurationMs: Long = 3000,
    val splashBackgroundColor: String = "#FFFFFF",

    // =====================================================================
    // BACKGROUND MUSIC (BGM)
    // =====================================================================
    val enableBgm: Boolean = false,
    val bgmUri: String? = null,
    val bgmVolume: Float = 0.5f,
    val bgmLoop: Boolean = true,
    val bgmAutoPlay: Boolean = true,

    // =====================================================================
    // ACTIVATION CODE
    // =====================================================================
    val enableActivation: Boolean = false,
    val activationCode: String? = null,
    val activationMessage: String = "Please enter activation code",

    // =====================================================================
    // ANNOUNCEMENT/NOTICE
    // =====================================================================
    val enableAnnouncement: Boolean = false,
    val announcementTitle: String? = null,
    val announcementContent: String? = null,
    val announcementButtonText: String = "OK",
    val announcementUrl: String? = null,
    val announcementTemplate: AnnouncementTemplate = AnnouncementTemplate.DEFAULT,
    val showAnnouncementOnce: Boolean = true,

    // =====================================================================
    // AUTO START & SCHEDULED RUN
    // =====================================================================
    val enableAutoStart: Boolean = false,
    val enableScheduledStart: Boolean = false,
    val scheduledStartTime: String? = null,
    val scheduledEndTime: String? = null,

    // =====================================================================
    // FORCED RUN MODE
    // =====================================================================
    val enableForcedRun: Boolean = false,
    val blockBackButton: Boolean = false,
    val blockHomeButton: Boolean = false,
    val blockRecentApps: Boolean = false,
    val forcedRunDurationMinutes: Int = 0,

    // =====================================================================
    // EXTENSION MODULES (Tampermonkey-like)
    // =====================================================================
    val enabledExtensions: List<String> = emptyList(),
    val customJavaScript: String? = null,
    val customCss: String? = null,
    val injectScriptAtStart: Boolean = true,

    // =====================================================================
    // ISOLATION & MULTI-INSTANCE
    // =====================================================================
    val enableIsolation: Boolean = false,
    val isolatedCookies: Boolean = false,
    val isolatedStorage: Boolean = false,

    // =====================================================================
    // ENCRYPTION (for APK export)
    // =====================================================================
    val enableEncryption: Boolean = false,
    val encryptConfig: Boolean = false,
    val encryptAssets: Boolean = false,
    val antiDebug: Boolean = false,

    // =====================================================================
    // TRANSLATION
    // =====================================================================
    val enableAutoTranslate: Boolean = false,
    val translateTargetLanguage: String = "en"

) : Parcelable {

    companion object {
        /**
         * Creates a default web app configuration from a URL
         */
        fun fromUrl(url: String, name: String? = null): WebApp {
            val extractedName = name ?: extractNameFromUrl(url)
            return WebApp(
                name = extractedName,
                url = normalizeUrl(url)
            )
        }

        private fun extractNameFromUrl(url: String): String {
            return try {
                val host = url
                    .removePrefix("https://")
                    .removePrefix("http://")
                    .removePrefix("www.")
                    .substringBefore("/")
                    .substringBefore(".")
                    .replaceFirstChar { it.uppercase() }
                host.ifEmpty { "Web App" }
            } catch (_: Exception) {
                "Web App"
            }
        }

        private fun normalizeUrl(url: String): String {
            return when {
                url.startsWith("http://") || url.startsWith("https://") -> url
                else -> "https://$url"
            }
        }
    }
}

/**
 * Screen orientation options
 */
@Serializable
enum class ScreenOrientation {
    UNSPECIFIED,
    PORTRAIT,
    LANDSCAPE,
    SENSOR,
    FULL_SENSOR
}

/**
 * User Agent type options
 */
@Serializable
enum class UserAgentType {
    DEFAULT,
    DESKTOP_CHROME,
    DESKTOP_FIREFOX,
    DESKTOP_SAFARI,
    MOBILE_CHROME,
    MOBILE_SAFARI,
    TABLET,
    CUSTOM
}

/**
 * Announcement template styles
 */
@Serializable
enum class AnnouncementTemplate {
    DEFAULT,
    GRADIENT,
    GLASSMORPHISM,
    NEON,
    MINIMAL,
    CARD,
    FULLSCREEN,
    BOTTOM_SHEET,
    SLIDE_IN,
    FADE
}

/**
 * Built-in extension modules (Tampermonkey-like)
 */
@Serializable
enum class ExtensionModule(val displayName: String, val description: String) {
    VIDEO_DOWNLOADER("Video Downloader", "Auto-detect and download videos from webpages"),
    DARK_MODE("Dark Mode", "Force dark theme on all webpages"),
    PRIVACY_PROTECTION("Privacy Protection", "Block trackers and fingerprinting"),
    AD_BLOCKER("Ad Blocker", "Block ads and popups"),
    VIDEO_ENHANCER("Video Enhancer", "Speed control, PiP, background play"),
    CONTENT_ENHANCER("Content Enhancer", "Force copy, selection translate"),
    ELEMENT_BLOCKER("Element Blocker", "Block specified page elements"),
    WEB_ANALYZER("Web Analyzer", "Element inspector, network monitor"),
    AUTO_SCROLL("Auto Scroll", "Automatic page scrolling"),
    SCREENSHOT("Screenshot", "Capture full page screenshots")
}

/**
 * State for WebToApp build process
 */
@Serializable
sealed class WebAppBuildState {
    @Serializable
    data object Idle : WebAppBuildState()
    @Serializable
    data object Preparing : WebAppBuildState()
    @Serializable
    data class Building(val progress: Int, val currentStep: String = "") : WebAppBuildState()
    @Serializable
    data class Success(val apkPath: String) : WebAppBuildState()
    @Serializable
    data class Error(val message: String) : WebAppBuildState()
}
