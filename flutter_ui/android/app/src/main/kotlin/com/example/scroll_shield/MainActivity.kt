package com.example.scroll_shield

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val CHANNEL = "com.example.contentblocker/blocker"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            val prefs = PrefsManager(this)
            when (call.method) {

                "isServiceEnabled" -> {
                    val enabled = Settings.Secure.getString(
                        contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
                    val cn = ComponentName(this, BlockerAccessibilityService::class.java).flattenToString()
                    result.success(enabled.split(":").any { it.equals(cn, ignoreCase = true) })
                }

                "getPrefs" -> result.success(prefs.toMap())

                "setPref" -> {
                    val key   = call.argument<String>("key")
                    val value = call.argument<Boolean>("value")
                    if (key == null || value == null) {
                        result.error("INVALID_ARGS", "key and value required", null)
                        return@setMethodCallHandler
                    }
                    when (key) {
                        "blockReels"      -> prefs.blockReels      = value
                        "blockReelsFeed"  -> prefs.blockReelsFeed  = value
                        "blockShorts"     -> prefs.blockShorts     = value
                        "blockShortsFeed" -> prefs.blockShortsFeed = value
                        "allowDmReels"    -> prefs.allowDmReels    = value
                    }
                    result.success(null)
                }

                "getSchedule" -> result.success(prefs.scheduleToMap())

                "setSchedule" -> {
                    prefs.scheduleEnabled   = call.argument<Boolean>("enabled")  ?: prefs.scheduleEnabled
                    prefs.scheduleStartHour = call.argument<Int>("startHour")     ?: prefs.scheduleStartHour
                    prefs.scheduleStartMin  = call.argument<Int>("startMin")      ?: prefs.scheduleStartMin
                    prefs.scheduleEndHour   = call.argument<Int>("endHour")       ?: prefs.scheduleEndHour
                    prefs.scheduleEndMin    = call.argument<Int>("endMin")        ?: prefs.scheduleEndMin
                    result.success(null)
                }

                "isPinEnabled" -> result.success(prefs.pinEnabled)

                "setPin" -> {
                    val pin = call.argument<String>("pin")
                    if (pin == null) { result.error("INVALID_ARGS", "pin required", null); return@setMethodCallHandler }
                    prefs.setPin(pin)
                    result.success(null)
                }

                "verifyPin" -> {
                    val pin = call.argument<String>("pin")
                    if (pin == null) { result.error("INVALID_ARGS", "pin required", null); return@setMethodCallHandler }
                    result.success(prefs.verifyPin(pin))
                }

                "removePin" -> { prefs.removePin(); result.success(null) }

                "openAccessibilitySettings" -> {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    result.success(null)
                }

                "getDailyLimit"   -> result.success(prefs.dailyLimitToMap())

                "setDailyLimit"   -> {
                    call.argument<Boolean>("enabled")?.let     { prefs.dailyLimitEnabled  = it }
                    call.argument<Int>("limitMinutes")?.let    { prefs.dailyLimitMinutes  = it }
                    result.success(null)
                }

                "getDailyUsage"   -> result.success(prefs.dailyUsageSeconds)

                "resetDailyUsage" -> { prefs.resetDailyUsage(); result.success(null) }

                else -> result.notImplemented()
            }
        }
    }
}
