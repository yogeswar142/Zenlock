package com.zenlock.focusguard.service.focus

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.zenlock.focusguard.FocusGuardApp
import com.zenlock.focusguard.R
import com.zenlock.focusguard.data.db.entity.FocusSessionEntity
import com.zenlock.focusguard.service.accessibility.FocusSessionService
import com.zenlock.focusguard.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * Foreground service that manages the focus session timer.
 *
 * Responsibilities:
 * - Run a countdown timer for the focus session
 * - Show a persistent notification with remaining time
 * - Save session data to the database when complete
 * - Coordinate with the AccessibilityService for blocking
 *
 * The service runs as a foreground service to prevent being killed by the system.
 */
class FocusSessionForegroundService : Service() {

    companion object {
        private const val TAG = "FocusService"

        const val ACTION_START = "com.zenlock.focusguard.action.START_FOCUS"
        const val ACTION_STOP = "com.zenlock.focusguard.action.STOP_FOCUS"
        const val ACTION_PAUSE = "com.zenlock.focusguard.action.PAUSE_FOCUS"
        const val ACTION_RESUME = "com.zenlock.focusguard.action.RESUME_FOCUS"
        const val ACTION_RECOVER = "com.zenlock.focusguard.action.RECOVER_FOCUS"
        const val EXTRA_DURATION_MINUTES = "duration_minutes"

        @Volatile
        var isRunning = false
            private set

        @Volatile
        var isPaused = false
            private set

        @Volatile
        var remainingSeconds = 0L
            private set

        @Volatile
        var totalSeconds = 0L
            private set

        @Volatile
        var currentSessionId: Long = -1L
            private set

        /**
         * Helper to start a focus session.
         */
        fun startSession(context: Context, durationMinutes: Int) {
            val intent = Intent(context, FocusSessionForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION_MINUTES, durationMinutes)
            }
            context.startForegroundService(intent)
        }

