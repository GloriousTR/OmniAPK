package com.omniapk.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val name: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false
)
