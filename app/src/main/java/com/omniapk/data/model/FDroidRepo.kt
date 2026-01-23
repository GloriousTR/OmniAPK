package com.omniapk.data.model

/**
 * F-Droid repository model
 * Based on user's Droid-ify repository list
 */
data class FDroidRepo(
    val id: String,
    val name: String,
    val address: String,
    val description: String = "",
    val fingerprint: String = "",
    val enabled: Boolean = true
) {
    companion object {
        /**
         * F-Droid repositories from user's Droid-ify app
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
                name = "My First F-Droid Archive Demo",
                address = "https://f-droid.org/archive",
                description = "These are the apps that have been archived from the main repo",
                enabled = false
            ),
            
            // Security & Privacy
            FDroidRepo(
                id = "bitwarden",
                name = "Bitwarden F-Droid",
                address = "https://mobileapp.bitwarden.com/fdroid/repo",
                description = "A repository of Bitwarden apps to be used with F-Droid"
            ),
            FDroidRepo(
                id = "guardian",
                name = "Guardian Project",
                address = "https://guardianproject.info/fdroid/repo",
                description = "The official repository of The Guardian Project apps"
            ),
            FDroidRepo(
                id = "guardian-archive",
                name = "Guardian Project Archive",
                address = "https://guardianproject.info/fdroid/archive",
                description = "Older versions of Guardian Project applications",
                enabled = false
            ),
            FDroidRepo(
                id = "briar",
                name = "Briar Project Repo",
                address = "https://briarproject.org/fdroid/repo",
                description = "Secure messaging app repository"
            ),
            FDroidRepo(
                id = "session",
                name = "Session F-Droid",
                address = "https://fdroid.getsession.org/fdroid/repo",
                description = "Private messenger repository"
            ),
            FDroidRepo(
                id = "molly",
                name = "Molly F-Droid repo",
                address = "https://molly.im/fdroid/foss/repo",
                description = "Signal fork with extra security features"
            ),
            FDroidRepo(
                id = "cryptomator",
                name = "Cryptomator F-Droid Repo",
                address = "https://static.cryptomator.org/android/fdroid/repo",
                description = "Cloud encryption app repository"
            ),
            
            // Browsers & Web
            FDroidRepo(
                id = "ironfox",
                name = "IronFox OSS",
                address = "https://gitlab.com/niconicoxx/niconicoxx.gitlab.io/-/raw/main/niconicoxx/repo",
                description = "Firefox-based browser repository"
            ),
            FDroidRepo(
                id = "vanadium",
                name = "Vanadium",
                address = "https://nicoe.cc/vanadium-fdroid-repo/repo",
                description = "Chromium-based browser for Android"
            ),
            
            // IzzyOnDroid
            FDroidRepo(
                id = "izzyondroid",
                name = "IzzyOnDroid F-Droid Repo",
                address = "https://apt.izzysoft.de/fdroid/repo",
                description = "Large collection of open source apps"
            ),
            
            // NewPipe & Media
            FDroidRepo(
                id = "newpipe",
                name = "NewPipe upstream releases",
                address = "https://archive.newpipe.net/fdroid/repo",
                description = "YouTube alternative with downloads"
            ),
            
            // MicroG
            FDroidRepo(
                id = "microg",
                name = "microG F-Droid repo",
                address = "https://microg.org/fdroid/repo",
                description = "microG Project apps for Google-free experience"
            ),
            FDroidRepo(
                id = "microg-alt",
                name = "MicroG F-Droid Repo",
                address = "https://microg.org/fdroid/repo",
                description = "Alternative microG repository"
            ),
            
            // DivestOS & CalyxOS
            FDroidRepo(
                id = "divestos",
                name = "DivestOS F-Droid",
                address = "https://divestos.org/apks/official/fdroid/repo",
                description = "DivestOS official applications"
            ),
            FDroidRepo(
                id = "calyx",
                name = "The Calyx Institute F-Droid repo (testing)",
                address = "https://calyxos.gitlab.io/calyx-fdroid-repo/fdroid/repo",
                description = "CalyxOS testing repository"
            ),
            
            // Productivity & Office
            FDroidRepo(
                id = "collabora",
                name = "Collabora Office F-Droid Repo",
                address = "https://www.collaboraoffice.com/downloads/fdroid/repo",
                description = "LibreOffice-based office suite"
            ),
            FDroidRepo(
                id = "fedilab",
                name = "Fedilab Apps",
                address = "https://fdroid.fedilab.app/repo",
                description = "Fediverse client applications"
            ),
            FDroidRepo(
                id = "thunderbird",
                name = "Thunderbird F-Droid Repo",
                address = "https://packages.thunderbird.net/m/repo",
                description = "Mozilla Thunderbird email client"
            ),
            
            // Crypto & Wallets
            FDroidRepo(
                id = "cakewallet",
                name = "CakeWallet official repo",
                address = "https://fdroid.cakelabs.com/fdroid/repo",
                description = "Monero/Bitcoin wallet repository"
            ),
            
            // FUTO
            FDroidRepo(
                id = "futo",
                name = "FUTO F-Droid Repo",
                address = "https://app.futo.org/fdroid/repo",
                description = "Software created by FUTO"
            ),
            
            // Weather
            FDroidRepo(
                id = "breezy",
                name = "Breezy Weather",
                address = "https://breezy-weather.github.io/fdroid-repo/fdroid/repo",
                description = "Weather app F-Droid repository"
            ),
            
            // KDE
            FDroidRepo(
                id = "kde",
                name = "KDE Android Release Pipeline",
                address = "https://cdn.kde.org/android/fdroid/repo",
                description = "KDE applications for Android"
            ),
            
            // Kagi
            FDroidRepo(
                id = "kagi",
                name = "Kagi Android App Store",
                address = "https://kagifeedback.org/uploads/short-url/c5dPqUoKV1ATujZHw9aQBKxKLOg.jar",
                description = "Kagi search engine apps"
            ),
            
            // Obtainium
            FDroidRepo(
                id = "obtainium",
                name = "Obtainium",
                address = "https://apps.obtainium.imranr.dev/fdroid/repo",
                description = "App updater for direct installs"
            ),
            
            // Other repos
            FDroidRepo(
                id = "nanna",
                name = "Nanna F-Droid Repo",
                address = "https://nanna.is-a.dev/fdroid/repo",
                description = "Nanna personal repository"
            ),
            FDroidRepo(
                id = "netsyms",
                name = "Netsyms F-Droid Apps Repository",
                address = "https://repo.netsyms.com/fdroid/repo",
                description = "Netsyms Technologies applications"
            ),
            FDroidRepo(
                id = "petercxy",
                name = "Peter Cxy's F-Droid Repo",
                address = "https://fdroid.typeblog.net/repo",
                description = "Peter Cxy's personal repository"
            ),
            FDroidRepo(
                id = "splitapk",
                name = "Split APK Apps",
                address = "https://nicoe.cc/split-apk-apps-fdroid-repo/repo",
                description = "Repository for split APK applications"
            ),
            FDroidRepo(
                id = "simplechat",
                name = "Simple Chat - Uncut",
                address = "https://nicoe.cc/simplechat-fdroid-repo/repo",
                description = "Simple Chat uncut version"
            ),
            FDroidRepo(
                id = "stevenblack",
                name = "StevenBlack hostlist mirror",
                address = "https://nicoe.cc/stevenblack-hostlist-fdroid-repo/repo",
                description = "Hosts file for ad blocking"
            ),
            FDroidRepo(
                id = "merry-foss",
                name = "Merry F-Droid Repo(FOSS)",
                address = "https://nicoe.cc/merry-fdroid-repo/repo",
                description = "Merry FOSS applications"
            ),
            FDroidRepo(
                id = "keepass2android",
                name = "Keepass2Android Plugins",
                address = "https://nicoe.cc/keepass2android-plugins-fdroid-repo/repo",
                description = "KeePass2Android plugin repository"
            ),
            FDroidRepo(
                id = "verificatum",
                name = "Verificatum",
                address = "https://nicoe.cc/verificatum-fdroid-repo/repo",
                description = "Verificatum verification apps"
            ),
            FDroidRepo(
                id = "zap",
                name = "Zap for Droid Repository",
                address = "https://nicoe.cc/zap-fdroid-repo/repo",
                description = "Zap Lightning wallet"
            ),
            FDroidRepo(
                id = "oandbackupx",
                name = "OAndBackupX - devel repo",
                address = "https://nicoe.cc/oandbackupx-devel-fdroid-repo/repo",
                description = "OAndBackupX development builds"
            )
        )
    }
}
