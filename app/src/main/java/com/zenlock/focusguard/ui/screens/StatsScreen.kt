package com.zenlock.focusguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.data.db.entity.FocusSessionEntity
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.MainViewModel
import com.zenlock.focusguard.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Focus Analytics screen – Zenlock "Digital Sanctuary" design.
 *
 * Large "Focus Analytics" headline, weekly bar chart glass card,
 * secondary insight cards with progress bars, and session breakdown bento.
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

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    val totalHours = weeklyFocusTime / 3600f
    val goalHours = 40f
    val completionPercent = ((totalHours / goalHours) * 100).coerceAtMost(100f)
    val avgSessionMinutes = if (weeklySessionCount > 0) {
        (weeklyFocusTime / 60) / weeklySessionCount
    } else 0L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenBackground)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // ── Hero Header ──
        item {
            Column {
                Text(
                    text = "Focus\nAnalytics",
                    style = MaterialTheme.typography.displayMedium,
                    color = ZenOnSurface,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A deep dive into your concentration cycles and cognitive performance over the last period.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ZenOnSurfaceVariant,
                    lineHeight = 26.sp
                )
            }
        }

        // ── Main Chart Card ──
        item {
            GlassCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Daily Focus Time",
                                style = MaterialTheme.typography.headlineMedium,
                                color = ZenOnSurface,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "LAST 7 DAYS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZenOutline,
                                letterSpacing = 3.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format("%.1f hrs", totalHours),
                                style = MaterialTheme.typography.headlineLarge,
                                color = ZenPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total Focus",
                                style = MaterialTheme.typography.labelLarge,
                                color = ZenTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bar chart
                    if (weeklyChartData.isNotEmpty()) {
                        WeeklyBarChart(data = weeklyChartData)
                    } else {
                        // Placeholder bars
                        val placeholderData = listOf(
                            "Mon" to 60f, "Tue" to 85f, "Wed" to 45f,
                            "Thu" to 100f, "Fri" to 70f, "Sat" to 30f, "Sun" to 55f
                        )
                        WeeklyBarChart(data = placeholderData)
                    }
                }
            }
        }

        // ── Secondary Insight Cards ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Weekly Trend
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.TrendingUp,
                                contentDescription = null,
                                tint = ZenPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Weekly Trend",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ZenOnSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("Your productivity increased by ")
                                withStyle(SpanStyle(color = ZenPrimaryContainer, fontWeight = FontWeight.Bold)) {
                                    append("+12%")
                                }
                                append(" compared to last week.")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = ZenOnSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { completionPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(50)),
                            color = ZenPrimaryContainer,
                            trackColor = ZenSurfaceContainer,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Goal: ${goalHours.toInt()}h",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZenOutline,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${completionPercent.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZenOnSurface,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Average Session
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = ZenPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Average Session",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ZenOnSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("You typically maintain peak focus for stretches of ")
                                withStyle(SpanStyle(color = ZenPrimaryContainer, fontWeight = FontWeight.Bold)) {
                                    append("${avgSessionMinutes} minutes")
                                }
                                append(".")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = ZenOnSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            ChipTag("Deep Work")
                            ChipTag("Consistency")
                        }
                    }
                }
            }
        }

        // ── Session Breakdown ──
        item {
            Text(
                text = "Session Breakdown",
                style = MaterialTheme.typography.headlineMedium,
                color = ZenOnSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Peak performance card (2/3 width)
                GlassCard(modifier = Modifier.weight(2f).height(160.dp)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Gradient decoration
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.5f)
                                .align(Alignment.CenterEnd)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            ZenPrimaryContainer.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "PEAK PERFORMANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZenPrimaryContainer,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Most productive at 10:00 AM",
                                style = MaterialTheme.typography.headlineMedium,
                                color = ZenOnSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Schedule your hardest tasks during this window.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ZenOnSurfaceVariant
                            )
                        }
                    }
                }

                // Flow states card (1/3 width)
                GlassCard(modifier = Modifier.weight(1f).height(160.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = ZenPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${todaySessionCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ZenOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Flow States Achieved",
                            style = MaterialTheme.typography.labelLarge,
                            color = ZenOutline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── Recent Sessions ──
        if (recentSessions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ZenOnSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(recentSessions.take(10)) { session ->
                SessionHistoryItem(session = session)
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Sub-components
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/** Glass card with #1F2937 bg, soft shadow, subtle white border */
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZenCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        content()
    }
}

@Composable
private fun ChipTag(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = ZenSurfaceContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = ZenTertiary,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WeeklyBarChart(
    data: List<Pair<String, Float>>,
) {
    val maxValue = data.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
    // Find the day with max value to highlight
    val maxDay = data.maxByOrNull { it.second }?.first

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (day, value) ->
            val barFraction = (value / maxValue).coerceIn(0.04f, 1f)
            val isHighlighted = day == maxDay && value > 0

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(barFraction)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (isHighlighted) ZenPrimaryContainer
                            else ZenPrimaryContainer.copy(alpha = 0.2f)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isHighlighted) ZenPrimaryContainer else ZenOutline,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
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

    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (session.isCompleted) Emerald.copy(alpha = 0.12f)
                        else Coral.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
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
                    color = ZenOnSurface,
                    fontSize = 14.sp
                )
                Text(
                    text = dateStr,
                    color = ZenOutline,
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = durationStr,
                    fontWeight = FontWeight.Bold,
                    color = if (session.isCompleted) ZenPrimaryContainer else ZenOnSurfaceVariant,
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
