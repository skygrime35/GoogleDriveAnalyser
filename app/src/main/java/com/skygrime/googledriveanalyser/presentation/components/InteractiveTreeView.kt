package com.skygrime.googledriveanalyser.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skygrime.googledriveanalyser.domain.model.DriveNode
import com.skygrime.googledriveanalyser.domain.model.FileCategory
import com.skygrime.googledriveanalyser.presentation.theme.CategoryColors
import com.skygrime.googledriveanalyser.presentation.utils.Formatter

data class FlatNode(
    val node: DriveNode,
    val depth: Int
)

@Composable
fun InteractiveTreeView(
    rootNode: DriveNode,
    modifier: Modifier = Modifier,
    onOpenFileLink: (String) -> Unit = {}
) {
    var expandedNodeIds by remember { mutableStateOf(setOf("root_virtual")) }

    val visibleNodes = remember(rootNode, expandedNodeIds) {
        flattenTree(rootNode, expandedNodeIds)
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = visibleNodes,
            key = { it.node.file.id }
        ) { flatNode ->
            val node = flatNode.node
            val file = node.file
            val isExpanded = expandedNodeIds.contains(file.id)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (file.isFolder) {
                            expandedNodeIds = if (isExpanded) {
                                expandedNodeIds - file.id
                            } else {
                                expandedNodeIds + file.id
                            }
                        } else {
                            file.webViewLink?.let { onOpenFileLink(it) }
                        }
                    }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indent
                Spacer(modifier = Modifier.width((flatNode.depth * 16).dp))

                // Expand/Collapse Arrow
                if (file.isFolder) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = "Expand/Collapse",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                // File/Folder Type Icon
                val icon = getIconForNode(node)
                val iconColor = getColorForNode(node)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Name and details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (file.isFolder && node.file.id != "root_virtual") {
                        Text(
                            text = "${node.fileCount} files, ${node.folderCount} folders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Size
                Text(
                    text = Formatter.formatSize(node.resolvedSize),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = (flatNode.depth * 16 + 48).dp)
            )
        }
    }
}

private fun flattenTree(
    node: DriveNode,
    expandedNodeIds: Set<String>,
    depth: Int = 0
): List<FlatNode> {
    val list = mutableListOf<FlatNode>()
    list.add(FlatNode(node, depth))
    if (node.file.isFolder && expandedNodeIds.contains(node.file.id)) {
        for (child in node.children) {
            list.addAll(flattenTree(child, expandedNodeIds, depth + 1))
        }
    }
    return list
}

@Composable
private fun getIconForNode(node: DriveNode): ImageVector {
    return if (node.file.isFolder) {
        if (node.file.id == "root_virtual") Icons.Default.Cloud else Icons.Default.Folder
    } else {
        val category = FileCategory.fromMimeType(node.file.mimeType)
        when (category) {
            FileCategory.IMAGES -> Icons.Default.Image
            FileCategory.VIDEOS -> Icons.Default.PlayArrow
            FileCategory.AUDIO -> Icons.Default.MusicNote
            FileCategory.DOCUMENTS -> Icons.Default.Description
            FileCategory.ARCHIVES -> Icons.Default.Archive
            FileCategory.OTHER -> Icons.Default.InsertDriveFile
        }
    }
}

@Composable
private fun getColorForNode(node: DriveNode): Color {
    return if (node.file.isFolder) {
        if (node.file.id == "root_virtual") MaterialTheme.colorScheme.primary else Color(0xFFFFA000) // Folder Amber
    } else {
        val category = FileCategory.fromMimeType(node.file.mimeType)
        CategoryColors[category.name] ?: Color.Gray
    }
}
