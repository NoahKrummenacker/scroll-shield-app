package com.example.scroll_shield

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.Calendar

class PrefsManager(context: Context) {
    companion object {
        const val PREFS_NAME = "blocker_prefs"
    }
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var blockReels: Boolean
        get() = prefs.getBoolean("block_reels", true)
        set(v) = prefs.edit().putBoolean("block_reels", v).apply()
    var blockReelsFeed: Boolean
        get() = prefs.getBoolean("block_reels_feed", false)
        set(v) = prefs.edit().putBoolean("block_reels_feed", v).apply()
    var allowDmReels: Boolean
        get() = prefs.getBoolean("allow_dm_reels", false)
        set(v) = prefs.edit().putBoolean("allow_dm_reels", v).apply()
    var blockShorts: Boolean
        get() = prefs.getBoolean("block_shorts", true)
        set(v) = prefs.edit().putBoolean("block_shorts", v).apply()
    var blockShortsFeed: Boolean
        get() = prefs.getBoolean("block_shorts_feed", false)
        set(v) = prefs.edit().putBoolean("block_shorts_feed", v).apply()

    var scheduleEnabled: Boolean
        get() = prefs.getBoolean("schedule_enabled", false)
        set(v) = prefs.edit().putBoolean("schedule_enabled", v).apply()
    var scheduleStartHour: Int
        get() = prefs.getInt("schedule_start_hour", 9)
        set(v) = prefs.edit().putInt("schedule_start_hour", v).apply()
    var scheduleStartMin: Int
        get() = prefs.getInt("schedule_start_min", 0)
        set(v) = prefs.edit().putInt("schedule_start_min", v).apply()
    var scheduleEndHour: Int
        get() = prefs.getInt("schedule_end_hour", 22)
        set(v) = prefs.edit().putInt("schedule_end_hour", v).apply()
    var scheduleEndMin: Int
        get() = prefs.getInt("schedule_end_min", 0)
        set(v) = prefs.edit().putInt("schedule_end_min", v).apply()

    var dailyLimitEnabled: Boolean
        get() = prefs.getBoolean("daily_limit_enabled", false)
        set(v) = prefs.edit().putBoolean("daily_limit_enabled", v).apply()
    var dailyLimitMinutes: Int
        get() = prefs.getInt("daily_limit_minutes", 30)
        set(v) = prefs.edit().putInt("daily_limit_minutes", v).apply()
    var dailyUsageSeconds: Long
        get() = prefs.getLong("daily_usage_seconds", 0L)
        set(v) = prefs.edit().putLong("daily_usage_seconds", v).apply()
    private var lastUsageDate: String
        get() = prefs.getString("last_usage_date", "") ?: ""
        set(v) = prefs.edit().putString("last_usage_date", v).apply()

    fun resetDailyUsageIfNeeded() {
        val today = java.time.LocalDate.now().toString()
        if (lastUsageDate != today) { dailyUsageSeconds = 0L; lastUsageDate = today }
    }

    fun addUsageSeconds(seconds: Long) {
        if (seconds > 0) dailyUsageSeconds = dailyUsageSeconds + seconds
    }

    fun shouldBlockByDailyLimit(): Boolean =
        dailyLimitEnabled && dailyUsageSeconds >= dailyLimitMinutes * 60L

    fun resetDailyUsage() {
        dailyUsageSeconds = 0L
        lastUsageDate = java.time.LocalDate.now().toString()
    }

    fun dailyLimitToMap(): Map<String, Any> = mapOf(
        "enabled"      to dailyLimitEnabled,
        "limitMinutes" to dailyLimitMinutes,
        "usageSeconds" to dailyUsageSeconds
    )

    fun isWithinSchedule(): Boolean {
        if (!scheduleEnabled) return true
        val cal = Calendar.getInstance()
        val now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val start = scheduleStartHour * 60 + scheduleStartMin
        val end   = scheduleEndHour   * 60 + scheduleEndMin
        return if (start <= end) now in start..end else now >= start || now <= end
    }

    fun toMap(): Map<String, Boolean> = mapOf(
        "blockReels" to blockReels, "blockReelsFeed" to blockReelsFeed,
        "blockShorts" to blockShorts, "blockShortsFeed" to blockShortsFeed,
        "allowDmReels" to allowDmReels)

    fun scheduleToMap(): Map<String, Any> = mapOf(
        "enabled" to scheduleEnabled, "startHour" to scheduleStartHour,
        "startMin" to scheduleStartMin, "endHour" to scheduleEndHour, "endMin" to scheduleEndMin)

    val pinEnabled: Boolean get() = prefs.getBoolean("pin_enabled", false)

    fun setPin(pin: String) {
        prefs.edit().putString("pin_hash", sha256(pin)).putBoolean("pin_enabled", true).apply()
    }
    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString("pin_hash", null) ?: return false
        return sha256(pin) == stored
    }
    fun removePin() {
        prefs.edit().remove("pin_hash").putBoolean("pin_enabled", false).apply()
    }
    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
