/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.data.model.webtoapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Model representing a web app configuration for WebToApp feature.
 * This allows users to create standalone apps from websites.
 */
@Parcelize
@Serializable
data class WebApp(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val url: String,
    val packageName: String = "com.webtoapp.${name.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")}",
    val iconUri: String? = null,
    val enableJavaScript: Boolean = true,
    val enableDesktopMode: Boolean = false,
    val enableDarkMode: Boolean = false,
    val enableFullscreen: Boolean = false,
    val enableAdBlock: Boolean = true,
    val userAgent: String? = null,
    val createdAt: Long = System.currentTimeMillis()
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
 * State for WebToApp build process
 */
@Serializable
sealed class WebAppBuildState {
    @Serializable
    data object Idle : WebAppBuildState()
    @Serializable
    data object Preparing : WebAppBuildState()
    @Serializable
    data class Building(val progress: Int) : WebAppBuildState()
    @Serializable
    data class Success(val apkPath: String) : WebAppBuildState()
    @Serializable
    data class Error(val message: String) : WebAppBuildState()
}
