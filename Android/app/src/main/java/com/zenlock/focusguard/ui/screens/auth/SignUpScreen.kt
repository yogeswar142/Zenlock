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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlock.focusguard.data.network.SignupRequest
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.AuthState
import com.zenlock.focusguard.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToProfileSetup: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onNavigateToProfileSetup()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenBackground)
    ) {
        // Abstract Background Glows
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-50).dp)
                .size(250.dp)
                .blur(120.dp)
                .background(ZenPrimaryContainer.copy(alpha = 0.1f), shape = CircleShape)
        )

        // Top Navigation
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(top = 40.dp, start = 16.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = ZenPrimaryContainer
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Branding Section ──
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ZenPrimaryContainer, Color(0xFF6844C7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Create your account",
                style = MaterialTheme.typography.headlineLarge,
                color = ZenOnSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Experience deep work in a distraction-free\ndigital sanctuary.",
                style = MaterialTheme.typography.bodyMedium,
                color = ZenOnSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Action Card ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = ZenCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.08f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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

                    // Google Sign-Up (Primary)
                    Button(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZenPrimaryContainer
                        )
                    ) {
                        Text(
                            "G",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Divider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.1f)
                        )
                        Text(
                            "or",
                            color = ZenOutline.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.1f)
                        )
                    }

                    // Email Field
                    Column {
                        Text(
                            "Email",
                            color = ZenOnSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = {
                                Text(
                                    "email@example.com",
                                    color = ZenOutline.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
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
                        Text(
                            "Password",
                            color = ZenOnSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = {
                                Text(
                                    "••••••••",
                                    color = ZenOutline.copy(alpha = 0.5f)
                                )
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
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

                    // Next Step Button
                    TextButton(
                        onClick = {
                            viewModel.signup(SignupRequest(email.trim(), password))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = email.isNotBlank() && password.length >= 6
                                && authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                color = ZenPrimaryContainer,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Next step",
                                color = if (email.isNotBlank() && password.length >= 6)
                                    ZenPrimaryContainer
                                else
                                    ZenOutline,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Footer ──
            Row {
                Text("Already have an account? ", color = ZenOnSurfaceVariant)
                Text(
                    "Sign In",
                    color = ZenPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }
        }
    }
}
