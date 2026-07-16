package com.skygrime.googledriveanalyser.infrastructure.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DriveFileDao {
    @Query("SELECT * FROM drive_files")
    suspend fun getAllFiles(): List<DriveFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<DriveFileEntity>)

    @Query("DELETE FROM drive_files")
    suspend fun deleteAllFiles()
}
