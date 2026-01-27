package com.aurora.store.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Generic app info model for all sources (Google Play, F-Droid, APKMirror, APKPure)
 */
@Parcelize
data class AppInfo(
    val packageName: String,
    val name: String,
    val versionName: String,
    val versionCode: Int,
    val iconUrl: String,
    val source: String,
    val downloadUrl: String = "",
    val developer: String = "",
    val description: String = "",
    val size: Long = 0,
    val rating: Float = 0f,
    val downloadCount: String = "",
    val lastUpdated: String = ""
) : Parcelable {
    
    companion object {
        const val SOURCE_GOOGLE_PLAY = "Google Play"
        const val SOURCE_FDROID = "F-Droid"
        const val SOURCE_APKMIRROR = "APKMirror"
        const val SOURCE_APKPURE = "APKPure"
    }
}
