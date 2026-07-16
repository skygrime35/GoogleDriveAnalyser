package com.skygrime.googledriveanalyser.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.skygrime.googledriveanalyser.domain.model.FileCategory
import com.skygrime.googledriveanalyser.presentation.theme.CategoryColors
import com.skygrime.googledriveanalyser.presentation.utils.Formatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonutChart(
    categorySizes: Map<FileCategory, Long>,
    totalSize: Long,
    modifier: Modifier = Modifier
) {
    if (totalSize <= 0) return

    // Calculate proportions and angles
    val slices = categorySizes.filter { it.value > 0 }.map { (category, size) ->
        val percentage = (size.toDouble() / totalSize.toDouble()).toFloat()
        val sweepAngle = percentage * 360f
        val color = CategoryColors[category.name] ?: Color.Gray
        Slice(category.name, percentage, sweepAngle, color, size)
    }.sortedByDescending { it.size }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 24.dp.toPx()
                var startAngle = -90f
                val spacingAngle = if (slices.size > 1) 2f else 0f

                for (slice in slices) {
                    val sweep = (slice.sweepAngle - spacingAngle).coerceAtLeast(1f)
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += slice.sweepAngle
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatter.formatSize(totalSize),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid-like Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3
        ) {
            for (slice in slices) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(slice.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${slice.name}: ${Formatter.formatSize(slice.size)} (${(slice.percentage * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private data class Slice(
    val name: String,
    val percentage: Float,
    val sweepAngle: Float,
    val color: Color,
    val size: Long
)
