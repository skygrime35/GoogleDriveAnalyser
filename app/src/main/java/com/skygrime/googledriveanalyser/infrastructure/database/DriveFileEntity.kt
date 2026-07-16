package com.skygrime.googledriveanalyser.infrastructure.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skygrime.googledriveanalyser.domain.model.DriveFile

@Entity(tableName = "drive_files")
data class DriveFileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val mimeType: String,
    val size: Long,
    val parentId: String?,
    val modifiedTime: Long,
    val webViewLink: String?
) {
    fun toDomain(): DriveFile {
        return DriveFile(
            id = id,
            name = name,
            mimeType = mimeType,
            size = size,
            parentId = parentId,
            modifiedTime = modifiedTime,
            webViewLink = webViewLink
        )
    }

    companion object {
        fun fromDomain(file: DriveFile): DriveFileEntity {
            return DriveFileEntity(
                id = file.id,
                name = file.name,
                mimeType = file.mimeType,
                size = file.size,
                parentId = file.parentId,
                modifiedTime = file.modifiedTime,
                webViewLink = file.webViewLink
            )
        }
    }
}
