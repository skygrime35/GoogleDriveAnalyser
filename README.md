# Google Drive Analyser (Android)

A modern, secure Android application that connects to your Google Drive and recursively analyzes its storage structure, showing you exactly how much space is consumed by files and folders.

---

## Features

1. **Official Google Sign-In:** Secure authentication using Google Play Services with **read-only** (`drive.readonly`) permission.
2. **Interactive File Tree:** Expand and collapse folders to inspect sizes recursively (e.g. know exactly which folder/file takes up Z gigabytes).
3. **Storage Dashboard:**
   * **Donut Chart:** Visual breakdown of space occupied by Images, Videos, Audio, Documents, Archives, and other types.
   * **Largest Items:** Direct list of the top 10 largest files or folders.
4. **Search & Sorting:**
   * Fast text search of files/folders matching name queries (retaining folder tree paths).
   * Sort children by size (largest/smallest), name (A-Z/Z-A), or modification date (newest/oldest).
5. **Local Cache (Offline Mode):** Metadata is cached locally in a SQLite database (via Room) so you can browse your drive structure offline.
6. **Dark Mode by Default:** Premium dark aesthetic built with Material 3.

---

## Technical Architecture

The application is built using **Hexagonal Architecture (Ports and Adapters)**:
* **Domain (Core):** Pure Kotlin models (`DriveFile`, `DriveNode`, `DriveStats`) and business logic (`AnalyzeStorageUseCase`, `SyncDriveUseCase`). No framework/library dependencies.
* **Ports:** Repository interfaces for database storage (`LocalCachePort`) and network retrieval (`DriveServicePort`).
* **Adapters (Infrastructure):**
  * **Room Database:** Caches Drive metadata locally.
  * **Google Drive SDK:** Integrates OAuth and lists files via the Drive REST API v3 with paging.
  * **Jetpack Compose UI:** Premium Material 3 UI with reactive view-model flow.

---

## How to Build

### Requirements
* OpenJDK 21
* Android SDK (Platforms 34, 35 or 36)
* **Termux Environment Specifics:** If building on an `aarch64` device (like Termux/PRoot), the project uses a native `aapt2` override pointing to `/data/data/com.termux/files/usr/bin/aapt2` inside `gradle.properties`.

### Command Line Build
Use the Gradle Wrapper from the project root:

1. **Build the Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```
   The resulting APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`

2. **Run Unit Tests:**
   ```bash
   ./gradlew test
   ```

3. **Run Lint Checks:**
   ```bash
   ./gradlew lintDebug
   ```

---

## Installation

Once the APK is compiled, you can install it on your Android device:
1. **Copy the APK** to a user-accessible storage folder, or run the following command in Termux:
   ```bash
   termux-open app/build/outputs/apk/debug/app-debug.apk
   ```
2. **Authorize installation** from Termux/your file manager if prompted.
