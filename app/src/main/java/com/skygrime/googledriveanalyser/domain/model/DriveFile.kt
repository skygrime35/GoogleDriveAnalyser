package com.skygrime.googledriveanalyser.domain.model

data class DriveFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val size: Long, // in bytes
    val parentId: String?,
    val modifiedTime: Long, // timestamp
    val webViewLink: String? = null
) {
    val isFolder: Boolean
        get() = mimeType == FOLDER_MIME_TYPE

    companion object {
        const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
    }
}
