package com.zenlock.focusguard.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Boot receiver to handle device restarts.
 * Currently just logs the event. In a full implementation,
 * this would restart the accessibility service if it was active.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("FocusGuard", "Device booted - FocusGuard ready")
        }
    }
}
