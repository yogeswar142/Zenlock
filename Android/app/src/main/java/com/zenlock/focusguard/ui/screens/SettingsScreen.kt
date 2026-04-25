package com.zenlock.focusguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.data.db.entity.KeywordEntity
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.MainViewModel
import com.zenlock.focusguard.util.PermissionUtils

/**
 * Settings screen – Zenlock "Digital Sanctuary" design.
 *
 * Grouped settings sections (General, Focus, Advanced) with glass cards,
 * violet accent icons, toggle switches, and a Sign Out footer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToProfile: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
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
            .background(ZenBackground)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // ═══════════════════ GENERAL ═══════════════════
        item {
            SectionLabel("GENERAL")
        }

        item {
            SettingsGroup {
                // Account Profile
                SettingsNavigationItem(
                    icon = Icons.Outlined.Person,
                    title = "Account Profile",
                    subtitle = "Manage your personal data",
                    onClick = onNavigateToProfile
                )

                GroupDivider()

                // Notifications
                SettingsToggleRow(
                    icon = Icons.Outlined.NotificationsActive,
                    title = "Notifications",
                    isChecked = motivationalQuotes,
                    onToggle = { viewModel.setMotivationalQuotes(it) }
                )
            }
        }

        // ═══════════════════ FOCUS ═══════════════════
        item {
            SectionLabel("FOCUS")
        }

        item {
            SettingsGroup {
                // Strict Mode
                SettingsToggleRow(
                    icon = Icons.Outlined.Lock,
                    title = "Strict Mode",
                    subtitle = "Block all bypass attempts",
                    isChecked = strictMode,
                    onToggle = { viewModel.setStrictMode(it) }
                )

                GroupDivider()

                // DND Sync (Keep Screen On equivalent)
                SettingsToggleRow(
                    icon = Icons.Outlined.LightMode,
                    title = "DND Sync",
                    subtitle = "Enable Do Not Disturb during sessions",
                    isChecked = dndSyncEnabled,
                    onToggle = { viewModel.setDndSyncEnabled(it) }
                )

                GroupDivider()

                // Focus Schedule
                SettingsNavigationItem(
                    icon = Icons.Outlined.CalendarMonth,
                    title = "Focus Schedule",
                    trailing = "Mon-Fri"
                )
            }
        }

        // ═══════════════════ YOUTUBE ═══════════════════
        item {
            SectionLabel("YOUTUBE FILTERING")
        }

        item {
            SettingsGroup {
                SettingsToggleRow(
                    icon = Icons.Outlined.OndemandVideo,
                    title = "Content Filtering",
                    subtitle = "Analyze YouTube videos during focus",
                    isChecked = youtubeFilterEnabled,
                    onToggle = { viewModel.setYoutubeFilterEnabled(it) }
                )

                GroupDivider()

                SettingsToggleRow(
                    icon = Icons.Outlined.FlashOn,
                    title = "Block Shorts",
                    subtitle = "Auto-block YouTube Shorts",
                    isChecked = shortsBlockEnabled,
                    enabled = youtubeFilterEnabled,
                    onToggle = { viewModel.setShortsBlockEnabled(it) }
                )

                GroupDivider()

                SettingsToggleRow(
                    icon = Icons.Outlined.Block,
                    title = "Block Unclassified",
                    subtitle = "Block videos not matching any keyword",
                    isChecked = blockUnknownContent,
                    enabled = youtubeFilterEnabled,
                    onToggle = { viewModel.setBlockUnknownContent(it) }
                )
            }
        }

        // ═══════════════════ KEYWORD RULES ═══════════════════
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KEYWORD RULES",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenOutline,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallFloatingActionButton(
                        onClick = { keywordType = "channel"; showAddKeywordDialog = true },
                        containerColor = Cyan.copy(alpha = 0.15f),
                        contentColor = Cyan,
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Person, "Channel", modifier = Modifier.size(18.dp))
                    }
                    SmallFloatingActionButton(
                        onClick = { keywordType = "allow"; showAddKeywordDialog = true },
                        containerColor = Emerald.copy(alpha = 0.15f),
                        contentColor = Emerald,
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Add, "Allow", modifier = Modifier.size(18.dp))
                    }
                    SmallFloatingActionButton(
                        onClick = { keywordType = "block"; showAddKeywordDialog = true },
                        containerColor = Coral.copy(alpha = 0.15f),
                        contentColor = Coral,
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Add, "Block", modifier = Modifier.size(18.dp))
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
                Text("📺 Whitelisted Channels", style = MaterialTheme.typography.bodySmall, color = Cyan, fontWeight = FontWeight.Medium)
            }
            item {
                KeywordChipGroup(keywords = channelKeywords, color = Cyan, onDelete = { viewModel.deleteKeyword(it) }, onToggle = { viewModel.toggleKeyword(it) })
            }
        }
        if (allowKeywords.isNotEmpty()) {
            item {
                Text("✅ Allow Keywords", style = MaterialTheme.typography.bodySmall, color = Emerald, fontWeight = FontWeight.Medium)
            }
            item {
                KeywordChipGroup(keywords = allowKeywords, color = Emerald, onDelete = { viewModel.deleteKeyword(it) }, onToggle = { viewModel.toggleKeyword(it) })
            }
        }
        if (blockKeywords.isNotEmpty()) {
            item {
                Text("🚫 Block Keywords", style = MaterialTheme.typography.bodySmall, color = Coral, fontWeight = FontWeight.Medium)
            }
            item {
                KeywordChipGroup(keywords = blockKeywords, color = Coral, onDelete = { viewModel.deleteKeyword(it) }, onToggle = { viewModel.toggleKeyword(it) })
            }
        }

        // ═══════════════════ ADVANCED ═══════════════════
        item {
            SectionLabel("ADVANCED")
        }

        item {
            SettingsGroup {
                SettingsNavigationItem(
                    icon = Icons.Outlined.Security,
                    title = "Privacy & Security"
                )

                GroupDivider()

                SettingsNavigationItem(
                    icon = Icons.Outlined.Storage,
                    title = "Data Usage",
                    trailing = "24.5 MB"
                )
            }
        }

        // ── Permissions ──
        item {
            SectionLabel("PERMISSIONS")
        }

        item {
            SettingsGroup {
                PermissionRow(
                    title = "Usage Access",
                    isGranted = hasUsageStats,
                    onClick = { PermissionUtils.openUsageStatsSettings(context) }
                )
                GroupDivider()
                PermissionRow(
                    title = "Draw Over Apps",
                    isGranted = hasOverlay,
                    onClick = { PermissionUtils.openOverlaySettings(context) }
                )
                GroupDivider()
                PermissionRow(
                    title = "Accessibility Service",
                    isGranted = hasAccessibility,
                    onClick = { PermissionUtils.openAccessibilitySettings(context) }
                )
            }
        }

        // ── Sign Out ──
        item {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ZenError.copy(alpha = 0.2f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ZenError
                )
            ) {
                Icon(Icons.Outlined.Logout, "Sign Out", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign Out",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ── App version footer ──
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Zenlock v2.4.0-stable",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenOutlineVariant
                )
                Text(
                    "Crafted for deep focus.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenOutlineVariant.copy(alpha = 0.6f)
                )
            }
        }
    }

    // ── Add keyword dialog ──
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

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Design System Components
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = ZenOutline,
        letterSpacing = 3.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 28.dp, bottom = 12.dp, start = 2.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZenCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        thickness = 1.dp,
        color = Color.White.copy(alpha = 0.05f)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    enabled: Boolean = true,
    onToggle: (Boolean) -> Unit
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
            tint = if (enabled) ZenPrimaryContainer else ZenOutlineVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) ZenOnSurface else ZenOutline
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) ZenOutline else ZenOutlineVariant
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ZenPrimaryContainer,
                uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                uncheckedTrackColor = ZenSurfaceContainerHigh
            )
        )
    }
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = ZenOnSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenOutline
                )
            }
        }
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                color = ZenOutline
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = ZenOutlineVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PermissionRow(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isGranted) { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isGranted) Icons.Outlined.CheckCircle else Icons.Outlined.Error,
            contentDescription = null,
            tint = if (isGranted) Emerald else Coral,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = ZenOnSurface
            )
            Text(
                text = if (isGranted) "Granted" else "Tap to grant",
                style = MaterialTheme.typography.labelSmall,
                color = if (isGranted) Emerald else ZenOutline
            )
        }
        if (!isGranted) {
            TextButton(onClick = onClick) {
                Text("Grant", color = ZenPrimaryContainer, fontWeight = FontWeight.Bold)
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
            val chipColor = if (keyword.isActive) color else ZenOutline

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
                ),
                shape = RoundedCornerShape(50)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ZenCard,
        title = {
            Text(
                text = when (type) {
                    "allow" -> "Add Allow Keyword"
                    "channel" -> "Whitelist Channel Name"
                    else -> "Add Block Keyword"
                },
                color = ZenOnSurface,
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
                    color = ZenOnSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    placeholder = { Text("Enter keyword...", color = ZenOutline) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZenPrimaryContainer,
                        unfocusedBorderColor = ZenSurfaceContainerHigh,
                        cursorColor = ZenPrimaryContainer,
                        focusedTextColor = ZenOnSurface,
                        unfocusedTextColor = ZenOnSurface
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
                    color = if (keyword.isNotBlank()) ZenPrimaryContainer else ZenOutline,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ZenOnSurfaceVariant)
            }
        }
    )
}
