package com.zenlock.focusguard.ui.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zenlock.focusguard.FocusGuardApp
import com.zenlock.focusguard.data.db.entity.BlockedAppEntity
import com.zenlock.focusguard.data.db.entity.FocusSessionEntity
import com.zenlock.focusguard.data.db.entity.KeywordEntity
import com.zenlock.focusguard.domain.model.AppInfo
import com.zenlock.focusguard.service.focus.FocusSessionForegroundService
import com.zenlock.focusguard.util.PermissionUtils
import com.zenlock.focusguard.util.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Main ViewModel for the FocusGuard application.
 * Manages state for the home screen, focus timer, blocked apps, and settings.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp

    // ===================== PERMISSIONS STATE =====================

    private val _hasUsageStatsPermission = MutableStateFlow(false)
    val hasUsageStatsPermission: StateFlow<Boolean> = _hasUsageStatsPermission

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission

    private val _hasAccessibilityPermission = MutableStateFlow(false)
    val hasAccessibilityPermission: StateFlow<Boolean> = _hasAccessibilityPermission

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission

    // ===================== FOCUS SESSION STATE =====================

    private val _focusDurationMinutes = MutableStateFlow(25)
    val focusDurationMinutes: StateFlow<Int> = _focusDurationMinutes

    private val _breakDurationMinutes = MutableStateFlow(5)
    val breakDurationMinutes: StateFlow<Int> = _breakDurationMinutes

    private val _isFocusActive = MutableStateFlow(false)
    val isFocusActive: StateFlow<Boolean> = _isFocusActive

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds

    private val _totalSeconds = MutableStateFlow(0L)
    val totalSeconds: StateFlow<Long> = _totalSeconds

    // ===================== BLOCKED APPS STATE =====================

    val blockedApps: StateFlow<List<BlockedAppEntity>> = app.blockedAppRepository
        .getAllBlockedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps

    private val _appSearchQuery = MutableStateFlow("")
    val appSearchQuery: StateFlow<String> = _appSearchQuery

    val filteredInstalledApps: StateFlow<List<AppInfo>> = combine(
        _installedApps, _appSearchQuery
    ) { apps, query ->
        if (query.isBlank()) apps
        else apps.filter { it.appName.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===================== KEYWORDS STATE =====================

    val keywords: StateFlow<List<KeywordEntity>> = app.keywordRepository
        .getAllKeywords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===================== SETTINGS STATE =====================

    private val _youtubeFilterEnabled = MutableStateFlow(true)
    val youtubeFilterEnabled: StateFlow<Boolean> = _youtubeFilterEnabled

    private val _shortsBlockEnabled = MutableStateFlow(true)
    val shortsBlockEnabled: StateFlow<Boolean> = _shortsBlockEnabled

    private val _blockUnknownContent = MutableStateFlow(true)
    val blockUnknownContent: StateFlow<Boolean> = _blockUnknownContent

    private val _strictMode = MutableStateFlow(false)
    val strictMode: StateFlow<Boolean> = _strictMode

    private val _motivationalQuotes = MutableStateFlow(true)
    val motivationalQuotes: StateFlow<Boolean> = _motivationalQuotes

    private val _dndSyncEnabled = MutableStateFlow(false)
    val dndSyncEnabled: StateFlow<Boolean> = _dndSyncEnabled

    // ===================== GAMIFICATION STATE =====================

    private val _userXP = MutableStateFlow(0)
    val userXP: StateFlow<Int> = _userXP

    private val _userStreak = MutableStateFlow(0)
    val userStreak: StateFlow<Int> = _userStreak

    // ===================== STATISTICS STATE =====================

    private val _todayFocusTime = MutableStateFlow(0L)
    val todayFocusTime: StateFlow<Long> = _todayFocusTime

    private val _weeklyFocusTime = MutableStateFlow(0L)
    val weeklyFocusTime: StateFlow<Long> = _weeklyFocusTime

    private val _monthlyFocusTime = MutableStateFlow(0L)
    val monthlyFocusTime: StateFlow<Long> = _monthlyFocusTime

    private val _todaySessionCount = MutableStateFlow(0)
    val todaySessionCount: StateFlow<Int> = _todaySessionCount

    private val _weeklySessionCount = MutableStateFlow(0)
    val weeklySessionCount: StateFlow<Int> = _weeklySessionCount

    private var baseTodayBlockedAttempts = 0

    private val _todayBlockedAttempts = MutableStateFlow(0)
    val todayBlockedAttempts: StateFlow<Int> = _todayBlockedAttempts

    private val _weeklyChartData = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val weeklyChartData: StateFlow<List<Pair<String, Float>>> = _weeklyChartData

    private val _recentSessions = MutableStateFlow<List<FocusSessionEntity>>(emptyList())
    val recentSessions: StateFlow<List<FocusSessionEntity>> = _recentSessions

    val allSessions: StateFlow<List<FocusSessionEntity>> = app.focusSessionRepository
        .getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadPreferences()
        seedDefaultKeywords()
        checkAndRecoverSession()
        startTimerPolling()
    }

    private fun checkAndRecoverSession() {
        viewModelScope.launch {
            val activeSession = app.focusSessionRepository.getActiveSession()
            if (activeSession != null && !FocusSessionForegroundService.isRunning) {
                FocusSessionForegroundService.triggerRecovery(getApplication())
            }
        }
    }

    // ===================== INITIALIZATION =====================

    private fun loadPreferences() {
        viewModelScope.launch {
            app.userPreferences.focusDurationMinutes.collect { _focusDurationMinutes.value = it }
        }
        viewModelScope.launch {
            app.userPreferences.breakDurationMinutes.collect { _breakDurationMinutes.value = it }
        }
        viewModelScope.launch {
            app.userPreferences.youtubeFilteringEnabled.collect { _youtubeFilterEnabled.value = it }
        }
        viewModelScope.launch {
            app.userPreferences.shortsBlockingEnabled.collect { _shortsBlockEnabled.value = it }
        }
        viewModelScope.launch {
            app.userPreferences.defaultBlockUnknown.collect { _blockUnknownContent.value = it }
        }
        viewModelScope.launch {
            app.userPreferences.strictMode.collect { _strictMode.value = it }
        }
        viewModelScope.launch {
            app.userPreferences.motivationalQuotesEnabled.collect { _motivationalQuotes.value = it }
        }
        viewModelScope.launch {
            app.userPreferences.isFocusActive.collect { _isFocusActive.value = it }
        }
        viewModelScope.launch {
            app.userPreferences.dndSyncEnabled.collect { _dndSyncEnabled.value = it }
        }
        viewModelScope.launch {
            app.userPreferences.userXP.collect { _userXP.value = it }
        }
        viewModelScope.launch {
            app.userPreferences.userStreak.collect { _userStreak.value = it }
        }
    }

    private fun seedDefaultKeywords() {
        viewModelScope.launch {
            app.keywordRepository.seedDefaultKeywords()
        }
    }

    /**
     * Polls the foreground service for timer state updates every second.
     * This is necessary because the service runs in a separate context.
     */
    private fun startTimerPolling() {
        viewModelScope.launch {
            while (true) {
                _isFocusActive.value = FocusSessionForegroundService.isRunning
                _isPaused.value = FocusSessionForegroundService.isPaused
                _remainingSeconds.value = FocusSessionForegroundService.remainingSeconds
                _totalSeconds.value = FocusSessionForegroundService.totalSeconds
                
                // Update live blocked attempts
                _todayBlockedAttempts.value = baseTodayBlockedAttempts + 
                    com.zenlock.focusguard.service.accessibility.FocusSessionService.blockedAttemptCount.get()
                    
                delay(500) // Poll every 500ms for smooth UI updates
            }
        }
    }

    // ===================== PERMISSION CHECKS =====================

    fun refreshPermissions() {
        val ctx = getApplication<Application>()
        _hasUsageStatsPermission.value = PermissionUtils.hasUsageStatsPermission(ctx)
        _hasOverlayPermission.value = PermissionUtils.hasOverlayPermission(ctx)
        _hasAccessibilityPermission.value = PermissionUtils.isAccessibilityServiceEnabled(ctx)
        _hasNotificationPermission.value = PermissionUtils.hasNotificationPermission(ctx)
    }

    // ===================== FOCUS SESSION ACTIONS =====================

    fun startFocusSession() {
        FocusSessionForegroundService.startSession(
            getApplication(),
            _focusDurationMinutes.value
        )
    }

    fun stopFocusSession() {
        FocusSessionForegroundService.stopSession(getApplication())
    }

    fun pauseFocusSession() {
        FocusSessionForegroundService.pauseSession(getApplication())
    }

    fun resumeFocusSession() {
        FocusSessionForegroundService.resumeSession(getApplication())
    }

    fun setFocusDuration(minutes: Int) {
        viewModelScope.launch {
            app.userPreferences.setFocusDurationMinutes(minutes)
        }
    }

    fun setBreakDuration(minutes: Int) {
        viewModelScope.launch {
            app.userPreferences.setBreakDurationMinutes(minutes)
        }
    }

    // ===================== BLOCKED APPS ACTIONS =====================

    fun loadInstalledApps() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            val blockedPackages = app.blockedAppRepository.getActiveBlockedAppsList()
                .map { it.packageName }.toSet()

            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { appInfo ->
                    // Filter out system apps and our own app
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
                            appInfo.packageName != "com.zenlock.focusguard" &&
                            pm.getLaunchIntentForPackage(appInfo.packageName) != null
                }
                .map { appInfo ->
                    AppInfo(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString(),
                        isBlocked = blockedPackages.contains(appInfo.packageName)
                    )
                }
                .sortedBy { it.appName }

            _installedApps.value = apps
        }
    }

    fun toggleAppBlocked(appInfo: AppInfo) {
        viewModelScope.launch {
            if (appInfo.isBlocked) {
                app.blockedAppRepository.removeByPackageName(appInfo.packageName)
            } else {
                app.blockedAppRepository.addBlockedApp(
                    BlockedAppEntity(
                        packageName = appInfo.packageName,
                        appName = appInfo.appName
                    )
                )
            }
            // Refresh installed apps list to update toggle state
            loadInstalledApps()
        }
    }

    fun removeBlockedApp(blockedApp: BlockedAppEntity) {
        viewModelScope.launch {
            app.blockedAppRepository.removeBlockedApp(blockedApp)
            loadInstalledApps()
        }
    }

    fun setAppSearchQuery(query: String) {
        _appSearchQuery.value = query
    }

    // ===================== KEYWORD ACTIONS =====================

    fun addKeyword(keyword: String, type: String) {
        viewModelScope.launch {
            app.keywordRepository.addKeyword(
                KeywordEntity(keyword = keyword.lowercase().trim(), type = type)
            )
        }
    }

    fun deleteKeyword(keyword: KeywordEntity) {
        viewModelScope.launch {
            app.keywordRepository.deleteKeyword(keyword)
        }
    }

    fun toggleKeyword(keyword: KeywordEntity) {
        viewModelScope.launch {
            app.keywordRepository.updateKeyword(keyword.copy(isActive = !keyword.isActive))
        }
    }

    // ===================== SETTINGS ACTIONS =====================

    fun setYoutubeFilterEnabled(enabled: Boolean) {
        viewModelScope.launch { app.userPreferences.setYoutubeFilteringEnabled(enabled) }
    }

    fun setShortsBlockEnabled(enabled: Boolean) {
        viewModelScope.launch { app.userPreferences.setShortsBlockingEnabled(enabled) }
    }

    fun setBlockUnknownContent(block: Boolean) {
        viewModelScope.launch { app.userPreferences.setDefaultBlockUnknown(block) }
    }

    fun setStrictMode(enabled: Boolean) {
        viewModelScope.launch { app.userPreferences.setStrictMode(enabled) }
    }

    fun setMotivationalQuotes(enabled: Boolean) {
        viewModelScope.launch { app.userPreferences.setMotivationalQuotesEnabled(enabled) }
    }

    fun setDndSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { app.userPreferences.setDndSyncEnabled(enabled) }
    }

    // ===================== STATISTICS =====================

    fun loadStatistics() {
        viewModelScope.launch {
            val startOfDay = TimeUtils.getStartOfDay()
            val startOfWeek = TimeUtils.getStartOfWeek()
            val startOfMonth = TimeUtils.getStartOfMonth()

            _todayFocusTime.value = app.focusSessionRepository.getTotalFocusTimeSince(startOfDay)
            _weeklyFocusTime.value = app.focusSessionRepository.getTotalFocusTimeSince(startOfWeek)
            _monthlyFocusTime.value = app.focusSessionRepository.getTotalFocusTimeSince(startOfMonth)

            _todaySessionCount.value = app.focusSessionRepository.getCompletedSessionCountSince(startOfDay)
            _weeklySessionCount.value = app.focusSessionRepository.getCompletedSessionCountSince(startOfWeek)

            baseTodayBlockedAttempts = app.focusSessionRepository.getTotalBlockedAttemptsSince(startOfDay)
            _todayBlockedAttempts.value = baseTodayBlockedAttempts + com.zenlock.focusguard.service.accessibility.FocusSessionService.blockedAttemptCount.get()

            // Build weekly chart data
            loadWeeklyChartData()
        }
    }

    private suspend fun loadWeeklyChartData() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (cal.timeInMillis > System.currentTimeMillis()) {
            cal.add(Calendar.WEEK_OF_YEAR, -1)
        }

        val chartData = mutableListOf<Pair<String, Float>>()
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        for (i in 0..6) {
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = cal.timeInMillis

            val focusTime = app.focusSessionRepository.getTotalFocusTimeSince(dayStart)
            // Only count time within this specific day
            val sessions = app.focusSessionRepository.getCompletedSessionsSince(dayStart)
            val dayFocusMinutes = sessions
                .filter { it.startTime < dayEnd }
                .sumOf { it.actualDurationSeconds } / 60f

            chartData.add(dayNames[i] to dayFocusMinutes)
        }

        _weeklyChartData.value = chartData
    }
}
