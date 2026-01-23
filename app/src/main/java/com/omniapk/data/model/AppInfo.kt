package com.omniapk.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val name: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Drawable? = null,
    val iconUrl: String? = null,
    val isSystemApp: Boolean = false,
    val isGame: Boolean = false,
    val source: String? = null,
    val description: String? = null,
    val category: Int = 0,
    val screenshots: List<String> = emptyList(),
    val rating: Double = 0.0,
    val downloads: String = "",
    val size: String = ""
)
