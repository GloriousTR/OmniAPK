/*
 * OmniAPK - XAPK/Bundle Installer
 * XAPK ve Split APK bundle dosyalarını kurar
 */

package com.aurora.store.data.installer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log
import androidx.core.app.PendingIntentCompat
import com.aurora.extensions.isNAndAbove
import com.aurora.extensions.isOAndAbove
import com.aurora.extensions.isSAndAbove
import com.aurora.extensions.isTAndAbove
import com.aurora.extensions.isUAndAbove
import com.aurora.store.data.receiver.InstallerStatusReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * XAPK ve Bundle (APKs/APKM) dosyalarını kurabilen installer
 */
@Singleton
class XAPKInstaller @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "XAPKInstaller"
    }

    private val packageInstaller = context.packageManager.packageInstaller

    /**
     * XAPK dosyasını kur
     * XAPK formatı: APK + OBB dosyaları içeren ZIP
     */
    suspend fun installXAPK(xapkFile: File): InstallResult = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "XAPK kurulumu başlıyor: ${xapkFile.name}")

            val zipFile = ZipFile(xapkFile)
            val apkEntries = mutableListOf<ZipEntry>()
            var manifestEntry: ZipEntry? = null

            // ZIP içeriğini tara
            zipFile.entries().asSequence().forEach { entry ->
                when {
                    entry.name.endsWith(".apk", ignoreCase = true) -> apkEntries.add(entry)
                    entry.name == "manifest.json" -> manifestEntry = entry
                }
            }

            if (apkEntries.isEmpty()) {
                return@withContext InstallResult.Error("XAPK içinde APK bulunamadı")
            }

            // Split APK olarak kur
            val result = installSplitApks(zipFile, apkEntries)

            zipFile.close()
            result
        } catch (e: Exception) {
            Log.e(TAG, "XAPK kurulum hatası", e)
            InstallResult.Error(e.message ?: "XAPK kurulum hatası")
        }
    }

    /**
     * APKs/APKM bundle dosyasını kur
     * Bu format sadece APK dosyaları içeren ZIP
     */
    suspend fun installBundle(bundleFile: File): InstallResult = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Bundle kurulumu başlıyor: ${bundleFile.name}")

            val zipFile = ZipFile(bundleFile)
            val apkEntries = mutableListOf<ZipEntry>()

            // ZIP içeriğini tara
            zipFile.entries().asSequence().forEach { entry ->
                if (entry.name.endsWith(".apk", ignoreCase = true)) {
                    apkEntries.add(entry)
                }
            }

            if (apkEntries.isEmpty()) {
                return@withContext InstallResult.Error("Bundle içinde APK bulunamadı")
            }

            // Split APK olarak kur
            val result = installSplitApks(zipFile, apkEntries)

            zipFile.close()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Bundle kurulum hatası", e)
            InstallResult.Error(e.message ?: "Bundle kurulum hatası")
        }
    }

    /**
     * Normal APK dosyasını kur
     */
    suspend fun installApk(apkFile: File): InstallResult = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "APK kurulumu başlıyor: ${apkFile.name}")

            val sessionParams = buildSessionParams(getPackageNameFromApk(apkFile))
            val sessionId = packageInstaller.createSession(sessionParams)
            val session = packageInstaller.openSession(sessionId)

            apkFile.inputStream().use { input ->
                session.openWrite(apkFile.name, 0, apkFile.length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }

            session.commit(getCallBackIntent(sessionId))
            session.close()

            InstallResult.Success(sessionId)
        } catch (e: Exception) {
            Log.e(TAG, "APK kurulum hatası", e)
            InstallResult.Error(e.message ?: "APK kurulum hatası")
        }
    }

    /**
     * Dosya türüne göre otomatik kur
     */
    suspend fun installAuto(file: File): InstallResult {
        return when {
            file.name.endsWith(".xapk", ignoreCase = true) -> installXAPK(file)
            file.name.endsWith(".apks", ignoreCase = true) -> installBundle(file)
            file.name.endsWith(".apkm", ignoreCase = true) -> installBundle(file)
            file.name.endsWith(".apk", ignoreCase = true) -> installApk(file)
            else -> InstallResult.Error("Desteklenmeyen dosya formatı: ${file.extension}")
        }
    }

    private fun installSplitApks(
        zipFile: ZipFile,
        apkEntries: List<ZipEntry>
    ): InstallResult {
        val totalSize = apkEntries.sumOf { it.size }
        val baseApk = apkEntries.find {
            it.name.contains("base", ignoreCase = true) ||
                !it.name.contains("split", ignoreCase = true)
        } ?: apkEntries.first()

        // Paket adını base APK'dan al
        val packageName = try {
            val tempFile = File.createTempFile("base", ".apk", context.cacheDir)
            zipFile.getInputStream(baseApk).use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val name = getPackageNameFromApk(tempFile)
            tempFile.delete()
            name
        } catch (e: Exception) {
            "unknown.package"
        }

        val sessionParams = buildSessionParams(packageName).apply {
            setSize(totalSize)
        }

        val sessionId = packageInstaller.createSession(sessionParams)
        val session = packageInstaller.openSession(sessionId)

        try {
            apkEntries.forEach { entry ->
                Log.i(TAG, "Split APK yazılıyor: ${entry.name}")
                val entryName = entry.name.substringAfterLast("/")
                zipFile.getInputStream(entry).use { input ->
                    session.openWrite(entryName, 0, entry.size).use { output ->
                        input.copyTo(output)
                        session.fsync(output)
                    }
                }
            }

            session.commit(getCallBackIntent(sessionId))
            session.close()

            return InstallResult.Success(sessionId)
        } catch (e: Exception) {
            session.abandon()
            throw e
        }
    }

    private fun getPackageNameFromApk(apkFile: File): String {
        return try {
            val packageInfo = context.packageManager.getPackageArchiveInfo(
                apkFile.absolutePath,
                PackageManager.GET_META_DATA
            )
            packageInfo?.packageName ?: "unknown.package"
        } catch (e: Exception) {
            "unknown.package"
        }
    }

    private fun buildSessionParams(packageName: String): PackageInstaller.SessionParams =
        PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(packageName)
            setInstallLocation(PackageInfo.INSTALL_LOCATION_AUTO)
            if (isNAndAbove) {
                setOriginatingUid(Process.myUid())
            }
            if (isOAndAbove) {
                setInstallReason(PackageManager.INSTALL_REASON_USER)
            }
            if (isSAndAbove) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            }
            if (isTAndAbove) {
                setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
            }
            if (isUAndAbove) {
                setInstallerPackageName(context.packageName)
            }
        }

    private fun getCallBackIntent(sessionId: Int): IntentSender {
        val callBackIntent = Intent(context, InstallerStatusReceiver::class.java).apply {
            action = AppInstaller.ACTION_INSTALL_STATUS
            setPackage(context.packageName)
            putExtra(PackageInstaller.EXTRA_SESSION_ID, sessionId)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        return PendingIntentCompat.getBroadcast(
            context,
            sessionId,
            callBackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT,
            true
        )!!.intentSender
    }
}

sealed class InstallResult {
    data class Success(val sessionId: Int) : InstallResult()
    data class Error(val message: String) : InstallResult()
}
