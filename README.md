# OmniAPK

**OmniAPK** is a feature-rich, open-source alternative app store for Android that combines Google Play Store access with F-Droid repository support, giving you the best of both worlds.

## Features

- 🎨 **Beautiful Design**: Built with Material 3 design guidelines
- 🔐 **Flexible Login**: Use your personal Google account or anonymous access
- 📱 **Device Spoofing**: Change device profile to access geo-locked apps
- 🛡️ **Privacy-Focused**: [Exodus Privacy](https://exodus-privacy.eu.org/) integration shows trackers in apps
- 🔓 **Open Source Apps**: Integrated F-Droid repository browser with sync support
- ⬇️ **Download Manager**: Reliable background downloads with progress tracking
- 🔄 **Auto Updates**: Automatic app update checking and installation
- 📋 **Blacklist**: Ignore updates for specific apps

## Downloads

Download the latest release from [GitHub Releases](https://github.com/GloriousTR/OmniAPK/releases).

## F-Droid Integration

OmniAPK includes built-in support for F-Droid repositories:

1. Go to **Settings → F-Droid Repositories**
2. Enable the repositories you want
3. Tap **Sync** to download the app index
4. Browse open-source apps in the **Open-Source** tab

### Default Repositories
- F-Droid Official
- IzzyOnDroid
- Guardian Project
- DivestOS
- Calyx Institute
- And more...

## Building from Source

```bash
git clone https://github.com/GloriousTR/OmniAPK.git
cd OmniAPK
./gradlew assembleVanillaRelease
```

## Limitations

- The underlying API is reverse-engineered from Google Play, changes may break it
- Cannot download or update paid apps
- Cannot update apps with [Play Asset Delivery](https://developer.android.com/guide/playcore/asset-delivery)
- Some features require Google account login (Library, Purchase History, Beta Programs)

## License

OmniAPK is licensed under the [GNU General Public License v3.0](LICENSE).

## Credits

OmniAPK is based on [Aurora Store](https://gitlab.com/AuroraOSS/AuroraStore) and these projects:
- [YalpStore](https://github.com/yeriomin/YalpStore)
- [AppCrawler](https://github.com/Akdeniz/google-play-crawler)
- [Raccoon](https://github.com/onyxbits/raccoon4)
- [SAI](https://github.com/Aefyr/SAI)
