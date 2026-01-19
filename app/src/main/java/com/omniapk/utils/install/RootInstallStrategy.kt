package com.omniapk.utils.install

import java.io.File
import javax.inject.Inject

class RootInstallStrategy @Inject constructor() : InstallStrategy {

    override fun isSupported(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c id")
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun install(file: File): Boolean {
        return try {
            // pm install -r <path>
            // -r: reinstall if needed
            val command = "pm install -r \"${file.absolutePath}\""
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
