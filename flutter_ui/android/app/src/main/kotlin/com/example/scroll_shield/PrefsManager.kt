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
        "blockShorts" to blockShorts, "blockShortsFeed" to blockShortsFeed)

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
