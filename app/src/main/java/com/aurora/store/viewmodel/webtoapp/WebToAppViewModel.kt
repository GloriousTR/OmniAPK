/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.viewmodel.webtoapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.extensions.TAG
import com.aurora.store.data.model.webtoapp.AnnouncementTemplate
import com.aurora.store.data.model.webtoapp.ExtensionModule
import com.aurora.store.data.model.webtoapp.ScreenOrientation
import com.aurora.store.data.model.webtoapp.UserAgentType
import com.aurora.store.data.model.webtoapp.WebApp
import com.aurora.store.data.model.webtoapp.WebAppBuildState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * ViewModel for WebToApp feature with comprehensive settings
 * Based on shiahonb777/web-to-app project features
 */
@HiltViewModel
class WebToAppViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    // =====================================================================
    // BASIC INFO
    // =====================================================================
    val appName = mutableStateOf("")
    val websiteUrl = mutableStateOf("")
    val packageName = mutableStateOf("")
    val versionName = mutableStateOf("1.0.0")
    val versionCode = mutableStateOf(1)

    // Selected icon URI
    private val _selectedIconUri = MutableStateFlow<Uri?>(null)
    val selectedIconUri = _selectedIconUri.asStateFlow()

    // =====================================================================
    // CORE WEBVIEW SETTINGS
    // =====================================================================
    val enableJavaScript = mutableStateOf(true)
    val enableDomStorage = mutableStateOf(true)
    val enableFileAccess = mutableStateOf(false)
    val enableGeolocation = mutableStateOf(false)
    val enableZoom = mutableStateOf(true)

    // =====================================================================
    // DISPLAY & APPEARANCE
    // =====================================================================
    val enableFullscreen = mutableStateOf(false)
    val enableDesktopMode = mutableStateOf(false)
    val enableDarkMode = mutableStateOf(false)
    val forceDarkMode = mutableStateOf(false)
    val screenOrientation = mutableStateOf(ScreenOrientation.UNSPECIFIED)
    val keepScreenOn = mutableStateOf(false)
    val hideStatusBar = mutableStateOf(false)
    val hideNavigationBar = mutableStateOf(false)
    val immersiveMode = mutableStateOf(false)

    // =====================================================================
    // PRIVACY & SECURITY
    // =====================================================================
    val enableAdBlock = mutableStateOf(true)
    val enablePopupBlock = mutableStateOf(true)
    val enableAntiTracking = mutableStateOf(false)
    val enableFingerprintSpoofing = mutableStateOf(false)
    val enableCookieManager = mutableStateOf(true)
    val thirdPartyCookies = mutableStateOf(false)
    val clearCacheOnExit = mutableStateOf(false)
    val clearCookiesOnExit = mutableStateOf(false)
    val enableIncognitoMode = mutableStateOf(false)

    // =====================================================================
    // NETWORK & USER AGENT
    // =====================================================================
    val userAgentType = mutableStateOf(UserAgentType.DEFAULT)
    val customUserAgent = mutableStateOf("")

    // =====================================================================
    // NAVIGATION & BEHAVIOR
    // =====================================================================
    val enablePullToRefresh = mutableStateOf(true)
    val enableSwipeNavigation = mutableStateOf(false)
    val enableBackButtonExit = mutableStateOf(true)
    val confirmExit = mutableStateOf(false)
    val openExternalLinksInBrowser = mutableStateOf(true)

    // =====================================================================
    // DOWNLOAD & MEDIA
    // =====================================================================
    val enableDownloads = mutableStateOf(true)
    val enableMediaPlayback = mutableStateOf(true)
    val enableVideoFullscreen = mutableStateOf(true)
    val muteAudio = mutableStateOf(false)
    val autoplayMedia = mutableStateOf(false)

    // =====================================================================
    // SPLASH SCREEN
    // =====================================================================
    val enableSplashScreen = mutableStateOf(false)
    private val _splashImageUri = MutableStateFlow<Uri?>(null)
    val splashImageUri = _splashImageUri.asStateFlow()
    val splashDurationMs = mutableStateOf(3000L)
    val splashBackgroundColor = mutableStateOf("#FFFFFF")

    // =====================================================================
    // BACKGROUND MUSIC (BGM)
    // =====================================================================
    val enableBgm = mutableStateOf(false)
    private val _bgmUri = MutableStateFlow<Uri?>(null)
    val bgmUri = _bgmUri.asStateFlow()
    val bgmVolume = mutableStateOf(0.5f)
    val bgmLoop = mutableStateOf(true)
    val bgmAutoPlay = mutableStateOf(true)

    // =====================================================================
    // ACTIVATION CODE
    // =====================================================================
    val enableActivation = mutableStateOf(false)
    val activationCode = mutableStateOf("")
    val activationMessage = mutableStateOf("Please enter activation code")

    // =====================================================================
    // ANNOUNCEMENT/NOTICE
    // =====================================================================
    val enableAnnouncement = mutableStateOf(false)
    val announcementTitle = mutableStateOf("")
    val announcementContent = mutableStateOf("")
    val announcementButtonText = mutableStateOf("OK")
    val announcementUrl = mutableStateOf("")
    val announcementTemplate = mutableStateOf(AnnouncementTemplate.DEFAULT)
    val showAnnouncementOnce = mutableStateOf(true)

    // =====================================================================
    // AUTO START & SCHEDULED RUN
    // =====================================================================
    val enableAutoStart = mutableStateOf(false)
    val enableScheduledStart = mutableStateOf(false)
    val scheduledStartTime = mutableStateOf("")
    val scheduledEndTime = mutableStateOf("")

    // =====================================================================
    // FORCED RUN MODE
    // =====================================================================
    val enableForcedRun = mutableStateOf(false)
    val blockBackButton = mutableStateOf(false)
    val blockHomeButton = mutableStateOf(false)
    val blockRecentApps = mutableStateOf(false)
    val forcedRunDurationMinutes = mutableStateOf(0)

    // =====================================================================
    // EXTENSION MODULES
    // =====================================================================
    val enabledExtensions = mutableStateOf<Set<ExtensionModule>>(emptySet())
    val customJavaScript = mutableStateOf("")
    val customCss = mutableStateOf("")
    val injectScriptAtStart = mutableStateOf(true)

    // =====================================================================
    // ISOLATION & ENCRYPTION
    // =====================================================================
    val enableIsolation = mutableStateOf(false)
    val enableEncryption = mutableStateOf(false)

    // =====================================================================
    // TRANSLATION
    // =====================================================================
    val enableAutoTranslate = mutableStateOf(false)
    val translateTargetLanguage = mutableStateOf("en")

    // =====================================================================
    // UI STATE
    // =====================================================================
    // Current settings section
    val currentSection = mutableStateOf(SettingsSection.BASIC)

    // Build state
    private val _buildState = MutableStateFlow<WebAppBuildState>(WebAppBuildState.Idle)
    val buildState = _buildState.asStateFlow()

    // Saved web apps
    private val _savedWebApps = MutableStateFlow<List<WebApp>>(emptyList())
    val savedWebApps = _savedWebApps.asStateFlow()

    // Validation
    val isFormValid: Boolean
        get() = appName.value.isNotBlank() && websiteUrl.value.isNotBlank() && isValidUrl(websiteUrl.value)

    init {
        loadSavedWebApps()
    }

    private fun isValidUrl(url: String): Boolean {
        val urlPattern = Regex(
            "^(https?://)?([\\w.-]+)(:[0-9]+)?(/.*)?$",
            RegexOption.IGNORE_CASE
        )
        return urlPattern.matches(url.trim())
    }

    fun setIconUri(uri: Uri?) {
        _selectedIconUri.value = uri
    }

    fun setSplashImageUri(uri: Uri?) {
        _splashImageUri.value = uri
    }

    fun setBgmUri(uri: Uri?) {
        _bgmUri.value = uri
    }

    fun toggleExtension(module: ExtensionModule) {
        val current = enabledExtensions.value.toMutableSet()
        if (module in current) {
            current.remove(module)
        } else {
            current.add(module)
        }
        enabledExtensions.value = current
    }

    fun setSection(section: SettingsSection) {
        currentSection.value = section
    }

    fun createWebApp(): WebApp {
        val finalPackageName = if (packageName.value.isNotBlank()) {
            packageName.value.trim()
        } else {
            "com.webtoapp.${appName.value.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")}"
        }

        return WebApp(
            name = appName.value.trim(),
            url = normalizeUrl(websiteUrl.value.trim()),
            packageName = finalPackageName,
            versionName = versionName.value,
            versionCode = versionCode.value,
            iconUri = _selectedIconUri.value?.toString(),

            // Core WebView
            enableJavaScript = enableJavaScript.value,
            enableDomStorage = enableDomStorage.value,
            enableFileAccess = enableFileAccess.value,
            enableGeolocation = enableGeolocation.value,
            enableZoom = enableZoom.value,

            // Display
            enableFullscreen = enableFullscreen.value,
            enableDesktopMode = enableDesktopMode.value,
            enableDarkMode = enableDarkMode.value,
            forceDarkMode = forceDarkMode.value,
            screenOrientation = screenOrientation.value,
            keepScreenOn = keepScreenOn.value,
            hideStatusBar = hideStatusBar.value,
            hideNavigationBar = hideNavigationBar.value,
            immersiveMode = immersiveMode.value,

            // Privacy & Security
            enableAdBlock = enableAdBlock.value,
            enablePopupBlock = enablePopupBlock.value,
            enableAntiTracking = enableAntiTracking.value,
            enableFingerprintSpoofing = enableFingerprintSpoofing.value,
            enableCookieManager = enableCookieManager.value,
            thirdPartyCookies = thirdPartyCookies.value,
            clearCacheOnExit = clearCacheOnExit.value,
            clearCookiesOnExit = clearCookiesOnExit.value,
            enableIncognitoMode = enableIncognitoMode.value,

            // User Agent
            userAgent = userAgentType.value,
            customUserAgent = customUserAgent.value.takeIf { it.isNotBlank() },

            // Navigation
            enablePullToRefresh = enablePullToRefresh.value,
            enableSwipeNavigation = enableSwipeNavigation.value,
            enableBackButtonExit = enableBackButtonExit.value,
            confirmExit = confirmExit.value,
            openExternalLinksInBrowser = openExternalLinksInBrowser.value,

            // Media
            enableDownloads = enableDownloads.value,
            enableMediaPlayback = enableMediaPlayback.value,
            enableVideoFullscreen = enableVideoFullscreen.value,
            muteAudio = muteAudio.value,
            autoplayMedia = autoplayMedia.value,

            // Splash
            enableSplashScreen = enableSplashScreen.value,
            splashImageUri = _splashImageUri.value?.toString(),
            splashDurationMs = splashDurationMs.value,
            splashBackgroundColor = splashBackgroundColor.value,

            // BGM
            enableBgm = enableBgm.value,
            bgmUri = _bgmUri.value?.toString(),
            bgmVolume = bgmVolume.value,
            bgmLoop = bgmLoop.value,
            bgmAutoPlay = bgmAutoPlay.value,

            // Activation
            enableActivation = enableActivation.value,
            activationCode = activationCode.value.takeIf { it.isNotBlank() },
            activationMessage = activationMessage.value,

            // Announcement
            enableAnnouncement = enableAnnouncement.value,
            announcementTitle = announcementTitle.value.takeIf { it.isNotBlank() },
            announcementContent = announcementContent.value.takeIf { it.isNotBlank() },
            announcementButtonText = announcementButtonText.value,
            announcementUrl = announcementUrl.value.takeIf { it.isNotBlank() },
            announcementTemplate = announcementTemplate.value,
            showAnnouncementOnce = showAnnouncementOnce.value,

            // Auto Start
            enableAutoStart = enableAutoStart.value,
            enableScheduledStart = enableScheduledStart.value,
            scheduledStartTime = scheduledStartTime.value.takeIf { it.isNotBlank() },
            scheduledEndTime = scheduledEndTime.value.takeIf { it.isNotBlank() },

            // Forced Run
            enableForcedRun = enableForcedRun.value,
            blockBackButton = blockBackButton.value,
            blockHomeButton = blockHomeButton.value,
            blockRecentApps = blockRecentApps.value,
            forcedRunDurationMinutes = forcedRunDurationMinutes.value,

            // Extensions
            enabledExtensions = enabledExtensions.value.map { it.name },
            customJavaScript = customJavaScript.value.takeIf { it.isNotBlank() },
            customCss = customCss.value.takeIf { it.isNotBlank() },
            injectScriptAtStart = injectScriptAtStart.value,

            // Isolation & Encryption
            enableIsolation = enableIsolation.value,
            enableEncryption = enableEncryption.value,

            // Translation
            enableAutoTranslate = enableAutoTranslate.value,
            translateTargetLanguage = translateTargetLanguage.value
        )
    }

    private fun normalizeUrl(url: String): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            else -> "https://$url"
        }
    }

    fun buildWebApp() {
        if (!isFormValid) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _buildState.value = WebAppBuildState.Preparing

                val webApp = createWebApp()

                delay(300)
                _buildState.value = WebAppBuildState.Building(10, "Initializing...")

                // Create output directory
                val outputDir = File(context.getExternalFilesDir(null), "webtoapp")
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }

                delay(300)
                _buildState.value = WebAppBuildState.Building(20, "Processing icon...")

                // Save icon if provided
                _selectedIconUri.value?.let { uri ->
                    try {
                        saveIcon(uri, webApp.id, outputDir)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to save icon", e)
                    }
                }

                delay(300)
                _buildState.value = WebAppBuildState.Building(35, "Processing splash screen...")

                // Save splash image if provided
                _splashImageUri.value?.let { uri ->
                    try {
                        saveSplashImage(uri, webApp.id, outputDir)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to save splash image", e)
                    }
                }

                delay(300)
                _buildState.value = WebAppBuildState.Building(50, "Generating configuration...")

                // Generate web app configuration using JSON serialization
                val configFile = File(outputDir, "${webApp.id}_config.json")
                configFile.writeText(json.encodeToString(webApp))

                delay(300)
                _buildState.value = WebAppBuildState.Building(65, "Processing extensions...")

                // Generate extension scripts if any
                if (enabledExtensions.value.isNotEmpty() || customJavaScript.value.isNotBlank()) {
                    generateExtensionScripts(webApp.id, outputDir)
                }

                delay(300)
                _buildState.value = WebAppBuildState.Building(80, "Saving app data...")

                // Save to database/preferences
                saveWebApp(webApp)

                delay(300)
                _buildState.value = WebAppBuildState.Building(95, "Finalizing...")

                delay(200)
                _buildState.value = WebAppBuildState.Success(configFile.absolutePath)

                // Reset form
                resetForm()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to build web app", e)
                _buildState.value = WebAppBuildState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun saveIcon(uri: Uri, appId: String, outputDir: File) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val iconFile = File(outputDir, "${appId}_icon.png")
            FileOutputStream(iconFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
        }
    }

    private fun saveSplashImage(uri: Uri, appId: String, outputDir: File) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val splashFile = File(outputDir, "${appId}_splash.png")
            FileOutputStream(splashFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
        }
    }

    private fun generateExtensionScripts(appId: String, outputDir: File) {
        val scriptBuilder = StringBuilder()

        // Add built-in extension scripts
        enabledExtensions.value.forEach { module ->
            scriptBuilder.appendLine("// ${module.displayName}")
            scriptBuilder.appendLine(getExtensionScript(module))
            scriptBuilder.appendLine()
        }

        // Add custom JavaScript
        if (customJavaScript.value.isNotBlank()) {
            scriptBuilder.appendLine("// Custom JavaScript")
            scriptBuilder.appendLine(customJavaScript.value)
        }

        if (scriptBuilder.isNotBlank()) {
            val scriptFile = File(outputDir, "${appId}_extensions.js")
            scriptFile.writeText(scriptBuilder.toString())
        }

        // Save custom CSS
        if (customCss.value.isNotBlank()) {
            val cssFile = File(outputDir, "${appId}_styles.css")
            cssFile.writeText(customCss.value)
        }
    }

    private fun getExtensionScript(module: ExtensionModule): String {
        return when (module) {
            ExtensionModule.DARK_MODE -> """
                (function() {
                    const style = document.createElement('style');
                    style.textContent = `
                        html { filter: invert(1) hue-rotate(180deg); }
                        img, video, picture, canvas, [style*="background-image"] { filter: invert(1) hue-rotate(180deg); }
                    `;
                    document.head.appendChild(style);
                })();
            """.trimIndent()

            ExtensionModule.AD_BLOCKER -> """
                (function() {
                    const adSelectors = [
                        '[class*="ad-"]', '[class*="ads-"]', '[class*="advertisement"]',
                        '[id*="ad-"]', '[id*="ads-"]', 'ins.adsbygoogle',
                        '[data-ad]', '.sponsored', '.ad-container'
                    ];
                    function hideAds() {
                        adSelectors.forEach(sel => {
                            document.querySelectorAll(sel).forEach(el => el.style.display = 'none');
                        });
                    }
                    hideAds();
                    new MutationObserver(hideAds).observe(document.body, {childList: true, subtree: true});
                })();
            """.trimIndent()

            ExtensionModule.VIDEO_ENHANCER -> """
                (function() {
                    document.querySelectorAll('video').forEach(v => {
                        v.playbackRate = 1.0;
                        v.controls = true;
                    });
                })();
            """.trimIndent()

            ExtensionModule.PRIVACY_PROTECTION -> """
                (function() {
                    // Block tracking
                    Object.defineProperty(navigator, 'sendBeacon', { value: () => false });
                    window.ga = window.gtag = function() {};
                })();
            """.trimIndent()

            ExtensionModule.CONTENT_ENHANCER -> """
                (function() {
                    // Enable text selection
                    document.body.style.userSelect = 'auto';
                    document.body.style.webkitUserSelect = 'auto';
                })();
            """.trimIndent()

            else -> "// ${module.displayName} module"
        }
    }

    private fun saveWebApp(webApp: WebApp) {
        val currentList = _savedWebApps.value.toMutableList()
        currentList.add(webApp)
        _savedWebApps.value = currentList

        // Save to SharedPreferences
        val prefs = context.getSharedPreferences("webtoapp_prefs", Context.MODE_PRIVATE)
        val webAppsJson = currentList.map { it.id }.toSet()
        prefs.edit().putStringSet("saved_webapp_ids", webAppsJson.toMutableSet()).apply()
    }

    private fun loadSavedWebApps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("webtoapp_prefs", Context.MODE_PRIVATE)
                val savedIds = prefs.getStringSet("saved_webapp_ids", emptySet()) ?: emptySet()

                val outputDir = File(context.getExternalFilesDir(null), "webtoapp")
                val loadedApps = mutableListOf<WebApp>()

                savedIds.forEach { id ->
                    val configFile = File(outputDir, "${id}_config.json")
                    if (configFile.exists()) {
                        try {
                            val webApp = json.decodeFromString<WebApp>(configFile.readText())
                            loadedApps.add(webApp)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to load web app $id", e)
                        }
                    }
                }

                _savedWebApps.value = loadedApps
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load saved web apps", e)
            }
        }
    }

    fun deleteWebApp(webApp: WebApp) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val outputDir = File(context.getExternalFilesDir(null), "webtoapp")
                File(outputDir, "${webApp.id}_config.json").delete()
                File(outputDir, "${webApp.id}_icon.png").delete()
                File(outputDir, "${webApp.id}_splash.png").delete()
                File(outputDir, "${webApp.id}_extensions.js").delete()
                File(outputDir, "${webApp.id}_styles.css").delete()

                val currentList = _savedWebApps.value.toMutableList()
                currentList.removeAll { it.id == webApp.id }
                _savedWebApps.value = currentList

                val prefs = context.getSharedPreferences("webtoapp_prefs", Context.MODE_PRIVATE)
                val savedIds = prefs.getStringSet("saved_webapp_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
                savedIds.remove(webApp.id)
                prefs.edit().putStringSet("saved_webapp_ids", savedIds).apply()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete web app", e)
            }
        }
    }

    fun resetBuildState() {
        _buildState.value = WebAppBuildState.Idle
    }

    private fun resetForm() {
        appName.value = ""
        websiteUrl.value = ""
        packageName.value = ""
        _selectedIconUri.value = null
        _splashImageUri.value = null
        _bgmUri.value = null

        // Reset all settings to defaults
        enableJavaScript.value = true
        enableDomStorage.value = true
        enableFileAccess.value = false
        enableGeolocation.value = false
        enableZoom.value = true
        enableFullscreen.value = false
        enableDesktopMode.value = false
        enableDarkMode.value = false
        forceDarkMode.value = false
        screenOrientation.value = ScreenOrientation.UNSPECIFIED
        keepScreenOn.value = false
        hideStatusBar.value = false
        hideNavigationBar.value = false
        immersiveMode.value = false
        enableAdBlock.value = true
        enablePopupBlock.value = true
        enableAntiTracking.value = false
        enableFingerprintSpoofing.value = false
        enableCookieManager.value = true
        thirdPartyCookies.value = false
        clearCacheOnExit.value = false
        clearCookiesOnExit.value = false
        enableIncognitoMode.value = false
        userAgentType.value = UserAgentType.DEFAULT
        customUserAgent.value = ""
        enablePullToRefresh.value = true
        enableSwipeNavigation.value = false
        enableBackButtonExit.value = true
        confirmExit.value = false
        openExternalLinksInBrowser.value = true
        enableDownloads.value = true
        enableMediaPlayback.value = true
        enableVideoFullscreen.value = true
        muteAudio.value = false
        autoplayMedia.value = false
        enableSplashScreen.value = false
        splashDurationMs.value = 3000L
        splashBackgroundColor.value = "#FFFFFF"
        enableBgm.value = false
        bgmVolume.value = 0.5f
        bgmLoop.value = true
        bgmAutoPlay.value = true
        enableActivation.value = false
        activationCode.value = ""
        activationMessage.value = "Please enter activation code"
        enableAnnouncement.value = false
        announcementTitle.value = ""
        announcementContent.value = ""
        announcementButtonText.value = "OK"
        announcementUrl.value = ""
        announcementTemplate.value = AnnouncementTemplate.DEFAULT
        showAnnouncementOnce.value = true
        enableAutoStart.value = false
        enableScheduledStart.value = false
        scheduledStartTime.value = ""
        scheduledEndTime.value = ""
        enableForcedRun.value = false
        blockBackButton.value = false
        blockHomeButton.value = false
        blockRecentApps.value = false
        forcedRunDurationMinutes.value = 0
        enabledExtensions.value = emptySet()
        customJavaScript.value = ""
        customCss.value = ""
        injectScriptAtStart.value = true
        enableIsolation.value = false
        enableEncryption.value = false
        enableAutoTranslate.value = false
        translateTargetLanguage.value = "en"
    }
}

/**
 * Settings sections for UI navigation
 */
enum class SettingsSection {
    BASIC,
    DISPLAY,
    PRIVACY,
    NAVIGATION,
    MEDIA,
    SPLASH,
    BGM,
    ACTIVATION,
    ANNOUNCEMENT,
    AUTO_START,
    FORCED_RUN,
    EXTENSIONS,
    ADVANCED
}
