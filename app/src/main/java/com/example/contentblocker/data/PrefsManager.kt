package com.example.contentblocker.data

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.Calendar

class PrefsManager(context: Context) {

    companion object {
        const val PREFS_NAME = "blocker_prefs"

        const val KEY_BLOCK_REELS       = "block_reels"
        const val KEY_BLOCK_REELS_FEED  = "block_reels_feed"
        const val KEY_BLOCK_SHORTS      = "block_shorts"
        const val KEY_BLOCK_SHORTS_FEED = "block_shorts_feed"

        const val KEY_SCHEDULE_ENABLED    = "schedule_enabled"
        const val KEY_SCHEDULE_START_HOUR = "schedule_start_hour"
        const val KEY_SCHEDULE_START_MIN  = "schedule_start_min"
        const val KEY_SCHEDULE_END_HOUR   = "schedule_end_hour"
        const val KEY_SCHEDULE_END_MIN    = "schedule_end_min"

        const val KEY_PIN_HASH    = "pin_hash"
        const val KEY_PIN_ENABLED = "pin_enabled"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Toggles

    var blockReels: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_REELS, true)
        set(v) = prefs.edit().putBoolean(KEY_BLOCK_REELS, v).apply()

    var blockReelsFeed: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_REELS_FEED, false)
        set(v) = prefs.edit().putBoolean(KEY_BLOCK_REELS_FEED, v).apply()

    var blockShorts: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_SHORTS, true)
        set(v) = prefs.edit().putBoolean(KEY_BLOCK_SHORTS, v).apply()

    var blockShortsFeed: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_SHORTS_FEED, false)
        set(v) = prefs.edit().putBoolean(KEY_BLOCK_SHORTS_FEED, v).apply()

    // Schedule

    var scheduleEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCHEDULE_ENABLED, false)
        set(v) = prefs.edit().putBoolean(KEY_SCHEDULE_ENABLED, v).apply()

    var scheduleStartHour: Int
        get() = prefs.getInt(KEY_SCHEDULE_START_HOUR, 9)
        set(v) = prefs.edit().putInt(KEY_SCHEDULE_START_HOUR, v).apply()

    var scheduleStartMin: Int
        get() = prefs.getInt(KEY_SCHEDULE_START_MIN, 0)
        set(v) = prefs.edit().putInt(KEY_SCHEDULE_START_MIN, v).apply()

    var scheduleEndHour: Int
        get() = prefs.getInt(KEY_SCHEDULE_END_HOUR, 22)
        set(v) = prefs.edit().putInt(KEY_SCHEDULE_END_HOUR, v).apply()

    var scheduleEndMin: Int
        get() = prefs.getInt(KEY_SCHEDULE_END_MIN, 0)
        set(v) = prefs.edit().putInt(KEY_SCHEDULE_END_MIN, v).apply()

    fun isWithinSchedule(): Boolean {
        if (!scheduleEnabled) return true
        val cal   = Calendar.getInstance()
        val now   = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val start = scheduleStartHour * 60 + scheduleStartMin
        val end   = scheduleEndHour   * 60 + scheduleEndMin
        return if (start <= end) now in start..end else now >= start || now <= end
    }

    // PIN

    val pinEnabled: Boolean
        get() = prefs.getBoolean(KEY_PIN_ENABLED, false)

    fun setPin(pin: String) {
        prefs.edit()
            .putString(KEY_PIN_HASH, sha256(pin))
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return sha256(pin) == stored
    }

    fun removePin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
