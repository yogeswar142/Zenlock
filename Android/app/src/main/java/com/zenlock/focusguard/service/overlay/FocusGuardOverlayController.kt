package com.zenlock.focusguard.service.overlay

import android.content.Context
import android.util.Log

/**
 * Global Thread-Safe Overlay Controller.
 * Ensures a single source of truth for overlay state, preventing window leaks or multiple duplicate overlays.
 */
class FocusGuardOverlayController private constructor(context: Context) {

    companion object {
        private const val TAG = "Zenlock[OverlayCtrl]"

        @Volatile
        private var instance: FocusGuardOverlayController? = null

        fun getInstance(context: Context): FocusGuardOverlayController {
            return instance ?: synchronized(this) {
                instance ?: FocusGuardOverlayController(context.applicationContext).also { instance = it }
            }
        }
    }

    private val overlayManager = OverlayManager(context.applicationContext)

    @Synchronized
    fun showBlockingOverlay(reason: String, onGoBack: () -> Unit) {
        if (!overlayManager.isOverlayShowing()) {
            Log.d(TAG, "Requesting overlay display: $reason")
            overlayManager.showOverlay(reason, onGoBack)
        } else {
            Log.d(TAG, "Overlay already showing, maintaining overlay.")
        }
    }

    @Synchronized
    fun hideBlockingOverlay() {
        if (overlayManager.isOverlayShowing()) {
            Log.d(TAG, "Hiding overlay")
            overlayManager.hideOverlay()
        }
    }

    fun isOverlayShowing(): Boolean = overlayManager.isOverlayShowing()
}
