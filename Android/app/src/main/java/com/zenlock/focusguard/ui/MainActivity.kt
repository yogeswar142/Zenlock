package com.zenlock.focusguard.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.zenlock.focusguard.R
import com.zenlock.focusguard.ui.screens.*
import com.zenlock.focusguard.ui.screens.auth.*
import com.zenlock.focusguard.ui.theme.*
import com.zenlock.focusguard.ui.viewmodel.MainViewModel
import com.zenlock.focusguard.ui.viewmodel.AuthViewModel
import com.zenlock.focusguard.data.datastore.TokenManager
import com.zenlock.focusguard.util.PermissionUtils

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var pendingGoogleAuth: ((String) -> Unit)? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled in ViewModel */ }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                Log.d("MainActivity", "Google Sign-In successful, got ID token")
                pendingGoogleAuth?.invoke(idToken)
            } else {
                Log.e("MainActivity", "Google Sign-In: ID token was null")
                pendingGoogleAuth?.invoke("") // Signal failure
            }
        } catch (e: ApiException) {
            Log.e("MainActivity", "Google Sign-In failed: code=${e.statusCode}", e)
            pendingGoogleAuth?.invoke("") // Signal failure
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            FocusGuardTheme {
                FocusGuardApp(
                    onGoogleSignIn = { callback ->
                        pendingGoogleAuth = callback
                        // Sign out first to always show the account picker
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Permissions might have changed while the user was in Settings
    }
}

/**
 * Navigation destinations for the app.
 */
sealed class Screen(
    val route: String,
    val title: String = "",
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    data object Splash : Screen("splash")
    data object SignIn : Screen("signin")
    data object SignUp : Screen("signup")
    data object ProfileSetup : Screen("profile_setup")
    data object Profile : Screen("profile")
    
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Apps : Screen("apps", "Apps", Icons.Filled.Apps, Icons.Outlined.Apps)
    data object Stats : Screen("stats", "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavItems = listOf(Screen.Home, Screen.Apps, Screen.Stats, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusGuardApp(
    onGoogleSignIn: ((idToken: String) -> Unit) -> Unit = {}
) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Refresh permissions when app becomes visible
    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        containerColor = ZenBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xE6020510), // slate-950/90
                    contentColor = ZenOnSurface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon!! else screen.unselectedIcon!!,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.5.sp,
                                    fontFamily = Manrope
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ZenPrimaryContainer,
                                selectedTextColor = ZenPrimaryContainer,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = ZenOutline,
                                unselectedTextColor = ZenOutline
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                val tokenManager = remember { TokenManager(navController.context) }
                val token by tokenManager.jwtToken.collectAsState(initial = "loading")
                val isProfileComplete by tokenManager.isProfileComplete.collectAsState(initial = false)

                LaunchedEffect(token, isProfileComplete) {
                    if (token != "loading") {
                        if (token.isNullOrEmpty()) {
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else if (!isProfileComplete) {
                            navController.navigate(Screen.ProfileSetup.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ZenPrimaryContainer)
                }
            }
            
            composable(Screen.SignIn.route) {
                SignInScreen(
                    viewModel = authViewModel,
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onNavigateToHome = { 
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onNavigateToProfileSetup = {
                        navController.navigate(Screen.ProfileSetup.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onGoogleSignInClick = {
                        onGoogleSignIn { idToken ->
                            if (idToken.isNotEmpty()) {
                                authViewModel.googleAuth(idToken)
                            } else {
                                authViewModel.setError("Google Sign-In was cancelled or failed")
                            }
                        }
                    }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSignIn = { 
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    onNavigateToProfileSetup = {
                        navController.navigate(Screen.ProfileSetup.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    onGoogleSignInClick = {
                        onGoogleSignIn { idToken ->
                            if (idToken.isNotEmpty()) {
                                authViewModel.googleAuth(idToken)
                            } else {
                                authViewModel.setError("Google Sign-In was cancelled or failed")
                            }
                        }
                    }
                )
            }
            composable(Screen.ProfileSetup.route) {
                ProfileSetupScreen(
                    viewModel = authViewModel,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    mainViewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(0) // Clear backstack entirely
                        }
                    }
                )
            }
            
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel)
            }
            composable(Screen.Apps.route) {
                AppsScreen(viewModel = viewModel)
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onSignOut = {
                        authViewModel.logout {
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(0)
                            }
                        }
                    }
                )
            }
        }
    }
}
