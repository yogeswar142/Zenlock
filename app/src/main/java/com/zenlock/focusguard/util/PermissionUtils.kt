package com.zenlock.focusguard.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import android.text.TextUtils

/**
 * Utility class for checking and requesting system permissions.
 *
 * FocusGuard requires three special permissions:
 * 1. Usage Stats - to detect which app is in the foreground
 * 2. Overlay (SYSTEM_ALERT_WINDOW) - to display blocking screens
 * 3. Accessibility Service - to read screen content and detect app switches
 */
object PermissionUtils {

    /**
     * Check if Usage Stats permission is granted.
     * This permission allows querying which app is currently in the foreground.
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Check if Overlay (draw over other apps) permission is granted.
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Check if our Accessibility Service is currently enabled.
     * Reads the system accessibility settings to find our service.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val serviceName = "${context.packageName}/.service.accessibility.FocusGuardAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(serviceName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * Check if notification permission is granted (Android 13+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Not needed below Android 13
        }
    }

    /**
     * Check if ALL required permissions are granted.
     */
    fun hasAllPermissions(context: Context): Boolean {
        return hasUsageStatsPermission(context) &&
                hasOverlayPermission(context) &&
                isAccessibilityServiceEnabled(context)
    }

    /**
     * Open Usage Stats settings page.
     */
    fun openUsageStatsSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    /**
     * Open Overlay settings page.
     */
    fun openOverlaySettings(context: Context) {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    /**
     * Open Accessibility settings page.
     */
    fun openAccessibilitySettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }
}
