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
            // === Main F-Droid ===
            FDroidRepo(
                id = "fdroid",
                name = "F-Droid",
                address = "https://f-droid.org/repo",
                description = "The main F-Droid repository with official builds",
                fingerprint = "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB"
            ),
            FDroidRepo(
                id = "fdroid-archive",
                name = "F-Droid Archive",
                address = "https://f-droid.org/archive",
                description = "Archived apps from the main repo",
                fingerprint = "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB",
                enabled = false
            ),
            
            // === Popular Repositories ===
            FDroidRepo(
                id = "izzyondroid",
                name = "IzzyOnDroid",
                address = "https://apt.izzysoft.de/fdroid/repo",
                description = "Large collection of FOSS apps",
                fingerprint = "3BF0D6ABFEAE2F401707B6D966BE743BF0EEE49C2561B9BA39073711F628937A"
            ),
            FDroidRepo(
                id = "guardian",
                name = "Guardian Project",
                address = "https://guardianproject.info/fdroid/repo",
                description = "Security & privacy apps",
                fingerprint = "B7C2EEFD8DAC7806AF67DFCD92EB18126BC08312A7F2D6F3862E46013C7A6135"
            ),
            FDroidRepo(
                id = "microg",
                name = "microG",
                address = "https://microg.org/fdroid/repo",
                description = "Google services replacement",
                fingerprint = "9BD06727E62796C0130EB6DAB39B73157451582CBD138E86C468ACC395D14165"
            ),
            
            // === Communication ===
            FDroidRepo(
                id = "newpipe",
                name = "NewPipe",
                address = "https://archive.newpipe.net/fdroid/repo",
                description = "YouTube alternative",
                fingerprint = "E2402C78F9B97C6C89E97DB914A2751FDA1D02FE2039CC0897A462BDB57E7501"
            ),
            FDroidRepo(
                id = "molly",
                name = "Molly (Signal FOSS)",
                address = "https://molly.im/fdroid/foss/fdroid/repo",
                description = "Signal fork with enhanced security",
                fingerprint = "5198DAEF37FC23C14D5EE32305B2AF45787BD7DF2034DE33AD302BDB3446DF74"
            ),
            FDroidRepo(
                id = "simplex",
                name = "SimpleX Chat",
                address = "https://app.simplex.chat/fdroid/repo",
                description = "Private decentralized messenger",
                fingerprint = "9F358FF284D1F71656A2BFAF0E005DEAE6AA14143720E089F11FF2DDCFEB01BA"
            ),
            FDroidRepo(
                id = "threema",
                name = "Threema",
                address = "https://releases.threema.ch/fdroid/repo",
                description = "Secure messenger",
                fingerprint = "5734E753899B25775D90FE85362A49866E05AC4F83C05BEF5A92880D2910639E"
            ),
            FDroidRepo(
                id = "briar",
                name = "Briar",
                address = "https://briarproject.org/fdroid/repo",
                description = "Secure P2P messenger",
                fingerprint = "1FB874BEE7276D28ECB2C9B06E8A122EC4BCB4008161436CE474C257CBF49BD6"
            ),
            FDroidRepo(
                id = "session",
                name = "Session",
                address = "https://fdroid.getsession.org/fdroid/repo",
                description = "Anonymous messenger",
                fingerprint = "DB0E5297EB65CC22D6BD93C869943BDCFCB6A07DC69A48A0DD8C7BA698EC04E6"
            ),
            
            // === Browsers ===
            FDroidRepo(
                id = "cromite",
                name = "Cromite",
                address = "https://www.cromite.org/fdroid/repo",
                description = "Privacy-focused Chromium fork",
                fingerprint = "49F37E74DEE483DCA2B991334FB5A0200787430D0B5F9A783DD5F13695E9517B"
            ),
            FDroidRepo(
                id = "brave",
                name = "Brave Browser",
                address = "https://brave-browser-apk-release.s3.brave.com/fdroid/repo",
                description = "Privacy browser with ad blocking",
                fingerprint = "3C60DE135AA19EC949E998469C908F7171885C1E2805F39EB403DDB0F37B4BD2"
            ),
            FDroidRepo(
                id = "ironfox",
                name = "IronFox",
                address = "https://fdroid.ironfoxoss.org/fdroid/repo",
                description = "Firefox fork for privacy",
                fingerprint = "C5E291B5A571F9C8CD9A9799C2C94E02EC9703948893F2CA756D67B94204F904"
            ),
            
            // === Security & Privacy ===
            FDroidRepo(
                id = "bitwarden",
                name = "Bitwarden",
                address = "https://mobileapp.bitwarden.com/fdroid/repo",
                description = "Password manager",
                fingerprint = "BC54EA6FD1CD5175BCCCC47C561C5726E1C3ED7E686B6DB4B18BAC843A3EFE6C"
            ),
            FDroidRepo(
                id = "divestos",
                name = "DivestOS",
                address = "https://divestos.org/fdroid/official",
                description = "De-Googled Android apps",
                fingerprint = "E4BE8D6ABFA4D9D4FEEF03CDDA7FF62A73FD64B75566F6DD4E5E577550BE8467"
            ),
            
            // === Productivity ===
            FDroidRepo(
                id = "collabora",
                name = "Collabora Office",
                address = "https://www.collaboraoffice.com/downloads/fdroid/repo",
                description = "Office suite",
                fingerprint = "573258C84E149B5F4D9299E7434B2B69A8410372921D4AE586BA91EC767892CC"
            ),
            FDroidRepo(
                id = "thunderbird",
                name = "Thunderbird",
                address = "https://thunderbird.github.io/fdroid-thunderbird/repo",
                description = "Email client (K-9 Mail)",
                fingerprint = "8B86E5D48983F0875F7EB7A1B2F91B225EE5B997E463E3D63D0E2556E53666BE"
            ),
            FDroidRepo(
                id = "kde",
                name = "KDE Apps",
                address = "https://cdn.kde.org/android/stable-releases/fdroid/repo",
                description = "KDE applications for Android",
                fingerprint = "13784BA6C80FF4E2181E55C56F961EED5844CEA16870D3B38D58780B85E1158F"
            ),
            
            // === Specialized ===
            FDroidRepo(
                id = "futo",
                name = "FUTO",
                address = "https://app.futo.org/fdroid/repo",
                description = "FUTO apps (Keyboard, Voice Input)",
                fingerprint = "39D47869D29CBFCE4691D9F7E6946A7B6D7E6FF4883497E6E675744ECDFA6D6D"
            ),
            FDroidRepo(
                id = "nethunter",
                name = "Kali NetHunter",
                address = "https://store.nethunter.com/repo",
                description = "Penetration testing tools",
                fingerprint = "7E418D34C3AD4F3C37D7E6B0FACE13332364459C862134EB099A3BDA2CCF4494",
                enabled = false
            ),
            FDroidRepo(
                id = "libretro",
                name = "LibRetro",
                address = "https://fdroid.libretro.com/repo",
                description = "Retro gaming emulators",
                fingerprint = "3F05B24D497515F31FEAB421297C79B19552C5C81186B3750B7C131EF41D733D",
                enabled = false
            ),
            FDroidRepo(
                id = "fedilab",
                name = "Fedilab Apps",
                address = "https://fdroid.fedilab.app/repo",
                description = "Fediverse applications",
                fingerprint = "11F0A69910A4280E2CD3CCC3146337D006BE539B18E1A9FEACE15FF757A94FEB"
            )
        )
    }
}
