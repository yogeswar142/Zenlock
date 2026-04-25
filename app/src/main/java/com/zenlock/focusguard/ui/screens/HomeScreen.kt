package com.zenlock.focusguard.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.MainViewModel
import com.zenlock.focusguard.util.PermissionUtils
import com.zenlock.focusguard.util.TimeUtils

/**
 * Home screen with the focus timer, quick stats, and permission status.
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

    // Refresh permissions and stats when screen is shown
    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
        viewModel.loadStatistics()
    }

    val allPermissionsGranted = hasUsageStats && hasOverlay && hasAccessibility

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🛡️ FocusGuard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isFocusActive) "Focus mode is active" else "Ready to focus",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            // Gamification Badge
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Star, "XP", tint = Amber, modifier = Modifier.size(16.dp))
                    Text(
                        text = "$userXP XP",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    if (userStreak > 0) {
                        Text(
                            text = "🔥 $userStreak",
                            fontWeight = FontWeight.Bold,
                            color = Coral,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        if (isFocusActive && isStrict) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Strict Mode", tint = Coral, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "STRICT MODE ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Coral,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Permission warnings
        if (!allPermissionsGranted) {
            PermissionCard(
                hasUsageStats = hasUsageStats,
                hasOverlay = hasOverlay,
                hasAccessibility = hasAccessibility,
                onGrantUsageStats = { PermissionUtils.openUsageStatsSettings(context) },
                onGrantOverlay = { PermissionUtils.openOverlaySettings(context) },
                onGrantAccessibility = { PermissionUtils.openAccessibilitySettings(context) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Timer circle
        FocusTimerCircle(
            remainingSeconds = remainingSeconds,
            totalSeconds = if (isFocusActive) totalSeconds else (focusDuration * 60).toLong(),
            isActive = isFocusActive,
            isPaused = isPaused
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Duration picker (only when not active)
        if (!isFocusActive) {
            DurationPicker(
                currentDuration = focusDuration,
                onDurationChange = { viewModel.setFocusDuration(it) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Control buttons
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

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Stats
        QuickStatsRow(
            focusTime = todayFocusTime,
            sessionCount = todaySessionCount,
            blockedAttempts = todayBlockedAttempts
        )
    }
}

@Composable
private fun FocusTimerCircle(
    remainingSeconds: Long,
    totalSeconds: Long,
    isActive: Boolean,
    isPaused: Boolean
) {
    val progress = if (totalSeconds > 0) {
        (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
    } else 0f

    // Animate the progress arc
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "progress"
    )

    // Pulsing animation when active
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(260.dp)
    ) {
        // Background circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Track
            drawCircle(
                color = DarkSurfaceVariant,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            if (isActive) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Purple60, Cyan, Purple60)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth)
                )
            }

            // Glow effect when active
            if (isActive && !isPaused) {
                drawCircle(
                    color = Purple60.copy(alpha = pulseAlpha * 0.2f),
                    radius = radius + 20.dp.toPx()
                )
            }
        }

        // Center content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displayTime = if (isActive) {
                TimeUtils.formatTimer(remainingSeconds)
            } else {
                TimeUtils.formatTimer(totalSeconds)
            }

            Text(
                text = displayTime,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color.White else TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when {
                    isActive && isPaused -> "PAUSED"
                    isActive -> "FOCUSING"
                    else -> "READY"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    isActive && isPaused -> Amber
                    isActive -> Cyan
                    else -> TextMuted
                },
                letterSpacing = 2.sp
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

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Session Duration",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
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
                        selectedContainerColor = Purple60,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
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
            containerColor = DarkCard,
            titleContentColor = Color.White,
            textContentColor = TextSecondary
        )
    }

    if (!isFocusActive) {
        // Start button
        Button(
            onClick = onStart,
            enabled = allPermissionsGranted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple60,
                disabledContainerColor = DarkSurfaceVariant
            )
        ) {
            Icon(Icons.Default.PlayArrow, "Start", modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (allPermissionsGranted) "Start Focus Session" else "Grant Permissions First",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        // Pause/Resume + Stop buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pause/Resume
            OutlinedButton(
                onClick = if (isPaused) onResume else onPause,
                enabled = !isStrict,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isPaused) Cyan else Amber,
                    disabledContentColor = TextMuted
                )
            ) {
                Icon(
                    if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    "Toggle Pause",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isPaused) "Resume" else "Pause", fontWeight = FontWeight.SemiBold)
            }

            // Stop
            Button(
                onClick = { showStopDialog = true },
                enabled = !isStrict,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Coral,
                    disabledContainerColor = DarkSurfaceVariant
                )
            ) {
                Icon(Icons.Default.Stop, "Stop", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stop", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun QuickStatsRow(
    focusTime: Long,
    sessionCount: Int,
    blockedAttempts: Int
) {
    Text(
        text = "Today's Progress",
        style = MaterialTheme.typography.titleSmall,
        color = TextSecondary,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = "⏱️",
            value = TimeUtils.formatDuration(focusTime),
            label = "Focus Time",
            color = Cyan,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = "🎯",
            value = sessionCount.toString(),
            label = "Sessions",
            color = Purple60,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = "🛡️",
            value = blockedAttempts.toString(),
            label = "Blocked",
            color = Coral,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextMuted
            )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FF6B6B)
        )
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
                    color = Color.White
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
            Text(text = name, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Text(text = description, fontSize = 12.sp, color = TextSecondary)
        }
        TextButton(onClick = onClick) {
            Text("Grant", color = Cyan, fontWeight = FontWeight.Bold)
        }
    }
}
