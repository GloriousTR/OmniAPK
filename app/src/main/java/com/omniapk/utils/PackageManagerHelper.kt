package com.omniapk.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.omniapk.data.model.AppInfo
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class PackageManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        // GET_META_DATA is often sufficient, we can add GET_PERMISSIONS or others if needed later.
        val apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        return apps.map { packageInfo ->
            val isSystemApp = (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            AppInfo(
                packageName = packageInfo.packageName,
                name = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = versionCode,
                icon = packageManager.getApplicationIcon(packageInfo.applicationInfo),
                isSystemApp = isSystemApp
            )
        }
    }
}
