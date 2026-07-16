# Database Local Cache Documentation

The offline capability is implemented via **Room Database** in the infrastructure adapter layer.

## Architecture

```
Domain Use Case  -->  LocalCachePort (Port)  <--  RoomLocalCacheAdapter (Adapter)
                                                           |
                                                      AppDatabase
                                                           |
                                                      DriveFileDao
                                                           |
                                                   DriveFileEntity (SQLite)
```

---

## Database Components

### 1. DriveFileEntity
* **Location:** `com.skygrime.googledriveanalyser.infrastructure.database.DriveFileEntity`
* **Purpose:** SQLite table schema for storing file/folder metadata.
* **Table Name:** `drive_files`
* **Primary Key:** `id`
* **Mappers:**
  * `toDomain()`: Converts database entity to the pure `DriveFile` domain model.
  * `fromDomain(DriveFile)`: Converts a `DriveFile` domain model into `DriveFileEntity`.

### 2. DriveFileDao
* **Location:** `com.skygrime.googledriveanalyser.infrastructure.database.DriveFileDao`
* **Queries:**
  * `getAllFiles()`: Returns `List<DriveFileEntity>` matching all rows in the database.
  * `insertFiles(List<DriveFileEntity>)`: Inserts or replaces a list of entities in a single transaction.
  * `deleteAllFiles()`: Wipes the table (used to clear cache before a new sync).

### 3. AppDatabase
* **Location:** `com.skygrime.googledriveanalyser.infrastructure.database.AppDatabase`
* **Purpose:** Configures Room with versioning (`version = 1`) and singleton thread-safe DB access using `Room.databaseBuilder`.
* **Database File Name:** `drive_analyser_db`

### 4. RoomLocalCacheAdapter
* **Location:** `com.skygrime.googledriveanalyser.infrastructure.database.RoomLocalCacheAdapter`
* **Implementation:** Implements the domain's `LocalCachePort` interface.
* **Thread Safety:** Standard Room query execution handles dispatching transactions efficiently on background threads.
