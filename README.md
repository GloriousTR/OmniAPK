# OmniAPK

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="128" alt="OmniAPK Logo">
</p>

<p align="center">
  <b>The Ultimate Android App Store</b><br>
  Google Play + APKMirror + APKPure + WebToApp — All in One
</p>

<p align="center">
  <a href="https://github.com/GloriousTR/OmniAPK/releases">
    <img src="https://img.shields.io/github/v/release/GloriousTR/OmniAPK?style=for-the-badge" alt="Latest Release">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/github/license/GloriousTR/OmniAPK?style=for-the-badge" alt="License">
  </a>
</p>

---

**OmniAPK** is a feature-rich, open-source alternative app store for Android that combines Google Play Store access with alternative download sources and powerful WebToApp functionality.

## ✨ Key Features

### 🛒 Multi-Source App Store

- **Google Play Access**: Download apps from Google Play with anonymous or personal account login
- **Alternative Sources**: Direct APKMirror and APKPure integration for hard-to-find apps
- **XAPK Support**: Install split APKs and expansion files seamlessly

### 🌐 WebToApp Engine

Transform any website into a standalone Android app with powerful customization:

| Feature | Description |
|---------|-------------|
| **Ad & Tracker Blocking** | Built-in ad blocker and anti-tracking protection |
| **Dark Mode** | Force dark theme on any website |
| **Custom Scripts** | Tampermonkey-like JavaScript/CSS injection |
| **Extensions** | Video Downloader, Screenshot, Auto Scroll, and more |
| **Privacy Controls** | Incognito mode, cookie management, fingerprint spoofing |
| **Full Customization** | Splash screens, BGM, activation codes, announcements |

### 📥 Alternative Downloads

When an app is unavailable on Play Store, OmniAPK provides:

- **APKMirror Browser**: In-app WebView with 600+ pre-mapped popular apps
- **APKPure Browser**: Alternative source for regional apps

### 🔧 Advanced Features

| Feature | Description |
|---------|-------------|
| 🎨 **Material 3 Design** | Beautiful, modern UI following latest guidelines |
| 📱 **Device Spoofing** | Change device profile to access geo-locked apps |
| 🌍 **Locale Spoofing** | Override system locale for app compatibility |
| 🛡️ **Exodus Privacy** | View trackers embedded in apps before installing |
| ⬇️ **Download Manager** | Reliable background downloads with progress tracking |
| 🔄 **Auto Updates** | Automatic app update checking and installation |
| 📋 **Blacklist** | Ignore updates for specific apps |
| ⭐ **Favourites** | Save apps for quick access |

## 📥 Downloads

Download the latest release from [GitHub Releases](https://github.com/GloriousTR/OmniAPK/releases).

## 🌐 WebToApp Usage

Create standalone apps from any website:

1. Go to **WebToApp** section
2. Enter the website URL
3. Customize settings (dark mode, ad blocking, scripts, etc.)
4. Build and install your custom app

### Extension Modules

| Module | Purpose |
|--------|---------|
| Video Downloader | Detect and download videos from webpages |
| Dark Mode | Force dark theme on all websites |
| Privacy Protection | Block trackers and fingerprinting |
| Ad Blocker | Block ads and popups |
| Video Enhancer | Speed control, PiP, background play |
| Content Enhancer | Force copy, selection translate |
| Element Blocker | Block specified page elements |
| Auto Scroll | Automatic page scrolling |
| Screenshot | Capture full page screenshots |

## 🔨 Building from Source

```bash
git clone https://github.com/GloriousTR/OmniAPK.git
cd OmniAPK
./gradlew assembleVanillaRelease
```

### Build Variants

- `vanillaRelease` - Standard release build
- `huaweiRelease` - Huawei AppGallery compatible build

## ⚠️ Limitations

- The underlying API is reverse-engineered from Google Play; changes may break it
- Cannot download or update paid apps
- Cannot update apps with [Play Asset Delivery](https://developer.android.com/guide/playcore/asset-delivery)
- Some features require Google account login (Library, Purchase History, Beta Programs)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

OmniAPK is licensed under the [GNU General Public License v3.0](LICENSE).

## 🙏 Credits

OmniAPK is based on [Aurora Store](https://gitlab.com/AuroraOSS/AuroraStore) and these projects:

- [YalpStore](https://github.com/yeriomin/YalpStore)
- [AppCrawler](https://github.com/Akdeniz/google-play-crawler)
- [Raccoon](https://github.com/onyxbits/raccoon4)
- [SAI](https://github.com/Aefyr/SAI)
- [web-to-app](https://github.com/AyakorK/web-to-app-pwa)

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/GloriousTR">GloriousTR</a>
</p>
