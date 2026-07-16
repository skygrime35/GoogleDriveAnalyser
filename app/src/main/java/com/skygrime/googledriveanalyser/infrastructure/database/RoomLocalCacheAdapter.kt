package com.skygrime.googledriveanalyser.infrastructure.database

import com.skygrime.googledriveanalyser.domain.model.DriveFile
import com.skygrime.googledriveanalyser.domain.repository.LocalCachePort

class RoomLocalCacheAdapter(private val driveFileDao: DriveFileDao) : LocalCachePort {
    override suspend fun saveFiles(files: List<DriveFile>) {
        val entities = files.map { DriveFileEntity.fromDomain(it) }
        driveFileDao.insertFiles(entities)
    }

    override suspend fun getCachedFiles(): List<DriveFile> {
        return driveFileDao.getAllFiles().map { it.toDomain() }
    }

    override suspend fun clearCache() {
        driveFileDao.deleteAllFiles()
    }
}
