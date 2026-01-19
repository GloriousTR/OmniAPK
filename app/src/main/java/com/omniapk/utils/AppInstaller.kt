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
    private val shizukuStrategy: ShizukuInstallStrategy
) {
    suspend fun installApk(fileName: String, method: InstallMethod = InstallMethod.STANDARD): Boolean {
        val file = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), fileName)
        if (!file.exists()) return false

        val strategy: InstallStrategy = when (method) {
            InstallMethod.ROOT -> rootStrategy
            InstallMethod.SHIZUKU -> shizukuStrategy
            InstallMethod.STANDARD -> standardStrategy
        }

        return if (strategy.isSupported()) {
            strategy.install(file)
        } else {
            // Fallback to standard if requested method is not supported
            standardStrategy.install(file)
        }
    }
}
