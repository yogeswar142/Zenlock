package com.zenlock.focusguard.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.zenlock.focusguard.FocusGuardApp
import com.zenlock.focusguard.domain.model.ContentVerdict
import com.zenlock.focusguard.domain.rules.YouTubeContentClassifier
import com.zenlock.focusguard.service.overlay.OverlayManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * AccessibilityService that monitors the foreground app and YouTube content.
 *
 * Key responsibilities:
 * 1. Detect when a blocked app is opened → show blocking overlay
 * 2. Monitor YouTube for Shorts content → block if detected
 * 3. Read YouTube video titles → classify using keyword rules → block if distracting
 *
 * IMPORTANT: This service runs in its own process lifecycle. It uses coroutines
 * for database operations but the accessibility callbacks themselves are on the main thread.
 *
 * The service avoids excessive processing by:
 * - Debouncing rapid accessibility events (100ms minimum between processing)
 * - Caching keyword lists (refreshed every 30 seconds)
 * - Only processing events from YouTube and blocked apps
 */
class FocusGuardAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "FocusGuardA11y"
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val DEBOUNCE_MS = 150L

        // Singleton reference for checking service status from the app
        @Volatile
        var instance: FocusGuardAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null
    }

    private lateinit var overlayManager: OverlayManager
    private lateinit var classifier: YouTubeContentClassifier
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var lastProcessedTime = 0L
    private var currentForegroundPackage: String? = null
    private var isOverlayShownForCurrentApp = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        overlayManager = OverlayManager(this)

        val app = FocusGuardApp.getInstance()
        classifier = YouTubeContentClassifier(app.keywordRepository)

        Log.d(TAG, "Accessibility service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        overlayManager.hideOverlay()
        serviceScope.cancel()
        Log.d(TAG, "Accessibility service destroyed")
    }

    /**
     * Called when an accessibility event is received.
     * This is the main entry point for all monitoring logic.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Debounce rapid events to reduce processing overhead
        val now = System.currentTimeMillis()
        if (now - lastProcessedTime < DEBOUNCE_MS) return
        lastProcessedTime = now

        val packageName = event.packageName?.toString() ?: return

        // Skip our own package to avoid recursion
        if (packageName == "com.zenlock.focusguard") return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowChanged(packageName, event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Only process YouTube content changes
                if (packageName == YOUTUBE_PACKAGE) {
                    handleYouTubeContentChanged(event)
                }
            }
        }
    }

    /**
     * Handle window state changes - detect foreground app switches.
     */
    private fun handleWindowChanged(packageName: String, event: AccessibilityEvent) {
        // If we switched to a different app, reset overlay state
        if (packageName != currentForegroundPackage) {
            currentForegroundPackage = packageName
            isOverlayShownForCurrentApp = false

            // Hide overlay when user navigates away from blocked app
            if (overlayManager.isOverlayShowing()) {
                overlayManager.hideOverlay()
            }
        }

        serviceScope.launch {
            try {
                val app = FocusGuardApp.getInstance()
                val isFocusActive = app.userPreferences.isFocusActive.first()

                if (!isFocusActive) return@launch

                // Check if this app is in the blocked list
                if (packageName != YOUTUBE_PACKAGE) {
                    val isBlocked = app.blockedAppRepository.isAppBlocked(packageName)
                    if (isBlocked && !isOverlayShownForCurrentApp) {
                        withContext(Dispatchers.Main) {
                            overlayManager.showOverlay("This app is blocked during your focus session") {
                                // Send to home screen for full app blocks
                                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(homeIntent)
                            }
                            isOverlayShownForCurrentApp = true
                        }
                        // Increment blocked attempt counter for the active session
                        incrementBlockedAttempts()
                    }
                }

                // YouTube-specific: check for Shorts on initial load
                if (packageName == YOUTUBE_PACKAGE) {
                    handleYouTubeContentChanged(event)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing window change", e)
            }
        }
    }

    /**
     * Handle YouTube content changes - detect Shorts and classify videos.
     *
     * Detection strategy:
     * - Shorts: Look for "Shorts" text in navigation elements, or reel_channel_name view IDs
     * - Video titles: Extract text from video title views
     * - Channel names: Extract text from channel name views
     */
    private fun handleYouTubeContentChanged(event: AccessibilityEvent) {
        serviceScope.launch {
            try {
                val app = FocusGuardApp.getInstance()
                val isFocusActive = app.userPreferences.isFocusActive.first()
                if (!isFocusActive) return@launch

                val youtubeFilterEnabled = app.userPreferences.youtubeFilteringEnabled.first()
                if (!youtubeFilterEnabled) return@launch

                val rootNode = rootInActiveWindow ?: return@launch

                try {
                    // Check for Shorts
                    val shortsBlockEnabled = app.userPreferences.shortsBlockingEnabled.first()
                    val isShortsPlaying = detectShorts(rootNode)
                    
                    if (shortsBlockEnabled && isShortsPlaying) {
                        val result = classifier.classify(null, null, isShorts = true)
                        if (result.verdict == ContentVerdict.BLOCK) {
                            withContext(Dispatchers.Main) {
                                if (!overlayManager.isOverlayShowing()) {
                                    overlayManager.showOverlay("YouTube Shorts blocked during focus session") {
                                        serviceScope.launch(Dispatchers.Main) {
                                            delay(250) // Wait for overlay to close completely
                                            performGlobalAction(GLOBAL_ACTION_BACK)
                                        }
                                    }
                                    incrementBlockedAttempts()
                                }
                            }
                            return@launch
                        }
                    }

                    // IMPORTANT: Only process keyword blocking if a regular video is actively playing!
                    // This prevents blocking the user while they are just browsing the home feed.
                    if (!detectVideoPlaying(rootNode) && !isShortsPlaying) {
                        return@launch
                    }

                    // Extract video information for keyword classification
                    var title = extractVideoTitle(rootNode)
                    var channelName = extractChannelName(rootNode)

                    if (title == null && channelName == null) {
                        // Fallback: extract all visible text to catch any keywords
                        title = extractAllText(rootNode)
                    }

                    if (!title.isNullOrBlank() || !channelName.isNullOrBlank()) {
                        val result = classifier.classify(title, channelName, isShorts = false)

                        when (result.verdict) {
                            ContentVerdict.BLOCK -> {
                                withContext(Dispatchers.Main) {
                                    if (!overlayManager.isOverlayShowing()) {
                                        overlayManager.showOverlay(
                                            "Video blocked: ${result.reason}"
                                        ) {
                                            serviceScope.launch(Dispatchers.Main) {
                                                delay(250)
                                                performGlobalAction(GLOBAL_ACTION_BACK)
                                            }
                                        }
                                        incrementBlockedAttempts()
                                    }
                                }
                            }
                            ContentVerdict.UNKNOWN -> {
                                // Check user's default setting for unknown content
                                val blockUnknown = app.userPreferences.defaultBlockUnknown.first()
                                if (blockUnknown) {
                                    withContext(Dispatchers.Main) {
                                        if (!overlayManager.isOverlayShowing()) {
                                            overlayManager.showOverlay(
                                                "Non-educational content blocked during focus session"
                                            ) {
                                                serviceScope.launch(Dispatchers.Main) {
                                                    delay(250)
                                                    performGlobalAction(GLOBAL_ACTION_BACK)
                                                }
                                            }
                                            incrementBlockedAttempts()
                                        }
                                    }
                                }
                            }
                            ContentVerdict.ALLOW -> {
                                // Educational content - hide overlay if showing
                                withContext(Dispatchers.Main) {
                                    if (overlayManager.isOverlayShowing()) {
                                        overlayManager.hideOverlay()
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    rootNode.recycle()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing YouTube content", e)
            }
        }
    }

    /**
     * Detect YouTube Shorts by examining the accessibility node tree.
     *
     * Heuristics used:
     * 1. Look for the Shorts player container (only present when watching)
     * 2. Look for "Shorts" text in tab/navigation elements where isSelected = true
     */
    private fun detectShorts(rootNode: AccessibilityNodeInfo): Boolean {
        try {
            // Check for reel player (This means a short is actively playing)
            val reelPlayerNodes = rootNode.findAccessibilityNodeInfosByViewId(
                "$YOUTUBE_PACKAGE:id/reel_player_page_container"
            )
            if (reelPlayerNodes.isNotEmpty()) {
                reelPlayerNodes.forEach { it.recycle() }
                return true
            }

            val reelPlayerViewNodes = rootNode.findAccessibilityNodeInfosByViewId(
                "$YOUTUBE_PACKAGE:id/reel_player_view"
            )
            if (reelPlayerViewNodes.isNotEmpty()) {
                reelPlayerViewNodes.forEach { it.recycle() }
                return true
            }

            // Fallback: Look for "Shorts" text in the UI
            val shortsTextNodes = rootNode.findAccessibilityNodeInfosByText("Shorts")
            for (node in shortsTextNodes) {
                val text = node.text?.toString() ?: ""
                // Check if this is the Shorts tab being selected
                if (text.equals("Shorts", ignoreCase = true) && node.isSelected) {
                    node.recycle()
                    shortsTextNodes.forEach { it.recycle() }
                    return true
                }
                node.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting Shorts", e)
        }

        return false
    }

    /**
     * Detect if a standard YouTube video is actively playing (user is on the watch page).
     */
    private fun detectVideoPlaying(rootNode: AccessibilityNodeInfo): Boolean {
        // We only want to detect the ACTUAL watch page, NOT inline playback on the home feed.
        // watch_panel and watch_player are typically only used on the full watch page.
        val playerIds = listOf(
            "$YOUTUBE_PACKAGE:id/watch_panel",
            "$YOUTUBE_PACKAGE:id/watch_player"
        )

        for (id in playerIds) {
            try {
                val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
                if (nodes.isNotEmpty()) {
                    nodes.forEach { it.recycle() }
                    return true
                }
            } catch (e: Exception) {
                // Ignore and try next
            }
        }
        return false
    }

    /**
     * Extract the video title from YouTube's accessibility tree.
     * YouTube uses various view IDs for the title depending on the screen.
     */
    private fun extractVideoTitle(rootNode: AccessibilityNodeInfo): String? {
        val titleIds = listOf(
            "$YOUTUBE_PACKAGE:id/watch_title_text",        // Watch page
            "$YOUTUBE_PACKAGE:id/title",                    // General title
            "$YOUTUBE_PACKAGE:id/video_title",              // Video list
            "$YOUTUBE_PACKAGE:id/compact_media_item_headline" // Compact view
        )

        for (titleId in titleIds) {
            try {
                val nodes = rootNode.findAccessibilityNodeInfosByViewId(titleId)
                for (node in nodes) {
                    val text = node.text?.toString()
                    node.recycle()
                    if (!text.isNullOrBlank()) {
                        nodes.forEach { it.recycle() }
                        return text
                    }
                }
            } catch (e: Exception) {
                // Continue to next ID
            }
        }

        return null
    }

    /**
     * Extract the channel name from YouTube's accessibility tree.
     */
    private fun extractChannelName(rootNode: AccessibilityNodeInfo): String? {
        val channelIds = listOf(
            "$YOUTUBE_PACKAGE:id/channel_name",
            "$YOUTUBE_PACKAGE:id/owner_text",
            "$YOUTUBE_PACKAGE:id/subtitle"
        )

        for (channelId in channelIds) {
            try {
                val nodes = rootNode.findAccessibilityNodeInfosByViewId(channelId)
                for (node in nodes) {
                    val text = node.text?.toString()
                    node.recycle()
                    if (!text.isNullOrBlank()) {
                        nodes.forEach { it.recycle() }
                        return text
                    }
                }
            } catch (e: Exception) {
                // Continue to next ID
            }
        }

        return null
    }

    /**
     * Extract all text from the node tree as a fallback.
     */
    private fun extractAllText(node: AccessibilityNodeInfo): String {
        val sb = java.lang.StringBuilder()
        fun traverse(n: AccessibilityNodeInfo) {
            if (n.text != null) sb.append(n.text).append(" ")
            if (n.contentDescription != null) sb.append(n.contentDescription).append(" ")
            for (i in 0 until n.childCount) {
                val child = n.getChild(i)
                if (child != null) {
                    traverse(child)
                    child.recycle()
                }
            }
        }
        traverse(node)
        return sb.toString().trim()
    }

    /**
     * Increment the blocked attempts counter for the current focus session.
     */
    private suspend fun incrementBlockedAttempts() {
        // This is tracked by the FocusSessionService through a shared counter
        FocusSessionService.blockedAttemptCount.incrementAndGet()
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
    }
}

/**
 * Companion object for focus session service reference.
 * Allows the accessibility service to communicate blocked attempt counts.
 */
object FocusSessionService {
    val blockedAttemptCount = java.util.concurrent.atomic.AtomicInteger(0)
}
