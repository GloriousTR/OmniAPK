package com.omniapk.data.model

/**
 * F-Droid repository model
 * Based on Droid-ify repository format
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
         * Default F-Droid repositories (from Droid-ify)
         */
        val DEFAULT_REPOS = listOf(
            FDroidRepo(
                id = "fdroid",
                name = "F-Droid",
                address = "https://f-droid.org/repo",
                description = "The official F-Droid repository",
                fingerprint = "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB"
            ),
            FDroidRepo(
                id = "fdroid-archive",
                name = "F-Droid Archive",
                address = "https://f-droid.org/archive",
                description = "Older versions of apps from the official F-Droid repository",
                fingerprint = "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB",
                enabled = false
            ),
            FDroidRepo(
                id = "izzyondroid",
                name = "IzzyOnDroid",
                address = "https://apt.izzysoft.de/fdroid/repo",
                description = "IzzyOnDroid F-Droid Repository",
                fingerprint = "3BF0D6ABFEAE2F401707B6D966BE743BF0EEE49C2561B9BA39073711F628937A"
            ),
            FDroidRepo(
                id = "guardian",
                name = "Guardian Project",
                address = "https://guardianproject.info/fdroid/repo",
                description = "Guardian Project Official Releases",
                fingerprint = "B7C2EEFD8DAC7806AF67DFCD92EB18126BC08312A7F2D6F3862E46013C7A6135"
            ),
            FDroidRepo(
                id = "bitwarden",
                name = "Bitwarden",
                address = "https://mobileapp.bitwarden.com/fdroid/repo",
                description = "Bitwarden official F-Droid repository",
                fingerprint = "BC54EA6FD1CD5175BCCCC47C561C5726E1C3ED7E686D4A78F8C10B97B4C7CE9F"
            ),
            FDroidRepo(
                id = "bromite",
                name = "Bromite",
                address = "https://fdroid.nicosetteminutiguardando.info/repo",
                description = "Bromite browser F-Droid repository",
                fingerprint = "E1EE5CD076D7B0DC84CB2B45FB78B86DF2EB39A3B6C56BA3DC292A5E0C3B9504",
                enabled = false
            ),
            FDroidRepo(
                id = "newpipe",
                name = "NewPipe",
                address = "https://archive.newpipe.net/fdroid/repo",
                description = "NewPipe official repository",
                fingerprint = "E2401985FDE3C8A2C6ACF7B9CE7B8C71EFB80A5A0C926A22D84816F8F4F5D0BB"
            )
        )
    }
}
