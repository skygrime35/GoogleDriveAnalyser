# Domain Layer Documentation

The domain layer contains the pure, platform-independent business models, repository interfaces (ports), and business use cases.

## Core Models

### 1. DriveFile
* **Location:** `com.skygrime.googledriveanalyser.domain.model.DriveFile`
* **Purpose:** Represents the flat metadata of a file or folder retrieved from Google Drive.
* **Fields:**
  * `id`: String (Unique file identifier)
  * `name`: String (File name)
  * `mimeType`: String (MIME Type)
  * `size`: Long (File size in bytes)
  * `parentId`: String? (ID of the parent folder)
  * `modifiedTime`: Long (Last modification timestamp)
  * `webViewLink`: String? (Optional web link to open the file)
* **Derived Properties:**
  * `isFolder`: Boolean (checks if mimeType equals `application/vnd.google-apps.folder`)

### 2. DriveNode
* **Location:** `com.skygrime.googledriveanalyser.domain.model.DriveNode`
* **Purpose:** A node in the hierarchical tree structure. Used to render the interactive tree.
* **Fields:**
  * `file`: DriveFile
  * `path`: String (Full folder path from the virtual root)
  * `resolvedSize`: Long (If it's a folder, this is the recursive sum of its children's sizes. If it's a file, this is the file size.)
  * `children`: List<DriveNode> (List of child nodes)
  * `fileCount`: Int (Number of recursive files inside)
  * `folderCount`: Int (Number of recursive folders inside)

### 3. DriveStats & FileCategory
* **Location:** `com.skygrime.googledriveanalyser.domain.model.DriveStats`
* **Purpose:** Summarizes storage space usage. Used to render dashboard stats cards and the Donut chart.
* **FileCategory:** Groups mimeTypes into classifications: `IMAGES`, `VIDEOS`, `AUDIO`, `DOCUMENTS`, `ARCHIVES`, and `OTHER`.

---

## Core Use Cases

### 1. SyncDriveUseCase
* **Location:** `com.skygrime.googledriveanalyser.domain.usecase.SyncDriveUseCase`
* **Flow:**
  1. Calls the `DriveServicePort` to fetch all non-trashed files.
  2. Clears the local SQLite database cache.
  3. Inserts the newly fetched list of files into the cache.
  4. Returns the list of files.

### 2. AnalyzeStorageUseCase
* **Location:** `com.skygrime.googledriveanalyser.domain.usecase.AnalyzeStorageUseCase`
* **Operations:**
  * **Tree Building:** Maps flat files by parent ID, resolves recursive folder sizes by summing children sizes, and encapsulates them under a single virtual root node (`My Drive`).
  * **Search Filtering:** Recursively traverses the tree. Keeps a node if its name matches the query, or if any of its children match.
  * **Sorting:** Sorts tree children levels based on `SortOption` (Size Descending/Ascending, Name A-Z/Z-A, Date Newest/Oldest).
  * **Stats Generation:** Excludes folders from size sums to prevent double-counting, compiles total counts, and segments size usage by `FileCategory`.
