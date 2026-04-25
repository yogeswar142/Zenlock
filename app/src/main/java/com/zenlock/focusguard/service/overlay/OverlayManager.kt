package com.zenlock.focusguard.service.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.delay

/**
 * Manages the full-screen blocking overlay displayed when a blocked app is opened.
 *
 * The overlay uses SYSTEM_ALERT_WINDOW permission to draw over other apps.
 * It uses Jetpack Compose for the UI and implements necessary lifecycle owners
 * for Compose to work outside of an Activity context.
 */
class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: FrameLayout? = null
    private var isShowing = false

    // Motivational quotes
    private val quotes = listOf(
        "Your future self will thank you for the focus you find today.",
        "The secret of getting ahead is getting started.",
        "Focus on being productive instead of busy.",
        "Stay focused, go after your dreams and keep moving toward your goals.",
        "The successful warrior is the average man, with laser-like focus.",
        "Concentrate all your thoughts upon the work at hand.",
        "Where focus goes, energy flows.",
        "Do what you have to do until you can do what you want to do.",
        "Starve your distractions and feed your focus.",
        "Your future is created by what you do today, not tomorrow."
    )

    /**
     * Show the blocking overlay with an optional custom message.
     */
    fun showOverlay(reason: String = "This app is temporarily blocked", onGoBackAction: () -> Unit = {}) {
        if (isShowing) return

        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                // FLAG_NOT_FOCUSABLE removed so overlay captures all touches
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val lifecycleOwner = OverlayLifecycleOwner()
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

            val composeView = ComposeView(context).apply {
                setContent {
                    BlockingOverlayContent(
                        reason = reason,
                        quote = quotes.random(),
                        onGoBack = { 
                            hideOverlay()
                            onGoBackAction()
                        }
                    )
                }
            }

            overlayView = FrameLayout(context).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                addView(composeView)
            }

            windowManager.addView(overlayView, params)
            isShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Hide and remove the blocking overlay.
     */
    fun hideOverlay() {
        if (!isShowing) return

        try {
            overlayView?.let {
                windowManager.removeView(it)
            }
            overlayView = null
            isShowing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isOverlayShowing(): Boolean = isShowing
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Zenlock "Digital Sanctuary" Overlay Design
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// Color constants – overlay runs outside the theme so we define them inline
private val OverlayBg = Color(0xFF0A0E17)
private val OverlayPrimaryContainer = Color(0xFF9D7BFF)
private val OverlayOnPrimaryContainer = Color(0xFF320085)
private val OverlayPrimary = Color(0xFFCEBDFF)
private val OverlayOnSurface = Color(0xFFE6E0EC)
private val OverlayOnSurfaceVariant = Color(0xFFCBC3D5)
private val OverlayCard = Color(0xFF1F2937)
private val OverlayOutline = Color(0xFF948E9F)

/**
 * Compose UI for the "Focus Mode Active" overlay.
 * Matches the blocked_app_overlay design screenshot.
 */
@Composable
private fun BlockingOverlayContent(
    reason: String,
    quote: String,
    onGoBack: () -> Unit
) {
    var isBreathing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OverlayBg)
    ) {
        // ── Radial glows ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            OverlayPrimaryContainer.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6844C7).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 500f
                    )
                )
        )

        if (isBreathing) {
            BreatheExercise(onComplete = onGoBack)
        } else {
            // ── Main Content ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.weight(0.15f))

                // Lock icon with glow
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = OverlayPrimaryContainer,
                        modifier = Modifier.size(72.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title
                    Text(
                        text = "Focus Mode\nActive",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = OverlayOnSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 44.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = reason,
                        fontSize = 16.sp,
                        color = OverlayOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(0.08f))

                // ── Motivational Quote Card (glassmorphism) ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = OverlayCard.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Quote marks
                        Text(
                            text = "❝",
                            fontSize = 24.sp,
                            color = OverlayPrimary.copy(alpha = 0.4f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = quote,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OverlayOnSurface,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Accent line
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .background(
                                    OverlayPrimary.copy(alpha = 0.2f),
                                    RoundedCornerShape(50)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.08f))

                // ── Refocus Button ──
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { isBreathing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OverlayPrimaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Go Back",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Refocus & Go Back",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Zenlock branding
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.alpha(0.4f)
                    ) {
                        Text(
                            text = "Zenlock",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OverlayOnSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(OverlayPrimaryContainer, CircleShape)
                        )
                        Text(
                            text = "SECURITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OverlayOnSurface,
                            letterSpacing = 3.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.05f))
            }
        }
    }
}

/**
 * Breathing exercise – "Digital Sanctuary" variant.
 * Large orb with radial gradient, Breathe In / Hold / Breathe Out phases,
 * heart icon at center, close (X) + "DIGITAL SANCTUARY" header.
 */
@Composable
private fun BreatheExercise(onComplete: () -> Unit) {
    var phase by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        delay(500)
        phase = 1 // Breathe In
        delay(3000)
        phase = 2 // Hold
        delay(1000)
        phase = 3 // Breathe Out
        delay(3000)
        onComplete()
    }

    val scale by animateFloatAsState(
        targetValue = when (phase) {
            1 -> 2.5f
            2 -> 2.5f
            3 -> 1.0f
            else -> 1.0f
        },
        animationSpec = tween(
            durationMillis = if (phase == 1 || phase == 3) 3000 else 1000,
            easing = FastOutSlowInEasing
        ),
        label = "breathScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (phase > 0) 1f else 0f,
        animationSpec = tween(1000),
        label = "breathAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OverlayBg)
    ) {
        // ── Background decorative blurs ──
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            OverlayPrimaryContainer.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            OverlayOnPrimaryContainer.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // ── Header: X + DIGITAL SANCTUARY ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .statusBarsPadding()
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer to balance
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "DIGITAL SANCTUARY",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // ── Center Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Phase text
            Text(
                text = when (phase) {
                    1 -> "Breathe In..."
                    2 -> "Hold..."
                    3 -> "Breathe Out..."
                    else -> ""
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = OverlayPrimary.copy(alpha = alpha * 0.8f),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Breathing Orb ──
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(Color.Transparent)
                        .alpha(0.2f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Transparent,
                                CircleShape
                            )
                            .then(
                                Modifier.background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.05f)
                                        )
                                    ),
                                    CircleShape
                                )
                            )
                    )
                }

                // Expanding orb
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    OverlayPrimaryContainer.copy(alpha = 0.15f),
                                    OverlayBg.copy(alpha = 0f)
                                )
                            ),
                            CircleShape
                        )
                )

                // Inner core icon
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = OverlayPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // ── Ambient bottom glow ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            OverlayPrimaryContainer.copy(alpha = 0.04f)
                        )
                    )
                )
        )
    }
}

/**
 * Custom LifecycleOwner + SavedStateRegistryOwner for Compose views outside of Activities.
 * Required because ComposeView needs these owners to function properly.
 */
private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    init {
        savedStateRegistryController.performRestore(null)
    }

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }
}
