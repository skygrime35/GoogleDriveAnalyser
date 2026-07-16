package com.skygrime.googledriveanalyser.domain.repository

import com.skygrime.googledriveanalyser.domain.model.DriveFile

interface LocalCachePort {
    suspend fun saveFiles(files: List<DriveFile>)
    suspend fun getCachedFiles(): List<DriveFile>
    suspend fun clearCache()
}
