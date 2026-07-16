# Google Drive Integration Documentation

This module handles Google Drive authentication and metadata queries.

## Authentication Flow

1. **Sign-In Configuration:**
   * Uses the official **Google Sign-In SDK for Android**.
   * Scope requested is `DriveScopes.DRIVE_READONLY` (`https://www.googleapis.com/auth/drive.readonly`). This ensures user privacy as the app cannot delete or edit files.

2. **Login Callback:**
   * Launching Intent: Activity launches `googleSignInClient.signInIntent` using `ActivityResultContracts.StartActivityForResult`.
   * On Success: Retrieves `GoogleSignInAccount`, saves state as `SignedIn(email)`, and proceeds to retrieve files.

---

## API Query Integration

### 1. GoogleDriveClientBuilder
* **Location:** `com.skygrime.googledriveanalyser.infrastructure.google.GoogleDriveClientBuilder`
* **Purpose:** Builds the raw `com.google.api.services.drive.Drive` client instance.
* **Credentials:** Wraps the signed-in account in `GoogleAccountCredential` using standard OAuth2.
* **Transport:** Employs `NetHttpTransport` and `GsonFactory` for network communications and JSON parsing.

### 2. GoogleDriveServiceAdapter
* **Location:** `com.skygrime.googledriveanalyser.infrastructure.google.GoogleDriveServiceAdapter`
* **Port Implementation:** Implements `DriveServicePort`.
* **Execution details:**
  * Runs on `Dispatchers.IO`.
  * Queries `driveService.files().list()`.
  * **Query String (`q`):** `trashed = false` (ignores deleted items).
  * **Page Size:** 1000 (maximum allowed by Google Drive API to optimize rate limits and reduce network trips).
  * **Page Looping:** Recursively loops utilizing the `nextPageToken` until all metadata is accumulated.
  * **Mime Type & Null Size Handling:** Google native document types (Docs, Sheets, Slides) and Folders have `null` sizes returned by Google Drive. The adapter automatically defaults null sizes to `0L`.
