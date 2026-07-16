package com.skygrime.googledriveanalyser.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skygrime.googledriveanalyser.domain.model.DriveFile
import com.skygrime.googledriveanalyser.domain.model.DriveNode
import com.skygrime.googledriveanalyser.domain.usecase.AnalysisResult
import com.skygrime.googledriveanalyser.domain.usecase.SortOption
import com.skygrime.googledriveanalyser.presentation.ScanState
import com.skygrime.googledriveanalyser.presentation.components.DonutChart
import com.skygrime.googledriveanalyser.presentation.components.InteractiveTreeView
import com.skygrime.googledriveanalyser.presentation.utils.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    email: String,
    scanState: ScanState,
    searchQuery: String,
    sortOption: SortOption,
    analysisResult: AnalysisResult?,
    onSyncClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSortOptionChanged: (SortOption) -> Unit,
    onOpenFileLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: File Tree
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Drive Analyser",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSyncClicked) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sync Now")
                    }
                    IconButton(onClick = onSignOutClicked) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                scanState is ScanState.Scanning -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scanning Google Drive...",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Fetching metadata. This might take a minute.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                scanState is ScanState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error Icon",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Failed to sync drive",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = scanState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onSyncClicked) {
                            Text("Retry Scan")
                        }
                    }
                }

                analysisResult == null -> {
                    // Empty state (no cache yet)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Scan Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Analyze your Storage",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Click the button below to fetch and analyze your Google Drive storage metadata recursively.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onSyncClicked,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Start Scanning Drive")
                        }
                    }
                }

                else -> {
                    // Show Dashboard Content
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search & Filter header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChanged,
                                placeholder = { Text("Search files or folders...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box {
                                IconButton(
                                    onClick = { sortMenuExpanded = true },
                                    modifier = Modifier.background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sort Options"
                                    )
                                }
                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false }
                                ) {
                                    SortOption.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(getSortOptionLabel(option)) },
                                            onClick = {
                                                onSortOptionChanged(option)
                                                sortMenuExpanded = false
                                            },
                                            leadingIcon = {
                                                if (option == sortOption) {
                                                    Icon(Icons.Default.Check, contentDescription = null)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Tab Selection
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Overview") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("File Tree") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tab content
                        Box(modifier = Modifier.weight(1f)) {
                            if (selectedTab == 0) {
                                DashboardOverviewTab(
                                    analysisResult = analysisResult,
                                    onOpenFileLink = onOpenFileLink
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                                    if (analysisResult.rootNode != null) {
                                        InteractiveTreeView(
                                            rootNode = analysisResult.rootNode,
                                            onOpenFileLink = onOpenFileLink
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No matches found for your search query.")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardOverviewTab(
    analysisResult: AnalysisResult,
    onOpenFileLink: (String) -> Unit
) {
    val stats = analysisResult.stats
    val root = analysisResult.rootNode

    // Collect the largest files from the tree
    val largestFiles = remember(root) {
        if (root == null) emptyList() else getLargestFiles(root, 10)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Storage Donut Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Storage Space Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DonutChart(
                        categorySizes = stats.sizeByCategory,
                        totalSize = stats.totalSize
                    )
                }
            }
        }

        // Summary Stats Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stats.totalFilesCount.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Folders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stats.totalFoldersCount.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Top 10 Largest files list
        if (largestFiles.isNotEmpty()) {
            item {
                Text(
                    text = "Largest Files & Folders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(largestFiles) { itemNode ->
                val file = itemNode.file
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { file.webViewLink?.let { onOpenFileLink(it) } },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (file.isFolder) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = if (file.isFolder) Color(0xFFFFA000) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = itemNode.path,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Formatter.formatSize(itemNode.resolvedSize),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun getLargestFiles(root: DriveNode, limit: Int): List<DriveNode> {
    val allItems = mutableListOf<DriveNode>()

    // Traverse tree to get all non-root nodes
    fun traverse(node: DriveNode) {
        if (node.file.id != "root_virtual") {
            allItems.add(node)
        }
        for (child in node.children) {
            traverse(child)
        }
    }

    traverse(root)
    return allItems.sortedByDescending { it.resolvedSize }.take(limit)
}

private fun getSortOptionLabel(option: SortOption): String {
    return when (option) {
        SortOption.SIZE_DESC -> "Size (Largest first)"
        SortOption.SIZE_ASC -> "Size (Smallest first)"
        SortOption.NAME_ASC -> "Name (A to Z)"
        SortOption.NAME_DESC -> "Name (Z to A)"
        SortOption.DATE_DESC -> "Date (Newest first)"
        SortOption.DATE_ASC -> "Date (Oldest first)"
    }
}
