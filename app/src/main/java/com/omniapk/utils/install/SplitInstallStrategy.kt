package com.omniapk.utils.install

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

class SplitInstallStrategy @Inject constructor(
    @ApplicationContext private val context: Context
) : InstallStrategy {

    override fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    override suspend fun install(file: File): Boolean = withContext(Dispatchers.IO) {
        if (!isSupported()) return@withContext false

        val packageInstaller = context.packageManager.packageInstaller
        val sessionParams = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        
        var sessionId = -1
        try {
            sessionId = packageInstaller.createSession(sessionParams)
            val session = packageInstaller.openSession(sessionId)

            // APKS/XAPK are essentially ZIP files containing multiple .apk files
            // We need to extract/stream all .apk files from the zip to the session
            val zipFile = ZipFile(file)
            val entries = zipFile.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.endsWith(".apk", ignoreCase = true)) {
                    val inputStream = zipFile.getInputStream(entry)
                    val outStream = session.openWrite(entry.name, 0, entry.size)
                    
                    inputStream.use { input ->
                        outStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    session.fsync(outStream)
                    // outStream is closed by use block
                }
            }
            zipFile.close()

            // Create commit intent
            val intent = Intent(context, SplitInstallReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                sessionId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            
            session.commit(pendingIntent.intentSender)
            session.close()
            true

        } catch (e: Exception) {
            e.printStackTrace()
            if (sessionId != -1) {
                try {
                    packageInstaller.abandonSession(sessionId)
                } catch (ignore: Exception) {}
            }
            false
        }
    }
}
