package com.example.deliverlk.feature.manager

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deliverlk.R
import com.example.deliverlk.core.designsystem.AvatarInitials
import com.example.deliverlk.core.designsystem.DeliverLkTopBar

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DeliverLkTopBar(title = stringResource(R.string.reports))
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedPeriod == ReportPeriod.TODAY,
                        onClick = { viewModel.onPeriodSelected(ReportPeriod.TODAY) },
                        label = { Text("Today") }
                    )
                    FilterChip(
                        selected = uiState.selectedPeriod == ReportPeriod.SEVEN_DAYS,
                        onClick = { viewModel.onPeriodSelected(ReportPeriod.SEVEN_DAYS) },
                        label = { Text("Last 7 Days") }
                    )
                    FilterChip(
                        selected = uiState.selectedPeriod == ReportPeriod.THIRTY_DAYS,
                        onClick = { viewModel.onPeriodSelected(ReportPeriod.THIRTY_DAYS) },
                        label = { Text("Last 30 Days") }
                    )
                }
            }

            // Summary Stats Row
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Orders", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${uiState.totalDeliveries}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Completion %", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${uiState.completionRatePercentage}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Avg Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${uiState.avgDeliveryTimeMins}m", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Bar Chart (Deliveries per driver)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Deliveries per Driver", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        val primaryColor = MaterialTheme.colorScheme.primary
                        val ranks = uiState.driverRanks.take(5)
                        val maxCount = (ranks.maxOfOrNull { it.completedDeliveries } ?: 10).toFloat()

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val barWidth = size.width / (ranks.size * 2)
                            ranks.forEachIndexed { index, driver ->
                                val barHeight = (driver.completedDeliveries / maxCount) * (size.height - 30.dp.toPx())
                                val x = index * barWidth * 2 + barWidth / 2
                                val y = size.height - barHeight - 20.dp.toPx()

                                drawRoundRect(
                                    color = primaryColor,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                            }
                        }
                    }
                }
            }

            // Line Chart (Daily deliveries trend)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Daily Deliveries Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        val secondaryColor = MaterialTheme.colorScheme.secondary
                        val trend = uiState.dailyTrendData
                        val maxVal = (trend.maxOfOrNull { it.second } ?: 30).toFloat()

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            if (trend.isNotEmpty()) {
                                val stepX = size.width / (trend.size - 1)
                                val path = Path()

                                trend.forEachIndexed { i, pair ->
                                    val x = i * stepX
                                    val y = size.height - (pair.second / maxVal) * (size.height - 20.dp.toPx()) - 10.dp.toPx()
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    drawCircle(color = secondaryColor, radius = 6f, center = Offset(x, y))
                                }

                                drawPath(
                                    path = path,
                                    color = secondaryColor,
                                    style = Stroke(width = 4f)
                                )
                            }
                        }
                    }
                }
            }

            // Ranked Driver Leaderboard
            item {
                Text("Driver Leaderboard", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            itemsIndexed(uiState.driverRanks, key = { _, item -> item.name }) { index, driver ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#${index + 1}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        AvatarInitials(name = driver.name, size = 36.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(driver.name, fontWeight = FontWeight.Bold)
                            Text("${driver.completedDeliveries} deliveries completed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Score: ${driver.score}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