        fun stopSession(context: Context) {
            val intent = Intent(context, FocusSessionForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun pauseSession(context: Context) {
            val intent = Intent(context, FocusSessionForegroundService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeSession(context: Context) {
            val intent = Intent(context, FocusSessionForegroundService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun triggerRecovery(context: Context) {
            val intent = Intent(context, FocusSessionForegroundService::class.java).apply {
                action = ACTION_RECOVER
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null
    private var startTime = 0L
    private var durationMinutes = 25

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == null) {
            // Service was likely recreated by the system
            if (!isRunning) {
                recoverSession()
            }
            return START_STICKY
        }

        when (intent.action) {
            ACTION_START -> {
                durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                startFocusSession()
            }
            ACTION_STOP -> stopFocusSession(completed = false)
            ACTION_PAUSE -> pauseFocusTimer()
            ACTION_RESUME -> resumeFocusTimer()
            ACTION_RECOVER -> {
                if (!isRunning) {
                    recoverSession()
                }
            }
        }
        return START_STICKY
    }

    private fun recoverSession() {
        serviceScope.launch {
            try {
                val app = FocusGuardApp.getInstance()
                val activeSession = app.focusSessionRepository.getActiveSession()
                
                if (activeSession != null) {
                    val expectedEndTime = activeSession.startTime + (activeSession.plannedDurationMinutes * 60 * 1000L)
                    val now = System.currentTimeMillis()
                    
                    if (expectedEndTime > now) {
                        // Session is still active
                        durationMinutes = activeSession.plannedDurationMinutes
                        startTime = activeSession.startTime
                        totalSeconds = durationMinutes * 60L
                        remainingSeconds = (expectedEndTime - now) / 1000
                        currentSessionId = activeSession.id
                        
                        isRunning = true
                        isPaused = false
                        
                        startForeground(FocusGuardApp.FOCUS_NOTIFICATION_ID, createNotification())
                        startTimer()
                        Log.d(TAG, "Recovered active focus session: ID=$currentSessionId, remaining=${remainingSeconds}s")
                    } else {
                        // Session should have ended
                        app.focusSessionRepository.updateSession(
                            activeSession.copy(
                                endTime = expectedEndTime,
                                actualDurationSeconds = activeSession.plannedDurationMinutes * 60L,
                                isCompleted = true
                            )
                        )
                        app.userPreferences.setFocusActive(false)
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                } else {
                    app.userPreferences.setFocusActive(false)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error recovering session", e)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun startFocusSession() {
        if (isRunning) return

        totalSeconds = durationMinutes * 60L
        remainingSeconds = totalSeconds
        startTime = System.currentTimeMillis()
        isRunning = true
        isPaused = false

        // Reset blocked attempt counter
        FocusSessionService.blockedAttemptCount.set(0)

        // Create session in database and handle DND
        serviceScope.launch {
            try {
                val app = FocusGuardApp.getInstance()
                app.userPreferences.setFocusActive(true)

                val session = FocusSessionEntity(
                    startTime = startTime,
                    plannedDurationMinutes = durationMinutes,
                    sessionType = "focus"
                )
                currentSessionId = app.focusSessionRepository.insertSession(session)
                Log.d(TAG, "Focus session started: ID=$currentSessionId, duration=${durationMinutes}min")

                // Enable DND if setting is checked and permission granted
                val dndEnabled = app.userPreferences.dndSyncEnabled.first()
                if (dndEnabled) {
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    if (manager.isNotificationPolicyAccessGranted) {
                        manager.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALARMS)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating session", e)
            }
        }

        // Start foreground with notification
        startForeground(FocusGuardApp.FOCUS_NOTIFICATION_ID, createNotification())

        // Start countdown timer
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (remainingSeconds > 0 && isRunning) {
                if (!isPaused) {
                    delay(1000)
                    remainingSeconds--

                    // Update notification every 10 seconds to reduce overhead
                    if (remainingSeconds % 10 == 0L || remainingSeconds <= 10) {
                        withContext(Dispatchers.Main) {
                            try {
                                val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                                manager.notify(FocusGuardApp.FOCUS_NOTIFICATION_ID, createNotification())
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating notification", e)
                            }
                        }
                    }
                } else {
                    delay(500) // Check pause state periodically
                }
            }

            if (remainingSeconds <= 0) {
                // Session completed naturally
                stopFocusSession(completed = true)
            }
        }
    }

    private fun pauseFocusTimer() {
        isPaused = true
    }

    private fun resumeFocusTimer() {
        isPaused = false
    }

    private fun stopFocusSession(completed: Boolean) {
        timerJob?.cancel()

        val endTime = System.currentTimeMillis()
        val actualDuration = (endTime - startTime) / 1000

        // Update session in database
        serviceScope.launch {
            try {
                val app = FocusGuardApp.getInstance()
                app.userPreferences.setFocusActive(false)

                if (currentSessionId > 0) {
                    val session = app.focusSessionRepository.getSessionById(currentSessionId)
                    session?.let {
                        app.focusSessionRepository.updateSession(
                            it.copy(
                                endTime = endTime,
                                actualDurationSeconds = actualDuration,
                                isCompleted = completed,
                                blockedAttempts = FocusSessionService.blockedAttemptCount.get()
                            )
                        )
                    }
                }

                // Handle Gamification XP & Streak
                if (completed) {
                    val xpEarned = durationMinutes * 10
                    app.userPreferences.addXP(xpEarned)
                    app.userPreferences.incrementStreak()
                } else {
                    app.userPreferences.resetStreak()
                }

                // Restore DND
                val dndEnabled = app.userPreferences.dndSyncEnabled.first()
                if (dndEnabled) {
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    if (manager.isNotificationPolicyAccessGranted) {
                        manager.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                }

                Log.d(TAG, "Focus session ended: completed=$completed, duration=${actualDuration}s, blocked=${FocusSessionService.blockedAttemptCount.get()}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating session", e)
            }
        }

        isRunning = false
        isPaused = false
        remainingSeconds = 0
        currentSessionId = -1L

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FocusSessionForegroundService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FocusGuardApp.FOCUS_CHANNEL_ID)
            .setContentTitle("🛡️ Focus Session Active")
            .setContentText("Time remaining: $timeText")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .setProgress(totalSeconds.toInt(), (totalSeconds - remainingSeconds).toInt(), false)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
        isRunning = false
        Log.d(TAG, "Focus service destroyed")
    }
}
