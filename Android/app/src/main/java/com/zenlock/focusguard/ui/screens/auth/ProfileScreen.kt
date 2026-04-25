package com.zenlock.focusguard.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.AuthViewModel
import com.zenlock.focusguard.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val monthlyFocusTime by mainViewModel.monthlyFocusTime.collectAsState()
    val userStreak by mainViewModel.userStreak.collectAsState()
    val blockedAttempts by mainViewModel.todayBlockedAttempts.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.loadProfile()
        mainViewModel.loadStatistics()
    }

    Scaffold(
        containerColor = ZenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        color = ZenOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ZenPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZenBackground)
            )
        }
    ) { innerPadding ->
        if (userProfile == null) {
            // ── Loading State with shimmer ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Shimmer avatar
                    ShimmerBox(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .width(160.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .width(120.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        } else {
            val user = userProfile!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // ── Avatar Section ──
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        ZenPrimaryContainer,
                                        Color(0xFF6844C7)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user.displayName ?: user.username ?: user.email)
                                .take(1)
                                .uppercase(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Edit badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ZenPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Name & Location ──
                Text(
                    text = user.displayName ?: user.username ?: "Unknown User",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ZenOnSurface,
                    fontWeight = FontWeight.Bold
                )

                if (user.city != null || user.country != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = ZenOutline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = listOfNotNull(user.city, user.country)
                                .filter { it.isNotBlank() }
                                .joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZenOutline
                        )
                    }
                } else {
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZenOutline
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Status badge ──
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Emerald.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Emerald.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Emerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Focused today",
                            style = MaterialTheme.typography.labelMedium,
                            color = Emerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Stats Row ──
                val focusHours = monthlyFocusTime / 3600
                val focusMinutes = (monthlyFocusTime % 3600) / 60
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatCard(
                        icon = Icons.Outlined.Timer,
                        value = "${focusHours}h\n${focusMinutes}m",
                        label = "Monthly\nFocus",
                        color = ZenPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        icon = Icons.Outlined.LocalFireDepartment,
                        value = "${userStreak}\nDays",
                        label = "Streak",
                        color = Amber,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        icon = Icons.Outlined.Block,
                        value = blockedAttempts.toString(),
                        label = "Apps\nBlocked",
                        color = Coral,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Account Settings Section ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "ACCOUNT SETTINGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZenOutline,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ZenCard),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.05f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Outlined.PersonOutline,
                                title = "Edit Profile"
                            )
                            MenuDivider()
                            ProfileMenuItem(
                                icon = Icons.Outlined.History,
                                title = "Focus History"
                            )
                            MenuDivider()
                            ProfileMenuItem(
                                icon = Icons.Outlined.Notifications,
                                title = "Notifications"
                            )
                            MenuDivider()
                            ProfileMenuItem(
                                icon = Icons.Outlined.Security,
                                title = "Privacy & Security"
                            )
                            MenuDivider()
                            ProfileMenuItem(
                                icon = Icons.Outlined.HelpOutline,
                                title = "Help & Support"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Logout Button ──
                OutlinedButton(
                    onClick = { authViewModel.logout(onLogout) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Coral.copy(alpha = 0.3f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Coral)
                ) {
                    Icon(
                        Icons.Outlined.Logout,
                        "Logout",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Version ──
                Text(
                    text = "Zenlock Version 1.0.4",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenOutlineVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ── Sub-components ──

@Composable
private fun ProfileStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZenCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = ZenOnSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ZenOutline,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = ZenPrimaryContainer,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = ZenOnSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = ZenOutlineVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        thickness = 1.dp,
        color = Color.White.copy(alpha = 0.05f)
    )
}

@Composable
private fun ShimmerBox(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        ZenSurfaceContainerLow,
        ZenSurfaceContainerHigh,
        ZenSurfaceContainerLow
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    Box(
        modifier = modifier.background(
            Brush.horizontalGradient(
                colors = shimmerColors,
                startX = translateAnim - 500f,
                endX = translateAnim
            )
        )
    )
}
