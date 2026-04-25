package com.zenlock.focusguard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.data.db.entity.FocusSessionEntity
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.MainViewModel
import com.zenlock.focusguard.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Statistics screen with charts showing focus time data over days, weeks, and months.
 */
@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val todayFocusTime by viewModel.todayFocusTime.collectAsState()
    val weeklyFocusTime by viewModel.weeklyFocusTime.collectAsState()
    val monthlyFocusTime by viewModel.monthlyFocusTime.collectAsState()
    val todaySessionCount by viewModel.todaySessionCount.collectAsState()
    val weeklySessionCount by viewModel.weeklySessionCount.collectAsState()
    val weeklyChartData by viewModel.weeklyChartData.collectAsState()
    val recentSessions by viewModel.allSessions.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Text(
                text = "📊 Statistics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Time period tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkCard,
                contentColor = Color.White,
                indicator = {},
                divider = {}
            ) {
                listOf("Today", "Week", "Month").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                if (selectedTab == index) Purple60.copy(alpha = 0.3f)
                                else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(vertical = 10.dp),
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Purple60 else TextSecondary
                        )
                    }
                }
            }
        }

        // Summary cards
        item {
            val focusTime = when (selectedTab) {
                0 -> todayFocusTime
                1 -> weeklyFocusTime
                else -> monthlyFocusTime
            }
            val sessionCount = when (selectedTab) {
                0 -> todaySessionCount
                else -> weeklySessionCount
            }
            val avgSessionMinutes = if (sessionCount > 0) {
                (focusTime / 60) / sessionCount
            } else 0L

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsOverviewCard(
                    title = "Focus Time",
                    value = TimeUtils.formatDuration(focusTime),
                    icon = "⏱️",
                    color = Cyan,
                    modifier = Modifier.weight(1f)
                )
                StatsOverviewCard(
                    title = "Sessions",
                    value = sessionCount.toString(),
                    icon = "🎯",
                    color = Purple60,
                    modifier = Modifier.weight(1f)
                )
                StatsOverviewCard(
                    title = "Avg Session",
                    value = "${avgSessionMinutes}m",
                    icon = "📈",
                    color = Emerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Weekly bar chart
        item {
            if (weeklyChartData.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Weekly Focus Time",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Minutes per day",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        WeeklyBarChart(
                            data = weeklyChartData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }
        }

        // Recent sessions
        item {
            Text(
                text = "Recent Sessions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (recentSessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No sessions yet",
                            color = TextSecondary
                        )
                        Text(
                            text = "Start your first focus session!",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(recentSessions.take(20)) { session ->
                SessionHistoryItem(session = session)
            }
        }
    }
}

@Composable
private fun StatsOverviewCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Custom bar chart for weekly focus time visualization.
 */
@Composable
private fun WeeklyBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1f
    val normalizedMax = if (maxValue > 0f) maxValue else 1f

    Column(modifier = modifier) {
        // Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { (day, value) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    // Value label
                    if (value > 0) {
                        Text(
                            text = "${value.toInt()}",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Bar
                    val barHeight = if (normalizedMax > 0f) {
                        (value / normalizedMax).coerceIn(0.02f, 1f)
                    } else 0.02f

                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight(barHeight)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (value > 0) listOf(Cyan, Purple60)
                                    else listOf(DarkSurfaceVariant, DarkSurfaceVariant)
                                ),
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { (day, _) ->
                Text(
                    text = day,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SessionHistoryItem(session: FocusSessionEntity) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(session.startTime))
    val durationStr = TimeUtils.formatDuration(session.actualDurationSeconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (session.isCompleted) Emerald.copy(alpha = 0.15f)
                        else Coral.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (session.isCompleted) Icons.Default.CheckCircle
                    else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (session.isCompleted) Emerald else Coral,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (session.isCompleted) "Completed" else "Stopped Early",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = dateStr,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = durationStr,
                    fontWeight = FontWeight.Bold,
                    color = if (session.isCompleted) Cyan else TextSecondary,
                    fontSize = 14.sp
                )
                if (session.blockedAttempts > 0) {
                    Text(
                        text = "${session.blockedAttempts} blocked",
                        color = Coral,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
