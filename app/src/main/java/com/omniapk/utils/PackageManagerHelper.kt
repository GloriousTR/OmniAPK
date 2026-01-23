package com.omniapk.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.omniapk.data.model.AppInfo
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class PackageManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        return apps.map { packageInfo ->
            val appInfo = packageInfo.applicationInfo
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            
            // Check if app is a game using category (API 26+)
            val category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category
            } else {
                ApplicationInfo.CATEGORY_UNDEFINED
            }
            
            val isGame = category == ApplicationInfo.CATEGORY_GAME

            AppInfo(
                packageName = packageInfo.packageName,
                name = packageManager.getApplicationLabel(appInfo).toString(),
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = versionCode,
                icon = packageManager.getApplicationIcon(appInfo),
                isSystemApp = isSystemApp,
                isGame = isGame,
                category = category
            )
        }
    }
}
