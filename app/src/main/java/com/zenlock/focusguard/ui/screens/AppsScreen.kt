package com.zenlock.focusguard.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.MainViewModel

/**
 * Apps screen for managing blocked apps.
 * Shows currently blocked apps and allows adding/removing from the block list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(viewModel: MainViewModel) {
    val blockedApps by viewModel.blockedApps.collectAsState()
    val installedApps by viewModel.filteredInstalledApps.collectAsState()
    val searchQuery by viewModel.appSearchQuery.collectAsState()
    var showAppPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Blocked Apps",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${blockedApps.size} apps blocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            FloatingActionButton(
                onClick = { showAppPicker = !showAppPicker },
                containerColor = Purple60,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    if (showAppPicker) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Add app"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showAppPicker) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setAppSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Search", tint = TextSecondary)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple60,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    cursorColor = Purple60,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // App picker list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(installedApps) { appInfo ->
                    AppPickerItem(
                        appName = appInfo.appName,
                        packageName = appInfo.packageName,
                        isBlocked = appInfo.isBlocked,
                        onToggle = { viewModel.toggleAppBlocked(appInfo) }
                    )
                }
            }
        } else {
            // Blocked apps list
            if (blockedApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📱", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No apps blocked yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add apps to your block list",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(blockedApps) { app ->
                        BlockedAppItem(
                            appName = app.appName,
                            packageName = app.packageName,
                            onRemove = { viewModel.removeBlockedApp(app) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppPickerItem(
    appName: String,
    packageName: String,
    isBlocked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBlocked) Color(0x33FF6B6B) else DarkCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isBlocked) Coral.copy(alpha = 0.3f) else DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = if (isBlocked) Coral else TextSecondary,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Switch(
                checked = isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Coral,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun BlockedAppItem(
    appName: String,
    packageName: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App initial
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Coral.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = Coral,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Blocked badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Coral.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "BLOCKED",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Coral,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
