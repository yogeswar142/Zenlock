package com.zenlock.focusguard.ui.screens.auth

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.data.network.ProfileSetupRequest
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.AuthState
import com.zenlock.focusguard.ui.viewmodel.AuthViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: AuthViewModel,
    onNavigateHome: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Date picker
    val calendar = Calendar.getInstance()
    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                dob = String.format("%02d/%02d/%04d", month + 1, day, year)
            },
            calendar.get(Calendar.YEAR) - 18,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }
    }

    // Dropdown data
    val countries = listOf("India", "USA", "UK", "Canada", "Australia", "Germany", "Japan", "France", "Brazil", "Other")
    val statesByCountry = mapOf(
        "India" to listOf("Andhra Pradesh", "Karnataka", "Kerala", "Maharashtra", "Tamil Nadu", "Telangana", "Delhi", "Uttar Pradesh", "Gujarat", "Rajasthan", "Other"),
        "USA" to listOf("California", "Texas", "Florida", "New York", "Illinois", "Pennsylvania", "Ohio", "Georgia", "Other"),
        "UK" to listOf("England", "Scotland", "Wales", "Northern Ireland"),
        "Canada" to listOf("Ontario", "Quebec", "British Columbia", "Alberta", "Other"),
        "Australia" to listOf("New South Wales", "Victoria", "Queensland", "Other"),
        "Germany" to listOf("Bavaria", "Berlin", "Hamburg", "Other"),
        "Japan" to listOf("Tokyo", "Osaka", "Kyoto", "Other"),
        "France" to listOf("Île-de-France", "Provence", "Normandy", "Other"),
        "Brazil" to listOf("São Paulo", "Rio de Janeiro", "Bahia", "Other"),
    )

    Scaffold(
        containerColor = ZenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Profile Setup",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "Zenlock",
                            style = MaterialTheme.typography.titleMedium,
                            color = ZenOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ZenPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZenBackground)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZenBackground.copy(alpha = 0.95f))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Step indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50)),
                        color = ZenPrimaryContainer,
                        trackColor = ZenSurfaceContainerHigh,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Step 1 of 1",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZenOutline
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.setupProfile(
                            ProfileSetupRequest(username, dob, gender, country, state, city)
                        ) {
                            onNavigateHome()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZenPrimaryContainer,
                        disabledContainerColor = ZenSurfaceContainerHigh
                    ),
                    enabled = username.isNotBlank() && authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Continue",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Header ──
            Text(
                text = "Complete your profile",
                style = MaterialTheme.typography.headlineLarge,
                color = ZenOnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Just a few details to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = ZenOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (authState is AuthState.Error) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Coral.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Coral.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = Coral,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Username ──
            GlassCard {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "USERNAME",
                        color = ZenPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("e.g. zen_master") },
                        trailingIcon = {
                            Icon(
                                Icons.Outlined.AlternateEmail,
                                contentDescription = null,
                                tint = ZenOutline
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = defaultTextFieldColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Date of Birth (clickable) ──
            GlassCard {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "DATE OF BIRTH",
                        color = ZenPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dob,
                        onValueChange = {},
                        placeholder = { Text("mm/dd/yyyy") },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = ZenOutline
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePicker.show() },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color(0xFF161B26),
                            disabledBorderColor = ZenSurfaceContainerHighest,
                            disabledTextColor = ZenOnSurface,
                            disabledPlaceholderColor = ZenOutline
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Gender ──
            GlassCard {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "GENDER",
                        color = ZenPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenderChip("Male", gender == "Male") { gender = "Male" }
                        GenderChip("Female", gender == "Female") { gender = "Female" }
                        GenderChip("Other", gender == "Other") { gender = "Other" }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Location ──
            GlassCard {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "LOCATION DETAILS",
                        color = ZenPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Country dropdown
                    ZenDropdown(
                        value = country,
                        placeholder = "Country",
                        options = countries,
                        onSelected = {
                            country = it
                            state = ""
                            city = ""
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // State dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            ZenDropdown(
                                value = state,
                                placeholder = "State",
                                options = statesByCountry[country] ?: emptyList(),
                                onSelected = {
                                    state = it
                                    city = ""
                                }
                            )
                        }

                        // City text input (free-form)
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            placeholder = { Text("City") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = defaultTextFieldColors()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ── Reusable Glass Card ──
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZenCard.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        content()
    }
}

// ── Dropdown Component ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZenDropdown(
    value: String,
    placeholder: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder) },
            trailingIcon = {
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ZenOutline
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = defaultTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ZenCard
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = if (option == value) ZenPrimaryContainer else ZenOnSurface
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

// ── Text field color defaults ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun defaultTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF161B26),
    unfocusedContainerColor = Color(0xFF161B26),
    focusedBorderColor = ZenPrimaryContainer,
    unfocusedBorderColor = ZenSurfaceContainerHighest,
    focusedTextColor = ZenOnSurface,
    unfocusedTextColor = ZenOnSurface,
    focusedPlaceholderColor = ZenOutline,
    unfocusedPlaceholderColor = ZenOutline,
    cursorColor = ZenPrimaryContainer
)

// ── Gender chip ──
@Composable
private fun GenderChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) ZenPrimaryContainer else ZenSurfaceContainer,
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(1.dp, ZenOutline.copy(alpha = 0.5f))
        } else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            color = if (isSelected) Color.White else ZenOnSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
