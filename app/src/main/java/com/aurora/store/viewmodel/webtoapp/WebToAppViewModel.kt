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

@HiltViewModel
class WebToAppViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Form fields
    val appName = mutableStateOf("")
    val websiteUrl = mutableStateOf("")
    val enableJavaScript = mutableStateOf(true)
    val enableDesktopMode = mutableStateOf(false)
    val enableDarkMode = mutableStateOf(false)
    val enableFullscreen = mutableStateOf(false)
    val enableAdBlock = mutableStateOf(true)

    // Selected icon URI
    private val _selectedIconUri = MutableStateFlow<Uri?>(null)
    val selectedIconUri = _selectedIconUri.asStateFlow()

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

    fun createWebApp(): WebApp {
        return WebApp(
            name = appName.value.trim(),
            url = normalizeUrl(websiteUrl.value.trim()),
            iconUri = _selectedIconUri.value?.toString(),
            enableJavaScript = enableJavaScript.value,
            enableDesktopMode = enableDesktopMode.value,
            enableDarkMode = enableDarkMode.value,
            enableFullscreen = enableFullscreen.value,
            enableAdBlock = enableAdBlock.value
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

                // Simulate build process
                delay(500)
                _buildState.value = WebAppBuildState.Building(10)

                // Create output directory
                val outputDir = File(context.getExternalFilesDir(null), "webtoapp")
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }

                delay(500)
                _buildState.value = WebAppBuildState.Building(30)

                // Save icon if provided
                _selectedIconUri.value?.let { uri ->
                    try {
                        saveIcon(uri, webApp.id, outputDir)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to save icon", e)
                    }
                }

                delay(500)
                _buildState.value = WebAppBuildState.Building(50)

                // Generate web app configuration
                val configFile = File(outputDir, "${webApp.id}_config.json")
                configFile.writeText(
                    """
                    {
                        "id": "${webApp.id}",
                        "name": "${webApp.name}",
                        "url": "${webApp.url}",
                        "packageName": "${webApp.packageName}",
                        "enableJavaScript": ${webApp.enableJavaScript},
                        "enableDesktopMode": ${webApp.enableDesktopMode},
                        "enableDarkMode": ${webApp.enableDarkMode},
                        "enableFullscreen": ${webApp.enableFullscreen},
                        "enableAdBlock": ${webApp.enableAdBlock}
                    }
                    """.trimIndent()
                )

                delay(500)
                _buildState.value = WebAppBuildState.Building(70)

                // Save to database/preferences
                saveWebApp(webApp)

                delay(500)
                _buildState.value = WebAppBuildState.Building(90)

                delay(300)
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
        }
    }

    private fun saveWebApp(webApp: WebApp) {
        val currentList = _savedWebApps.value.toMutableList()
        currentList.add(webApp)
        _savedWebApps.value = currentList

        // Save to SharedPreferences
        val prefs = context.getSharedPreferences("webtoapp_prefs", Context.MODE_PRIVATE)
        val webAppsJson = currentList.map { it.id }.toSet()
        prefs.edit().putStringSet("saved_webapp_ids", webAppsJson).apply()
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
                        // Parse and load the web app
                        // For now, we'll skip loading as this is a demo
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
        _selectedIconUri.value = null
        enableJavaScript.value = true
        enableDesktopMode.value = false
        enableDarkMode.value = false
        enableFullscreen.value = false
        enableAdBlock.value = true
    }
}
