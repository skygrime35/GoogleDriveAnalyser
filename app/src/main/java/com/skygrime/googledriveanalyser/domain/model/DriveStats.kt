package com.skygrime.googledriveanalyser.domain.model

data class DriveStats(
    val totalSize: Long,
    val totalFoldersCount: Int,
    val totalFilesCount: Int,
    val sizeByCategory: Map<FileCategory, Long>,
    val fileCountByCategory: Map<FileCategory, Int>
)

enum class FileCategory {
    IMAGES,
    VIDEOS,
    AUDIO,
    DOCUMENTS,
    ARCHIVES,
    OTHER;

    companion object {
        fun fromMimeType(mimeType: String): FileCategory {
            return when {
                mimeType.startsWith("image/") -> IMAGES
                mimeType.startsWith("video/") -> VIDEOS
                mimeType.startsWith("audio/") -> AUDIO
                mimeType.contains("pdf") ||
                mimeType.contains("document") ||
                mimeType.contains("sheet") ||
                mimeType.contains("presentation") ||
                mimeType.contains("text/") ||
                mimeType == "application/msword" ||
                mimeType == "application/vnd.ms-excel" ||
                mimeType == "application/vnd.ms-powerpoint" -> DOCUMENTS
                mimeType.contains("zip") ||
                mimeType.contains("rar") ||
                mimeType.contains("tar") ||
                mimeType.contains("gzip") ||
                mimeType.contains("x-7z") -> ARCHIVES
                else -> OTHER
            }
        }
    }
}
