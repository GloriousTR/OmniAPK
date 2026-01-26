package com.aurora.store.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * F-Droid repository model
 * Adapted from OmniAPK legacy code
 */
@Parcelize
data class FDroidRepo(
    val id: String,
    val name: String,
    val address: String,
    val description: String = "",
    val fingerprint: String = "",
    var enabled: Boolean = true
) : Parcelable {
    companion object {
        /**
         * F-Droid repositories list
         */
        val DEFAULT_REPOS = listOf(
            // Main F-Droid
            FDroidRepo(
                id = "fdroid",
                name = "F-Droid",
                address = "https://f-droid.org/repo",
                description = "The main F-Droid repository with official builds"
            ),
            FDroidRepo(
                id = "fdroid-archive",
                name = "F-Droid Archive",
                address = "https://f-droid.org/archive",
                description = "Archived apps from the main repo",
                enabled = false
            ),
            
            // Security & Privacy
            FDroidRepo(
                id = "guardian",
                name = "Guardian Project",
                address = "https://guardianproject.info/fdroid/repo",
                description = "The official repository of The Guardian Project apps"
            ),
            FDroidRepo(
                id = "bitwarden",
                name = "Bitwarden",
                address = "https://mobileapp.bitwarden.com/fdroid/repo",
                description = "Bitwarden password manager"
            ),
            
            // Browsers
            FDroidRepo(
                id = "chromium",
                name = "Chromium",
                address = "https://ungoogled-software.github.io/ungoogled-chromium-android/fdroid/repo",
                description = "Ungoogled Chromium repository"
            ),
            
            // IzzyOnDroid
            FDroidRepo(
                id = "izzyondroid",
                name = "IzzyOnDroid",
                address = "https://apt.izzysoft.de/fdroid/repo",
                description = "Large collection of open source apps"
            ),
            
            // NewPipe
            FDroidRepo(
                id = "newpipe",
                name = "NewPipe",
                address = "https://archive.newpipe.net/fdroid/repo",
                description = "YouTube alternative"
            ),
            
            // MicroG
            FDroidRepo(
                id = "microg",
                name = "microG",
                address = "https://microg.org/fdroid/repo",
                description = "microG Project apps"
            )
        )
    }
}
