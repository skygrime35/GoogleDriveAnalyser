package com.skygrime.googledriveanalyser.domain.usecase

import com.skygrime.googledriveanalyser.domain.model.DriveFile
import com.skygrime.googledriveanalyser.domain.model.FileCategory
import org.junit.Assert.*
import org.junit.Test

class AnalyzeStorageUseCaseTest {

    private val useCase = AnalyzeStorageUseCase()

    private val mockFiles = listOf(
        // Root items
        DriveFile("1", "Folder A", DriveFile.FOLDER_MIME_TYPE, 0L, null, 1000L),
        DriveFile("2", "Document A.txt", "text/plain", 1024L, null, 2000L),
        // Under Folder A (ID: 1)
        DriveFile("3", "Subfolder A1", DriveFile.FOLDER_MIME_TYPE, 0L, "1", 3000L),
        DriveFile("4", "Image A.png", "image/png", 2048L, "1", 4000L),
        // Under Subfolder A1 (ID: 3)
        DriveFile("5", "Video B.mp4", "video/mp4", 4096L, "3", 5000L)
    )

    @Test
    fun testExecute_emptyList_returnsNullRoot() {
        val result = useCase.execute(emptyList())
        assertNull(result.rootNode)
        assertEquals(0, result.stats.totalSize)
    }

    @Test
    fun testExecute_correctTreeBuiltAndSizesResolved() {
        val result = useCase.execute(mockFiles)
        val root = result.rootNode

        assertNotNull(root)
        assertEquals("root_virtual", root!!.file.id)

        // Total size = 1024 (Document A) + 2048 (Image A) + 4096 (Video B) = 7168
        assertEquals(7168L, root.resolvedSize)
        assertEquals(3, root.fileCount) // Document A, Image A, Video B
        assertEquals(2, root.folderCount) // Folder A, Subfolder A1

        // Find Folder A node (should be child of virtual root)
        val folderANode = root.children.find { it.file.id == "1" }
        assertNotNull(folderANode)
        // Folder A size = Image A (2048) + Subfolder A1 (which is Video B (4096)) = 6144
        assertEquals(6144L, folderANode!!.resolvedSize)

        // Find Subfolder A1 node
        val subfolderNode = folderANode.children.find { it.file.id == "3" }
        assertNotNull(subfolderNode)
        assertEquals(4096L, subfolderNode!!.resolvedSize)
    }

    @Test
    fun testExecute_statsCalculation() {
        val result = useCase.execute(mockFiles)
        val stats = result.stats

        assertEquals(7168L, stats.totalSize)
        assertEquals(3, stats.totalFilesCount)
        assertEquals(2, stats.totalFoldersCount)

        assertEquals(2048L, stats.sizeByCategory[FileCategory.IMAGES])
        assertEquals(4096L, stats.sizeByCategory[FileCategory.VIDEOS])
        assertEquals(1024L, stats.sizeByCategory[FileCategory.DOCUMENTS])
    }

    @Test
    fun testExecute_searchFiltering() {
        // Search for "Video" should keep Subfolder A1, Folder A and Virtual Root, but filter out Document A and Image A
        val result = useCase.execute(mockFiles, searchQuery = "Video")
        val root = result.rootNode

        assertNotNull(root)
        // Children of virtual root should only include Folder A (because it contains Video B)
        assertEquals(1, root!!.children.size)
        assertEquals("1", root.children[0].file.id)

        // Under Folder A, children should only include Subfolder A1
        val folderA = root.children[0]
        assertEquals(1, folderA.children.size)
        assertEquals("3", folderA.children[0].file.id)

        // Under Subfolder A1, children should only include Video B
        val subfolderA1 = folderA.children[0]
        assertEquals(1, subfolderA1.children.size)
        assertEquals("5", subfolderA1.children[0].file.id)
    }

    @Test
    fun testExecute_sorting() {
        // Sort by size ascending: Document A (1024) should come before Folder A (6144) under root
        val result = useCase.execute(mockFiles, sortBy = SortOption.SIZE_ASC)
        val root = result.rootNode

        assertNotNull(root)
        assertEquals(2, root!!.children.size)
        assertEquals("2", root.children[0].file.id) // Document A (1024)
        assertEquals("1", root.children[1].file.id) // Folder A (6144)
    }
}
