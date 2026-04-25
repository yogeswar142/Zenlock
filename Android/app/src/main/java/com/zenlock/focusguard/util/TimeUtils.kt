package com.zenlock.focusguard.util

import java.util.concurrent.TimeUnit

/**
 * Formatting utilities for time display throughout the app.
 */
object TimeUtils {

    /**
     * Format seconds into MM:SS display.
     */
    fun formatTimer(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Format seconds into a human-readable duration string.
     * e.g., "2h 30m", "45m", "30s"
     */
    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }

    /**
     * Format minutes into a human-readable duration.
     */
    fun formatMinutes(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    /**
     * Get the start of the current day in milliseconds.
     */
    fun getStartOfDay(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Get the start of the current week (Monday) in milliseconds.
     */
    fun getStartOfWeek(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        if (cal.timeInMillis > System.currentTimeMillis()) {
            cal.add(java.util.Calendar.WEEK_OF_YEAR, -1)
        }
        return cal.timeInMillis
    }

    /**
     * Get the start of the current month in milliseconds.
     */
    fun getStartOfMonth(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Get the day of week name from a timestamp.
     */
    fun getDayOfWeek(timestamp: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        return when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Mon"
            java.util.Calendar.TUESDAY -> "Tue"
            java.util.Calendar.WEDNESDAY -> "Wed"
            java.util.Calendar.THURSDAY -> "Thu"
            java.util.Calendar.FRIDAY -> "Fri"
            java.util.Calendar.SATURDAY -> "Sat"
            java.util.Calendar.SUNDAY -> "Sun"
            else -> ""
        }
    }
}
