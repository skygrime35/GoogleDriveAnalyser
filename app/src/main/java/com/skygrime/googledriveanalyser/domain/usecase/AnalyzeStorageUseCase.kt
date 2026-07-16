package com.skygrime.googledriveanalyser.domain.usecase

import com.skygrime.googledriveanalyser.domain.model.*
import java.util.Locale

class AnalyzeStorageUseCase {

    fun execute(
        files: List<DriveFile>,
        searchQuery: String = "",
        sortBy: SortOption = SortOption.SIZE_DESC
    ): AnalysisResult {
        if (files.isEmpty()) {
            return AnalysisResult(
                rootNode = null,
                stats = DriveStats(0, 0, 0, emptyMap(), emptyMap())
            )
        }

        // 1. Calculate general stats
        val stats = calculateStats(files)

        // 2. Build the full tree
        val fullRoot = buildTree(files)

        // 3. Apply search filtering if active
        val filteredRoot = if (searchQuery.isNotBlank()) {
            filterTree(fullRoot, searchQuery.lowercase(Locale.ROOT))
        } else {
            fullRoot
        }

        // 4. Apply sorting to the tree nodes recursively
        val sortedRoot = filteredRoot?.let { sortTree(it, sortBy) }

        return AnalysisResult(sortedRoot, stats)
    }

    private fun calculateStats(files: List<DriveFile>): DriveStats {
        val nonFolderFiles = files.filter { !it.isFolder }
        val folders = files.filter { it.isFolder }

        val totalSize = nonFolderFiles.sumOf { it.size }
        val totalFoldersCount = folders.size
        val totalFilesCount = nonFolderFiles.size

        val sizeByCategory = mutableMapOf<FileCategory, Long>()
        val countByCategory = mutableMapOf<FileCategory, Int>()

        for (file in nonFolderFiles) {
            val category = FileCategory.fromMimeType(file.mimeType)
            sizeByCategory[category] = sizeByCategory.getOrDefault(category, 0L) + file.size
            countByCategory[category] = countByCategory.getOrDefault(category, 0) + 1
        }

        return DriveStats(
            totalSize = totalSize,
            totalFoldersCount = totalFoldersCount,
            totalFilesCount = totalFilesCount,
            sizeByCategory = sizeByCategory,
            fileCountByCategory = countByCategory
        )
    }

    private fun buildTree(files: List<DriveFile>): DriveNode {
        val fileMap = files.associateBy { it.id }
        val parentToChildrenMap = files.groupBy { it.parentId }

        // Find root files: parentId is null, or parentId is not present in fileMap
        val rootFiles = files.filter { it.parentId == null || !fileMap.containsKey(it.parentId) }

        val visited = mutableSetOf<String>()

        fun buildNode(file: DriveFile, currentPath: String): DriveNode {
            visited.add(file.id)
            val path = if (currentPath.isEmpty()) file.name else "$currentPath/${file.name}"

            if (file.isFolder) {
                val childrenFiles = parentToChildrenMap[file.id] ?: emptyList()
                val childrenNodes = childrenFiles
                    .filter { it.id !in visited } // prevent cycles
                    .map { buildNode(it, path) }

                val resolvedSize = childrenNodes.sumOf { it.resolvedSize }
                val fileCount = childrenNodes.sumOf { it.fileCount }
                val folderCount = childrenNodes.sumOf { it.folderCount + if (it.file.isFolder) 1 else 0 }

                return DriveNode(
                    file = file,
                    path = path,
                    resolvedSize = resolvedSize,
                    children = childrenNodes,
                    fileCount = fileCount,
                    folderCount = folderCount
                )
            } else {
                return DriveNode(
                    file = file,
                    path = path,
                    resolvedSize = file.size,
                    children = emptyList(),
                    fileCount = 1,
                    folderCount = 0
                )
            }
        }

        // Create a virtual root node to represent the whole Drive
        val childrenNodes = rootFiles.map { buildNode(it, "") }
        val totalSize = childrenNodes.sumOf { it.resolvedSize }
        val fileCount = childrenNodes.sumOf { it.fileCount }
        val folderCount = childrenNodes.sumOf { it.folderCount + if (it.file.isFolder) 1 else 0 }

        val virtualRootFile = DriveFile(
            id = "root_virtual",
            name = "My Drive",
            mimeType = DriveFile.FOLDER_MIME_TYPE,
            size = 0L,
            parentId = null,
            modifiedTime = System.currentTimeMillis()
        )

        return DriveNode(
            file = virtualRootFile,
            path = "My Drive",
            resolvedSize = totalSize,
            children = childrenNodes,
            fileCount = fileCount,
            folderCount = folderCount
        )
    }

    private fun filterTree(node: DriveNode, query: String): DriveNode? {
        val matchesQuery = node.file.name.lowercase(Locale.ROOT).contains(query)
        
        // Filter children recursively
        val filteredChildren = node.children.mapNotNull { filterTree(it, query) }

        // Keep this node if it matches the query, OR if it has children that match
        if (matchesQuery || filteredChildren.isNotEmpty()) {
            return node.copy(children = filteredChildren)
        }
        return null
    }

    private fun sortTree(node: DriveNode, sortBy: SortOption): DriveNode {
        if (node.children.isEmpty()) return node

        // Recursively sort children
        val sortedChildren = node.children.map { sortTree(it, sortBy) }.toMutableList()

        // Sort current children level
        when (sortBy) {
            SortOption.SIZE_DESC -> sortedChildren.sortByDescending { it.resolvedSize }
            SortOption.SIZE_ASC -> sortedChildren.sortBy { it.resolvedSize }
            SortOption.NAME_ASC -> sortedChildren.sortBy { it.file.name.lowercase(Locale.ROOT) }
            SortOption.NAME_DESC -> sortedChildren.sortByDescending { it.file.name.lowercase(Locale.ROOT) }
            SortOption.DATE_DESC -> sortedChildren.sortByDescending { it.file.modifiedTime }
            SortOption.DATE_ASC -> sortedChildren.sortBy { it.file.modifiedTime }
        }

        return node.copy(children = sortedChildren)
    }
}

data class AnalysisResult(
    val rootNode: DriveNode?,
    val stats: DriveStats
)

enum class SortOption {
    SIZE_DESC,
    SIZE_ASC,
    NAME_ASC,
    NAME_DESC,
    DATE_DESC,
    DATE_ASC
}
