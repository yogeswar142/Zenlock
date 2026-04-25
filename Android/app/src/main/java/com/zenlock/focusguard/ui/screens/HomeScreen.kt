package com.zenlock.focusguard.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.MainViewModel
import com.zenlock.focusguard.util.PermissionUtils
import com.zenlock.focusguard.util.TimeUtils

/**
 * Home dashboard – Zenlock "Digital Sanctuary" design.
 *
 * Hero-centric layout: massive countdown timer, pill-shaped CTA,
 * two bento-style stat cards, and a motivational quote.
 */
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    val isFocusActive by viewModel.isFocusActive.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val totalSeconds by viewModel.totalSeconds.collectAsState()
    val focusDuration by viewModel.focusDurationMinutes.collectAsState()
    val isStrict by viewModel.strictMode.collectAsState()

    val hasUsageStats by viewModel.hasUsageStatsPermission.collectAsState()
    val hasOverlay by viewModel.hasOverlayPermission.collectAsState()
    val hasAccessibility by viewModel.hasAccessibilityPermission.collectAsState()

    val todayFocusTime by viewModel.todayFocusTime.collectAsState()
    val todaySessionCount by viewModel.todaySessionCount.collectAsState()
    val todayBlockedAttempts by viewModel.todayBlockedAttempts.collectAsState()

    val userXP by viewModel.userXP.collectAsState()
    val userStreak by viewModel.userStreak.collectAsState()

    // Motivational quotes
    val quotes = listOf(
        "Stay focused on what matters.",
        "The secret of getting ahead is getting started.",
        "Where focus goes, energy flows.",
        "Do what you have to do until you can do what you want to do.",
        "Starve your distractions and feed your focus.",
    )
    val quote = remember { quotes.random() }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
        viewModel.loadStatistics()
    }

    val allPermissionsGranted = hasUsageStats && hasOverlay && hasAccessibility

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Permission warnings ──
        if (!allPermissionsGranted) {
            Spacer(modifier = Modifier.height(8.dp))
            PermissionCard(
                hasUsageStats = hasUsageStats,
                hasOverlay = hasOverlay,
                hasAccessibility = hasAccessibility,
                onGrantUsageStats = { PermissionUtils.openUsageStatsSettings(context) },
                onGrantOverlay = { PermissionUtils.openOverlaySettings(context) },
                onGrantAccessibility = { PermissionUtils.openAccessibilitySettings(context) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.weight(0.15f))

        // ── Hero Focus Section ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Session status label
            Text(
                text = when {
                    isFocusActive && isPaused -> "SESSION PAUSED"
                    isFocusActive && isStrict -> "STRICT MODE"
                    isFocusActive -> "SESSION ACTIVE"
                    else -> "READY TO FOCUS"
                },
                style = MaterialTheme.typography.labelSmall,
                color = ZenOutline,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Accent bar
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ZenPrimaryContainer)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Large Timer ──
            val displayTime = if (isFocusActive) {
                TimeUtils.formatTimer(remainingSeconds)
            } else {
                TimeUtils.formatTimer((focusDuration * 60).toLong())
            }

            Text(
                text = displayTime,
                fontSize = 96.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Manrope,
                color = ZenOnSurface,
                letterSpacing = (-4).sp,
                textAlign = TextAlign.Center,
                lineHeight = 96.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Duration presets (only when idle) ──
            if (!isFocusActive) {
                DurationPicker(
                    currentDuration = focusDuration,
                    onDurationChange = { viewModel.setFocusDuration(it) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Primary CTA ──
            FocusControlButtons(
                isFocusActive = isFocusActive,
                isPaused = isPaused,
                isStrict = isStrict,
                allPermissionsGranted = allPermissionsGranted,
                onStart = { viewModel.startFocusSession() },
                onPause = { viewModel.pauseFocusSession() },
                onResume = { viewModel.resumeFocusSession() },
                onStop = { viewModel.stopFocusSession() }
            )

            if (!isFocusActive) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Current session: Pomodoro",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZenOutline
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.12f))

        // ── Bento Stat Cards ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoStatCard(
                icon = Icons.Outlined.Timer,
                value = String.format("%.1fh", todayFocusTime / 3600f),
                label = "TIME FOCUSED",
                modifier = Modifier.weight(1f)
            )
            BentoStatCard(
                icon = Icons.Outlined.Block,
                value = todayBlockedAttempts.toString(),
                label = "APPS BLOCKED",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Motivational Quote ──
        Text(
            text = "\"$quote\"",
            style = MaterialTheme.typography.bodyMedium,
            color = ZenOutline,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )

        Spacer(modifier = Modifier.weight(0.1f))
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Sub-components
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
private fun BentoStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZenSurfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ZenPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = ZenOnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ZenOutline,
                letterSpacing = 0.5.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DurationPicker(
    currentDuration: Int,
    onDurationChange: (Int) -> Unit
) {
    val presets = listOf(15, 25, 30, 45, 60, 90)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .horizontalScroll(rememberScrollState()),
    ) {
        presets.forEach { minutes ->
            val isSelected = minutes == currentDuration
            FilterChip(
                selected = isSelected,
                onClick = { onDurationChange(minutes) },
                label = {
                    Text(
                        text = TimeUtils.formatMinutes(minutes),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ZenPrimaryContainer,
                    selectedLabelColor = Color.White,
                    containerColor = ZenSurfaceContainerLow,
                    labelColor = ZenOutline
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.White.copy(alpha = 0.05f),
                    selectedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(50)
            )
        }
    }
}

@Composable
private fun FocusControlButtons(
    isFocusActive: Boolean,
    isPaused: Boolean,
    isStrict: Boolean,
    allPermissionsGranted: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    var showStopDialog by remember { mutableStateOf(false) }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Stop Focus Session?") },
            text = { Text("Are you sure you want to give up on your focus session early? This will be recorded in your statistics.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStopDialog = false
                        onStop()
                    }
                ) {
                    Text("Give Up", color = Coral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("Keep Focusing")
                }
            },
            containerColor = ZenCard,
            titleContentColor = ZenOnSurface,
            textContentColor = ZenOnSurfaceVariant
        )
    }

    if (!isFocusActive) {
        // Pill-shaped START FOCUS button
        Button(
            onClick = onStart,
            enabled = allPermissionsGranted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 40.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = ZenPrimaryContainer,
                disabledContainerColor = ZenSurfaceContainerLow
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            )
        ) {
            Icon(Icons.Default.PlayArrow, "Start", modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (allPermissionsGranted) "START FOCUS" else "GRANT PERMISSIONS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pause / Resume – secondary pill
            OutlinedButton(
                onClick = if (isPaused) onResume else onPause,
                enabled = !isStrict,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.2f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isPaused) Cyan else Amber,
                    disabledContentColor = ZenOutline
                )
            ) {
                Icon(
                    if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    "Toggle",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPaused) "Resume" else "Pause",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Stop – coral pill
            Button(
                onClick = { showStopDialog = true },
                enabled = !isStrict,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Coral,
                    disabledContainerColor = ZenSurfaceContainerLow
                )
            ) {
                Icon(Icons.Default.Stop, "Stop", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Stop",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    hasUsageStats: Boolean,
    hasOverlay: Boolean,
    hasAccessibility: Boolean,
    onGrantUsageStats: () -> Unit,
    onGrantOverlay: () -> Unit,
    onGrantAccessibility: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ZenCard
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Coral.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    "Warning",
                    tint = Amber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ZenOnSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!hasUsageStats) {
                PermissionRow(
                    name = "Usage Access",
                    description = "Detect foreground apps",
                    onClick = onGrantUsageStats
                )
            }
            if (!hasOverlay) {
                PermissionRow(
                    name = "Draw Over Apps",
                    description = "Show blocking overlay",
                    onClick = onGrantOverlay
                )
            }
            if (!hasAccessibility) {
                PermissionRow(
                    name = "Accessibility",
                    description = "Read YouTube content",
                    onClick = onGrantAccessibility
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    name: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 14.sp, color = ZenOnSurface, fontWeight = FontWeight.Medium)
            Text(text = description, fontSize = 12.sp, color = ZenOnSurfaceVariant)
        }
        TextButton(onClick = onClick) {
            Text("Grant", color = ZenPrimaryContainer, fontWeight = FontWeight.Bold)
        }
    }
}
