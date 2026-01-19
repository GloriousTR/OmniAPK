package com.omniapk.utils.install

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import rikka.shizuku.Shizuku
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class ShizukuInstallStrategy @Inject constructor(
    @ApplicationContext private val context: Context
) : InstallStrategy {

    override fun isSupported(): Boolean {
        return try {
            if (Shizuku.pingBinder()) {
                val permission = if (Build.VERSION.SDK_INT >= 23) {
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                } else {
                    true // Older versions might differ, but safe bet
                }
                permission
            } else {
                false
            }
        } catch (e: Throwable) {
            false
        }
    }

    override suspend fun install(file: File): Boolean {
        // Implementation detail: Using Shizuku to install involves PackageInstaller
        // obtained via ShizukuBinderWrapper or similar reflection if needed.
        // However, a simpler way for this proof-of-concept might be just command line via Shizuku
        // similar to Root strategy but using Shizuku.newProcess ("pm install ...")
        
        return try {
             val command = "pm install -r \"${file.absolutePath}\""
             val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
             val exitCode = process.waitFor()
             exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
