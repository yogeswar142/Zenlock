package com.zenlock.focusguard.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.data.db.entity.KeywordEntity
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.MainViewModel
import com.zenlock.focusguard.util.PermissionUtils

/**
 * Settings screen for configuring YouTube filtering, keywords, and app behavior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    val youtubeFilterEnabled by viewModel.youtubeFilterEnabled.collectAsState()
    val shortsBlockEnabled by viewModel.shortsBlockEnabled.collectAsState()
    val blockUnknownContent by viewModel.blockUnknownContent.collectAsState()
    val strictMode by viewModel.strictMode.collectAsState()
    val motivationalQuotes by viewModel.motivationalQuotes.collectAsState()
    val dndSyncEnabled by viewModel.dndSyncEnabled.collectAsState()
    
    val focusDuration by viewModel.focusDurationMinutes.collectAsState()
    val breakDuration by viewModel.breakDurationMinutes.collectAsState()
    val keywords by viewModel.keywords.collectAsState()

    val hasUsageStats by viewModel.hasUsageStatsPermission.collectAsState()
    val hasOverlay by viewModel.hasOverlayPermission.collectAsState()
    val hasAccessibility by viewModel.hasAccessibilityPermission.collectAsState()

    var showAddKeywordDialog by remember { mutableStateOf(false) }
    var keywordType by remember { mutableStateOf("block") }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "⚙️ Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ==================== PERMISSIONS ====================
        item {
            SectionHeader(title = "Permissions")
        }

        item {
            PermissionSettingItem(
                title = "Usage Access",
                description = "Required to detect foreground apps",
                isGranted = hasUsageStats,
                onClick = { PermissionUtils.openUsageStatsSettings(context) }
            )
        }
        item {
            PermissionSettingItem(
                title = "Draw Over Apps",
                description = "Required to show blocking overlay",
                isGranted = hasOverlay,
                onClick = { PermissionUtils.openOverlaySettings(context) }
            )
        }
        item {
            PermissionSettingItem(
                title = "Accessibility Service",
                description = "Required to read YouTube content",
                isGranted = hasAccessibility,
                onClick = { PermissionUtils.openAccessibilitySettings(context) }
            )
        }

        // ==================== YOUTUBE FILTERING ====================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "YouTube Filtering")
        }

        item {
            SettingToggleItem(
                title = "YouTube Content Filtering",
                description = "Analyze and filter YouTube videos during focus sessions",
                isChecked = youtubeFilterEnabled,
                onToggle = { viewModel.setYoutubeFilterEnabled(it) }
            )
        }

        item {
            SettingToggleItem(
                title = "Block YouTube Shorts",
                description = "Automatically block Shorts during focus sessions",
                isChecked = shortsBlockEnabled,
                enabled = youtubeFilterEnabled,
                onToggle = { viewModel.setShortsBlockEnabled(it) }
            )
        }

        item {
            SettingToggleItem(
                title = "Block Unclassified Videos",
                description = "Block videos that don't match any keywords (strict mode)",
                isChecked = blockUnknownContent,
                enabled = youtubeFilterEnabled,
                onToggle = { viewModel.setBlockUnknownContent(it) }
            )
        }

        // ==================== KEYWORD RULES ====================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Keyword Rules")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallFloatingActionButton(
                        onClick = {
                            keywordType = "channel"
                            showAddKeywordDialog = true
                        },
                        containerColor = Cyan.copy(alpha = 0.2f),
                        contentColor = Cyan
                    ) {
                        Icon(Icons.Default.Person, "Whitelist Channel", modifier = Modifier.size(18.dp))
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            keywordType = "allow"
                            showAddKeywordDialog = true
                        },
                        containerColor = Emerald.copy(alpha = 0.2f),
                        contentColor = Emerald
                    ) {
                        Icon(Icons.Default.Add, "Add allow keyword", modifier = Modifier.size(18.dp))
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            keywordType = "block"
                            showAddKeywordDialog = true
                        },
                        containerColor = Coral.copy(alpha = 0.2f),
                        contentColor = Coral
                    ) {
                        Icon(Icons.Default.Add, "Add block keyword", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Keyword groups
        val channelKeywords = keywords.filter { it.type == "channel" }
        val allowKeywords = keywords.filter { it.type == "allow" }
        val blockKeywords = keywords.filter { it.type == "block" }

        if (channelKeywords.isNotEmpty()) {
            item {
                Text(
                    text = "📺 Whitelisted Channels",
                    style = MaterialTheme.typography.bodySmall,
                    color = Cyan,
                    fontWeight = FontWeight.Medium
                )
            }
            item {
                KeywordChipGroup(
                    keywords = channelKeywords,
                    color = Cyan,
                    onDelete = { viewModel.deleteKeyword(it) },
                    onToggle = { viewModel.toggleKeyword(it) }
                )
            }
        }

        if (allowKeywords.isNotEmpty()) {
            item {
                Text(
                    text = "✅ Allow Keywords (educational)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Emerald,
                    fontWeight = FontWeight.Medium
                )
            }
            item {
                KeywordChipGroup(
                    keywords = allowKeywords,
                    color = Emerald,
                    onDelete = { viewModel.deleteKeyword(it) },
                    onToggle = { viewModel.toggleKeyword(it) }
                )
            }
        }

        if (blockKeywords.isNotEmpty()) {
            item {
                Text(
                    text = "🚫 Block Keywords (distracting)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Coral,
                    fontWeight = FontWeight.Medium
                )
            }
            item {
                KeywordChipGroup(
                    keywords = blockKeywords,
                    color = Coral,
                    onDelete = { viewModel.deleteKeyword(it) },
                    onToggle = { viewModel.toggleKeyword(it) }
                )
            }
        }

        // ==================== BEHAVIOR ====================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "Behavior")
        }

        item {
            SettingToggleItem(
                title = "Strict Mode",
                description = "Prevent stopping focus sessions early",
                isChecked = strictMode,
                onToggle = { viewModel.setStrictMode(it) }
            )
        }

        item {
            SettingToggleItem(
                title = "DND Synchronization",
                description = "Automatically enable Do Not Disturb during focus sessions",
                isChecked = dndSyncEnabled,
                onToggle = { viewModel.setDndSyncEnabled(it) }
            )
        }

        item {
            SettingToggleItem(
                title = "Motivational Quotes",
                description = "Show inspirational quotes on the blocking screen",
                isChecked = motivationalQuotes,
                onToggle = { viewModel.setMotivationalQuotes(it) }
            )
        }

        // App info
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🛡️ FocusGuard", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Version 1.0.0", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Stay focused. Stay productive.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Add keyword dialog
    if (showAddKeywordDialog) {
        AddKeywordDialog(
            type = keywordType,
            onDismiss = { showAddKeywordDialog = false },
            onAdd = { keyword ->
                viewModel.addKeyword(keyword, keywordType)
                showAddKeywordDialog = false
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Purple80
    )
}

@Composable
private fun SettingToggleItem(
    title: String,
    description: String,
    isChecked: Boolean,
    enabled: Boolean = true,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) Color.White else TextMuted,
                    fontSize = 14.sp
                )
                Text(
                    text = description,
                    color = if (enabled) TextSecondary else TextMuted,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = isChecked,
                onCheckedChange = onToggle,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Purple60,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun PermissionSettingItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        ),
        onClick = { if (!isGranted) onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isGranted) Emerald else Coral,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isGranted) "Granted" else description,
                    color = if (isGranted) Emerald else TextSecondary,
                    fontSize = 12.sp
                )
            }
            if (!isGranted) {
                TextButton(onClick = onClick) {
                    Text("Grant", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordChipGroup(
    keywords: List<KeywordEntity>,
    color: Color,
    onDelete: (KeywordEntity) -> Unit,
    onToggle: (KeywordEntity) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keywords.forEach { keyword ->
            val chipColor = if (keyword.isActive) color else TextMuted

            InputChip(
                selected = keyword.isActive,
                onClick = { onToggle(keyword) },
                label = {
                    Text(
                        text = keyword.keyword,
                        fontSize = 12.sp,
                        color = chipColor
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { onDelete(keyword) },
                        modifier = Modifier.size(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = chipColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                },
                colors = InputChipDefaults.inputChipColors(
                    containerColor = chipColor.copy(alpha = 0.1f),
                    selectedContainerColor = chipColor.copy(alpha = 0.2f)
                ),
                border = InputChipDefaults.inputChipBorder(
                    enabled = true,
                    selected = keyword.isActive,
                    borderColor = chipColor.copy(alpha = 0.3f),
                    selectedBorderColor = chipColor.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun AddKeywordDialog(
    type: String,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    val isAllow = type == "allow"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Text(
                text = when (type) {
                    "allow" -> "Add Allow Keyword"
                    "channel" -> "Whitelist Channel Name"
                    else -> "Add Block Keyword"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = when (type) {
                        "allow" -> "Videos matching this keyword will be allowed."
                        "channel" -> "All videos from this channel will be completely allowed."
                        else -> "Videos matching this keyword will be blocked."
                    },
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    placeholder = { Text("Enter keyword...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = when (type) {
                            "allow" -> Emerald
                            "channel" -> Cyan
                            else -> Coral
                        },
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = when (type) {
                            "allow" -> Emerald
                            "channel" -> Cyan
                            else -> Coral
                        },
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (keyword.isNotBlank()) onAdd(keyword) },
                enabled = keyword.isNotBlank()
            ) {
                Text(
                    "Add",
                    color = if (keyword.isNotBlank()) {
                        when (type) {
                            "allow" -> Emerald
                            "channel" -> Cyan
                            else -> Coral
                        }
                    } else TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
