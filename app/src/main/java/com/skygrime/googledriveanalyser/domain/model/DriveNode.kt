package com.skygrime.googledriveanalyser.domain.model

data class DriveNode(
    val file: DriveFile,
    val path: String,
    val resolvedSize: Long, // Recursive size for folders, file size for files
    val children: List<DriveNode> = emptyList(),
    val fileCount: Int = 0,
    val folderCount: Int = 0
)
