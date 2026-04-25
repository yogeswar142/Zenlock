package com.zenlock.focusguard.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.MainViewModel

/**
 * Apps screen – Zenlock "Digital Sanctuary" design.
 *
 * Search bar, featured "Zen Mode" hero card, time-saved stat,
 * and a list of installed apps with toggle switches.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(viewModel: MainViewModel) {
    val blockedApps by viewModel.blockedApps.collectAsState()
    val installedApps by viewModel.filteredInstalledApps.collectAsState()
    val searchQuery by viewModel.appSearchQuery.collectAsState()
    var showAppPicker by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenBackground)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // ── Search Bar ──
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setAppSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search apps...", color = ZenOutline)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Search", tint = ZenOutline)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZenPrimaryContainer,
                    unfocusedBorderColor = ZenCard,
                    focusedContainerColor = Color(0xFF161B26),
                    unfocusedContainerColor = Color(0xFF161B26),
                    cursorColor = ZenPrimaryContainer,
                    focusedTextColor = ZenOnSurface,
                    unfocusedTextColor = ZenOnSurface
                ),
                shape = RoundedCornerShape(50),
                singleLine = true
            )
        }

        // ── Featured Bento Section ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Zen Mode hero card
                Card(
                    modifier = Modifier
                        .weight(2f)
                        .height(170.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        ZenCard.copy(alpha = 0.6f),
                                        ZenCard.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "ACTIVE FOCUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZenPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Zen Mode",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = ZenOnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Blocking ${blockedApps.size} apps.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZenOutline,
                                    lineHeight = 16.sp,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = { showAppPicker = !showAppPicker },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ZenPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Customize",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Time saved card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(170.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ZenCard),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(50))
                                .background(ZenPrimaryContainer.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.TimerOff,
                                contentDescription = null,
                                tint = ZenPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "2.4h",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ZenOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "TIME SAVED TODAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZenOutline,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // ── App List Header ──
        item {
            Text(
                text = "INSTALLED APPLICATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = ZenOutline,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // ── App List Items ──
        items(installedApps) { appInfo ->
            AppListItem(
                appName = appInfo.appName,
                packageName = appInfo.packageName,
                isBlocked = appInfo.isBlocked,
                onToggle = { viewModel.toggleAppBlocked(appInfo) }
            )
        }

        // Empty state
        if (installedApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Apps,
                            contentDescription = null,
                            tint = ZenOutline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No apps found",
                            style = MaterialTheme.typography.titleSmall,
                            color = ZenOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppListItem(
    appName: String,
    packageName: String,
    isBlocked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ZenCard.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161B26)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = ZenOnSurfaceVariant,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ZenOnSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = if (isBlocked) "Blocked" else "Allowed",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isBlocked) ZenPrimaryContainer else ZenOutline
                )
            }

            Switch(
                checked = isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ZenPrimaryContainer,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                    uncheckedTrackColor = ZenSurfaceContainerHigh
                )
            )
        }
    }
}
