package com.zenlock.focusguard.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.data.network.LoginRequest
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.AuthState
import com.zenlock.focusguard.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onNavigateToSignUp: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfileSetup: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val state = authState as AuthState.Success
                if (state.isNewUser || !state.isProfileComplete) {
                    onNavigateToProfileSetup()
                } else {
                    onNavigateToHome()
                }
                viewModel.resetState()
            }
            is AuthState.Error -> {
                // Error displayed in UI below
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenBackground),
        contentAlignment = Alignment.Center
    ) {
        // Abstract Background Glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .blur(100.dp)
                .background(ZenPrimaryContainer.copy(alpha = 0.12f), shape = CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Branding bar (top-left) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = ZenPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Zenlock",
                    style = MaterialTheme.typography.titleMedium,
                    color = ZenOnSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── Hero icon ──
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ZenSurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = ZenPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Zenlock",
                style = MaterialTheme.typography.displaySmall,
                color = ZenOnSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Stay focused. Stay in control.",
                style = MaterialTheme.typography.bodyLarge,
                color = ZenOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Glass Panel Card ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = ZenCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Error banner
                    AnimatedVisibility(
                        visible = authState is AuthState.Error,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Coral.copy(alpha = 0.1f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Coral.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = (authState as? AuthState.Error)?.message ?: "",
                                color = Coral,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Email Field
                    Column {
                        Text(
                            "Email Address",
                            color = ZenOnSurface,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = {
                                Text("name@company.com", color = ZenOutline)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    "Email",
                                    tint = ZenOutline
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF161B26),
                                unfocusedContainerColor = Color(0xFF161B26),
                                focusedBorderColor = ZenPrimaryContainer,
                                unfocusedBorderColor = ZenSurfaceContainerHighest,
                                focusedTextColor = ZenOnSurface,
                                unfocusedTextColor = ZenOnSurface,
                                cursorColor = ZenPrimaryContainer
                            )
                        )
                    }

                    // Password Field
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Password",
                                color = ZenOnSurface,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                "Forgot password?",
                                color = ZenPrimaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.clickable { /* TODO: forgot password flow */ }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = {
                                Text("••••••••", color = ZenOutline)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    "Lock",
                                    tint = ZenOutline
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = ZenOutline
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF161B26),
                                unfocusedContainerColor = Color(0xFF161B26),
                                focusedBorderColor = ZenPrimaryContainer,
                                unfocusedBorderColor = ZenSurfaceContainerHighest,
                                focusedTextColor = ZenOnSurface,
                                unfocusedTextColor = ZenOnSurface,
                                cursorColor = ZenPrimaryContainer
                            )
                        )
                    }

                    // Sign In Button
                    Button(
                        onClick = {
                            viewModel.login(LoginRequest(email.trim(), password))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZenPrimaryContainer,
                            disabledContainerColor = ZenSurfaceContainerHigh
                        ),
                        enabled = email.isNotBlank() && password.isNotBlank()
                                && authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Sign In",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Or divider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.1f)
                        )
                        Text(
                            "OR CONTINUE WITH",
                            color = ZenOutline,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.1f)
                        )
                    }

                    // Google Sign In
                    OutlinedButton(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            "G",
                            color = Color(0xFFDB4437),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Continue with Google",
                            color = ZenOnSurface,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Footer ──
            Row {
                Text("Don't have an account? ", color = ZenOutline)
                Text(
                    "Sign Up",
                    color = ZenPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Copyright footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "© 2024 Zenlock. Digital Sanctuary.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenOutlineVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
