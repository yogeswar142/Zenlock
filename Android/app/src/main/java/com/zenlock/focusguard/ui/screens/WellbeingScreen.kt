package com.zenlock.focusguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.domain.model.AppUsageInfo
import com.zenlock.focusguard.domain.model.DailyDigitalReport
import com.zenlock.focusguard.domain.model.DailyUsageSummary
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.MainViewModel
import com.zenlock.focusguard.util.PermissionUtils
import com.zenlock.focusguard.util.TimeUtils

/**
 * Digital Wellbeing / Daily Digital Report Screen.
 * Visual design matches Zenlock's Digital Sanctuary / glassmorphism / Bento-style UI.
 */
@Composable
fun WellbeingScreen(viewModel: MainViewModel) {
    val dailyReport by viewModel.dailyReport.collectAsState()
    val weeklyReport by viewModel.weeklyReport.collectAsState()
    val hasUsageStats by viewModel.hasUsageStatsPermission.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadDigitalWellbeingData()
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
                viewModel.loadDigitalWellbeingData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                    text = "Digital\nWellbeing",
                    style = MaterialTheme.typography.displayMedium,
                    color = ZenOnSurface,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Daily Digital Report & Screen Time Breakdown.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ZenOnSurfaceVariant,
                    lineHeight = 24.sp
                )
            }
        }

        if (!hasUsageStats) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ZenCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Coral.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Amber)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Usage Access Required", fontWeight = FontWeight.Bold, color = ZenOnSurface)
                            Text("Enable usage stats access to see exact daily report.", fontSize = 12.sp, color = ZenOutline)
                        }
                        TextButton(onClick = { PermissionUtils.openUsageStatsSettings(context) }) {
                            Text("Grant", color = ZenPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        val report = dailyReport
        if (report != null) {
            // ── Hero Overview Bento Cards ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoWellbeingCard(
                        title = "TOTAL SCREEN TIME",
                        value = TimeUtils.formatDuration(report.totalScreenTimeMs / 1000),
                        subtitle = "Tracked today",
                        icon = Icons.Outlined.Smartphone,
                        modifier = Modifier.weight(1f)
                    )
                    BentoWellbeingCard(
                        title = "DISTRACTION TIME",
                        value = TimeUtils.formatDuration(report.distractionTimeMs / 1000),
                        subtitle = "Blocked app usage",
                        icon = Icons.Outlined.Block,
                        valueColor = Coral,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Productivity Insight Score Card ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ZenCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Productivity Score",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ZenOnSurface
                                )
                                Text(
                                    text = "Based on intent vs distraction ratio",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZenOutline
                                )
                            }
                            Text(
                                text = "${report.productivityPercentage}%",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = ZenPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { report.productivityPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = ZenPrimaryContainer,
                            trackColor = ZenSurfaceContainer
                        )
                    }
                }
            }

            // ── Factual Insights Banner ──
            if (report.mostUsedAppSummary != null || report.launchCountSummary != null || report.productivitySummary != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ZenSurfaceContainerLow),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenPrimaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Insights, contentDescription = null, tint = ZenPrimaryContainer, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Key Daily Insights", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = ZenOnSurface)
                            }
                            report.mostUsedAppSummary?.let {
                                Text("• $it", style = MaterialTheme.typography.bodyMedium, color = ZenOnSurfaceVariant)
                            }
                            report.launchCountSummary?.let {
                                Text("• $it", style = MaterialTheme.typography.bodyMedium, color = ZenOnSurfaceVariant)
                            }
                            report.productivitySummary?.let {
                                Text("• $it", style = MaterialTheme.typography.bodyMedium, color = ZenOnSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // ── Weekly Screen Time Summary ──
            if (weeklyReport.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ZenCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Weekly Screen Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ZenOnSurface)
                            Text("DAILY USAGE PAST 7 DAYS", style = MaterialTheme.typography.labelSmall, color = ZenOutline, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            WeeklyWellbeingBarChart(weeklyReport)
                        }
                    }
                }
            }

            // ── Top Applications List ──
            item {
                Text(
                    text = "TOP APPS TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenOutline,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(report.topAppUsage.take(15), key = { it.packageName }) { appUsage ->
                AppUsageListItem(appUsage = appUsage, totalScreenTimeMs = report.totalScreenTimeMs)
            }
        }
    }
}

@Composable
private fun BentoWellbeingCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = ZenOnSurface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZenCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = ZenPrimaryContainer, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = valueColor)
            Text(title, style = MaterialTheme.typography.labelSmall, color = ZenOutline, letterSpacing = 1.sp)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ZenOnSurfaceVariant)
        }
    }
}

@Composable
private fun AppUsageListItem(appUsage: AppUsageInfo, totalScreenTimeMs: Long) {
    val usagePercent = if (totalScreenTimeMs > 0) {
        ((appUsage.totalTimeInForegroundMs.toFloat() / totalScreenTimeMs.toFloat()) * 100).toInt()
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZenCard.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161B26)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appUsage.appName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = ZenOnSurfaceVariant,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appUsage.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ZenOnSurface,
                    maxLines = 1
                )
                Text(
                    text = "${appUsage.launchCount} launches • $usagePercent% of day",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenOutline
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = TimeUtils.formatDuration(appUsage.totalTimeInForegroundMs / 1000),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (appUsage.isDistraction) Coral else ZenPrimaryContainer
                )
                if (appUsage.isDistraction) {
                    Text("Distracting", fontSize = 11.sp, color = Coral)
                }
            }
        }
    }
}

@Composable
private fun WeeklyWellbeingBarChart(data: List<DailyUsageSummary>) {
    val maxTime = data.maxOfOrNull { it.totalScreenTimeMs }?.coerceAtLeast(1L) ?: 1L

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { day ->
            val fraction = (day.totalScreenTimeMs.toFloat() / maxTime.toFloat()).coerceIn(0.05f, 1f)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(ZenPrimaryContainer)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(day.dayName, style = MaterialTheme.typography.labelSmall, color = ZenOutline)
            }
        }
    }
}
