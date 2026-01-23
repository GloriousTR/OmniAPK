package com.omniapk.utils

import android.content.Context
import com.omniapk.utils.install.InstallStrategy
import com.omniapk.utils.install.RootInstallStrategy
import com.omniapk.utils.install.ShizukuInstallStrategy
import com.omniapk.utils.install.StandardInstallStrategy
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

enum class InstallMethod {
    STANDARD,
    ROOT,
    SHIZUKU
}

class AppInstaller @Inject constructor(
    @ApplicationContext private val context: Context,
    private val standardStrategy: StandardInstallStrategy,
    private val rootStrategy: RootInstallStrategy,
    private val shizukuStrategy: ShizukuInstallStrategy,
    private val splitInstallStrategy: SplitInstallStrategy
) {
    suspend fun installApk(fileName: String, method: InstallMethod = InstallMethod.STANDARD): Boolean {
        val file = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), fileName)
        if (!file.exists()) return false

        val isSplit = file.name.endsWith(".apks", true) || 
                      file.name.endsWith(".xapk", true) || 
                      file.name.endsWith(".apkm", true)

        val strategy: InstallStrategy = when (method) {
            InstallMethod.ROOT -> rootStrategy
            InstallMethod.SHIZUKU -> shizukuStrategy
            InstallMethod.STANDARD -> if (isSplit) splitInstallStrategy else standardStrategy
        }

        return if (strategy.isSupported()) {
            strategy.install(file)
        } else {
            // Fallback: If split strategy is not supported (e.g. old Android version), we can't really fallback to standard as it won't handle zips.
            if (isSplit && !splitInstallStrategy.isSupported()) return false
            standardStrategy.install(file)
        }
    }
}
