package com.skygrime.googledriveanalyser.domain.usecase

import com.skygrime.googledriveanalyser.domain.model.DriveFile
import com.skygrime.googledriveanalyser.domain.repository.DriveServicePort
import com.skygrime.googledriveanalyser.domain.repository.LocalCachePort

class SyncDriveUseCase(
    private val driveService: DriveServicePort,
    private val localCache: LocalCachePort
) {
    suspend fun execute(): List<DriveFile> {
        val files = driveService.fetchAllFiles()
        localCache.clearCache()
        localCache.saveFiles(files)
        return files
    }
}
