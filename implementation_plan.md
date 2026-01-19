# Universal App Manager - Implementation Plan

## Goal Description
Create a unified Android application that manages installed apps by checking for updates across multiple sources (Google Play Store, APKMirror, APKPure) and allows searching and installing new apps.

## User Review Required
> [!IMPORTANT]
> **Build Environment**: Since you do not have Android Studio locally, **all builds will be handled via GitHub Actions**. I will create a workflow that builds the debug APK and makes it available as an artifact for you to download and install on your phone.
> **Scraping**: We will proceed with scraping strategies for APKMirror and APKPure, and version checking for Play Store.

## Proposed Changes

### CI/CD Pipeline
- **GitHub Actions**: A `.github/workflows/android.yml` file will be created to:
    - **Release Workflow**:
        - **Versioning**: Auto-calculate version from Git tags (e.g., `v1.0.1`).
        - **Renaming**: Rename `app-debug.apk` to `OmniAPK_v{version}.apk`.
        - **Releasing**: Use `softprops/action-gh-release@v2` to publish the APK to GitHub Releases.
    - Set up JDK 17.
    - Setup Android SDK.
    - Grant execute permissions to `gradlew`.
    - Build with `./gradlew assembleDebug`.
    - Upload the resulting APK as an artifact.

### Architecture
We will use **Native Android (Kotlin)** with **MVVM** architecture.

### Dependencies
- **Jsoup**: For parsing HTML from APKMirror/APKPure.
- **Retrofit/OkHttp**: For networking and downloads.
- **Coroutines/Flow**: For asynchronous operations.
- **Hilt**: For dependency injection.
- **Hilt**: For dependency injection.
- **Coil**: For image loading.
- **Shizuku API**: For non-root system-level installation.

### Core Components

#### 1. Package Manager Module
- `AppRepository`: Fetches list of installed packages.

#### 2. Source Providers
- `SourceProvider` (Interface)
- `ApkMirrorProvider`: Scraping logic.
- `ApkPureProvider`: Scraping logic.
- `PlayStoreProvider`: Version checking only.

#### 3. Update Manager
- Compares local versions with remote versions.

#### 4. Install Manager (Advanced)
- **Standard**: `Intent(ACTION_VIEW)` (User interaction required).
- **Root**: `su -c pm install ...` (Silent).
- **Shizuku**: Uses `PackageInstaller` via Shizuku binder (Silent/Less intrusive).

#### 5. UI Layer
- **Dashboard**: Installed apps list.
- **Search**: Global search with "Install" button.
- **App Details**: Description, screenshots, "Update/Install" button with progress bar.

### Directory Structure
```
.github/
└── workflows/
    └── android.yml  <-- CRITICAL for your workflow
app/src/main/java/com/example/universalappmanager/
├── data/
│   ├── model/ 
│   ├── repository/
│   └── sources/
├── di/
├── ui/
│   ├── home/
│   ├── search/
│   └── details/
├── utils/
└── MainActivity.kt
```

## Verification Plan

### Automated Tests
- Unit tests run within the CI/CD pipeline.

### Manual Verification
- You will download the APK from the GitHub Actions run.
- Install it on your physical device.
- Verify features (Listing, Search, Update Check).
