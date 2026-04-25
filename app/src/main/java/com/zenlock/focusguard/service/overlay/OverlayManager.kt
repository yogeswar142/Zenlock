package com.zenlock.focusguard.service.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

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
        "The secret of getting ahead is getting started.",
        "Focus on being productive instead of busy.",
        "It's not about having time, it's about making time.",
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
    fun showOverlay(reason: String = "This app is blocked during your focus session", onGoBackAction: () -> Unit = {}) {
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

/**
 * Compose UI for the full-screen blocking overlay.
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0C29),
                        Color(0xFF302B63),
                        Color(0xFF24243E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isBreathing) {
            BreatheExercise(onComplete = onGoBack)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Lock icon
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Blocked",
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "🛡️ FocusGuard Active",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reason
                Text(
                    text = reason,
                    fontSize = 16.sp,
                    color = Color(0xFFB0B0B0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Motivational quote card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x33FFFFFF)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"$quote\"",
                            fontSize = 14.sp,
                            color = Color(0xFFE0E0E0),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Go Back button
                Button(
                    onClick = { isBreathing = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B68EE)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Refocus & Go Back",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
        Text(
            text = "Take a moment...",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 60.dp)
        )
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp)
        ) {
            // Expanding circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(scale)
                    .background(Color(0xFF7B68EE).copy(alpha = 0.4f), shape = CircleShape)
            )
            
            // Inner core
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFF7B68EE),
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(80.dp))
        
        Text(
            text = when (phase) {
                1 -> "Breathe In..."
                2 -> "Hold..."
                3 -> "Breathe Out..."
                else -> ""
            },
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alpha(alpha)
        )
    }
}
    // Entire content of BlockingOverlayContent was replaced above.

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
