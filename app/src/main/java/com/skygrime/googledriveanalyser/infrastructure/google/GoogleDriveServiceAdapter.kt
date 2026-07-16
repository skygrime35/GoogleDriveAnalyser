package com.skygrime.googledriveanalyser.infrastructure.google

import com.google.api.services.drive.Drive
import com.skygrime.googledriveanalyser.domain.model.DriveFile
import com.skygrime.googledriveanalyser.domain.repository.DriveServicePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleDriveServiceAdapter(private val driveService: Drive) : DriveServicePort {

    override suspend fun fetchAllFiles(): List<DriveFile> = withContext(Dispatchers.IO) {
        val filesList = mutableListOf<DriveFile>()
        var pageToken: String? = null

        do {
            val result = driveService.files().list().apply {
                q = "trashed = false"
                spaces = "drive"
                fields = "nextPageToken, files(id, name, mimeType, size, parents, modifiedTime, webViewLink)"
                pageSize = 1000
                if (pageToken != null) {
                    this.pageToken = pageToken
                }
            }.execute()

            val files = result.files
            if (files != null) {
                for (file in files) {
                    val parentId = file.parents?.firstOrNull()
                    val size = file.getSize() ?: 0L // Google Docs, Sheets, Folders have null size
                    val modifiedTime = file.modifiedTime?.value ?: 0L

                    filesList.add(
                        DriveFile(
                            id = file.id,
                            name = file.name ?: "Untitled",
                            mimeType = file.mimeType ?: "application/octet-stream",
                            size = size,
                            parentId = parentId,
                            modifiedTime = modifiedTime,
                            webViewLink = file.webViewLink
                        )
                    )
                }
            }
            pageToken = result.nextPageToken
        } while (pageToken != null)

        filesList
    }
}
