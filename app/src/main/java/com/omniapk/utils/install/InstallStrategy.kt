package com.omniapk.utils.install

import java.io.File

interface InstallStrategy {
    /**
     * Attempts to install the APK found at [file].
     * Returns true if the installation process was successfully started (or completed for sync methods).
     */
    suspend fun install(file: File): Boolean
    
    /**
     * Returns true if this strategy is supported/available on the current device.
     */
    fun isSupported(): Boolean
}
