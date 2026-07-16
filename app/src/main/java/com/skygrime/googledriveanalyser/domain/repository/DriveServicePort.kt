package com.skygrime.googledriveanalyser.domain.repository

import com.skygrime.googledriveanalyser.domain.model.DriveFile

interface DriveServicePort {
    suspend fun fetchAllFiles(): List<DriveFile>
}
