package com.omniapk.utils.install

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Shizuku install strategy - currently stubbed out.
 * Shizuku library is disabled to isolate build issues.
 */
class ShizukuInstallStrategy @Inject constructor(
    @ApplicationContext private val context: Context
) : InstallStrategy {

    override fun isSupported(): Boolean {
        // Shizuku is temporarily disabled
        return false
    }

    override suspend fun install(file: File): Boolean {
        // Shizuku is temporarily disabled
        return false
    }
}
