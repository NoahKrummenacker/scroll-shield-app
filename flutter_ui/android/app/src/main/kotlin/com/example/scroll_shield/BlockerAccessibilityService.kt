package com.example.scroll_shield

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BlockerAccessibilityService : AccessibilityService() {
    companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        const val YOUTUBE_PACKAGE   = "com.google.android.youtube"
        const val INSTAGRAM_REELS_TAB = "com.instagram.android:id/clips_tab"
        val INSTAGRAM_REEL_PLAYER_IDS = listOf(
            "com.instagram.android:id/clips_viewer_action_bar",
            "com.instagram.android:id/clips_viewer_container")
        val INSTAGRAM_HOME_TAB_IDS = listOf(
            "com.instagram.android:id/feed_tab", "com.instagram.android:id/tab_icon_home",
            "com.instagram.android:id/home_tab", "com.instagram.android:id/tab_icon_0")
        val YOUTUBE_HOME_TAB_IDS = listOf(
            "com.google.android.youtube:id/home_button",
            "com.google.android.youtube:id/pivot_bar_item_home",
            "com.google.android.youtube:id/pivot_bar_button_home",
            "com.google.android.youtube:id/bottom_bar_item_0")
        val YOUTUBE_SHORTS_PLAYER_IDS = listOf(
            "com.google.android.youtube:id/shorts_container",
            "com.google.android.youtube:id/reel_watch_fragment_root",
            "com.google.android.youtube:id/shorts_player_view_pager")
    }

    private lateinit var prefs: PrefsManager
    private var lastActionTime = 0L
    private var sessionStart   = 0L

    override fun onServiceConnected() {
        prefs = PrefsManager(this)
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            packageNames = arrayOf(INSTAGRAM_PACKAGE, YOUTUBE_PACKAGE)
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 150L
        }
    }

    override fun onInterrupt() {}

    private fun trackUsage(isOnContent: Boolean, now: Long) {
        prefs.resetDailyUsageIfNeeded()
        if (isOnContent) {
            if (sessionStart == 0L) sessionStart = now
            if (now - sessionStart >= 10_000L) {
                prefs.addUsageSeconds((now - sessionStart) / 1000L)
                sessionStart = now
            }
        } else {
            if (sessionStart != 0L) {
                prefs.addUsageSeconds((now - sessionStart) / 1000L)
                sessionStart = 0L
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val now = System.currentTimeMillis()
        if (now - lastActionTime < 500L) return
        val packageName = event.packageName?.toString() ?: return
        val root = rootInActiveWindow ?: return
        try {
            when (packageName) {
                INSTAGRAM_PACKAGE -> {
                    val isOnReelsTab   = detectInstagramReels(root)
                    val isOnReelPlayer = detectInstagramReelPlayer(root)
                    val isOnContent    = isOnReelsTab || isOnReelPlayer
                    trackUsage(isOnContent, now)
                    if (!prefs.isWithinSchedule()) return
                    val shouldBlock = if (prefs.dailyLimitEnabled) {
                        prefs.shouldBlockByDailyLimit() && isOnContent
                    } else {
                        (prefs.blockReels && isOnReelsTab) ||
                        (prefs.blockReelsFeed && isOnContent)
                    }
                    if (shouldBlock) {
                        lastActionTime = now
                        if (!navigateToInstagramHome(root)) performGlobalAction(GLOBAL_ACTION_BACK)
                    }
                }
                YOUTUBE_PACKAGE -> {
                    val isOnContent = detectYouTubeShortsAnywhere(root)
                    trackUsage(isOnContent, now)
                    if (!prefs.isWithinSchedule()) return
                    val shouldBlock = if (prefs.dailyLimitEnabled) {
                        prefs.shouldBlockByDailyLimit() && isOnContent
                    } else {
                        (prefs.blockShorts && detectYouTubeShorts(root)) ||
                        (prefs.blockShortsFeed && isOnContent)
                    }
                    if (shouldBlock) {
                        lastActionTime = now
                        if (!navigateToYouTubeHome(root)) performGlobalAction(GLOBAL_ACTION_BACK)
                    }
                }
            }
        } finally { root.recycle() }
    }

    private fun detectInstagramReels(root: AccessibilityNodeInfo): Boolean {
        val nodes = root.findAccessibilityNodeInfosByViewId(INSTAGRAM_REELS_TAB)
        val sel = nodes.any { it.isSelected }
        nodes.forEach { it.recycle() }
        return sel
    }
    private fun detectInstagramReelPlayer(root: AccessibilityNodeInfo) = anyViewFound(root, INSTAGRAM_REEL_PLAYER_IDS)
    private fun navigateToInstagramHome(root: AccessibilityNodeInfo) = clickFirstFound(root, INSTAGRAM_HOME_TAB_IDS)
    private fun detectYouTubeShorts(root: AccessibilityNodeInfo): Boolean {
        if (anyViewFound(root, listOf("com.google.android.youtube:id/shorts_container"))) return true
        val nodes = root.findAccessibilityNodeInfosByText("Shorts")
        val sel = nodes.any { it.isSelected }
        nodes.forEach { it.recycle() }
        return sel
    }
    private fun detectYouTubeShortsAnywhere(root: AccessibilityNodeInfo) =
        detectYouTubeShorts(root) || anyViewFound(root, YOUTUBE_SHORTS_PLAYER_IDS)
    private fun navigateToYouTubeHome(root: AccessibilityNodeInfo) = clickFirstFound(root, YOUTUBE_HOME_TAB_IDS)

    private fun anyViewFound(root: AccessibilityNodeInfo, ids: List<String>): Boolean {
        for (id in ids) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            val found = nodes.any { it.isVisibleToUser }
            nodes.forEach { it.recycle() }
            if (found) return true
        }
        return false
    }
    private fun clickFirstFound(root: AccessibilityNodeInfo, ids: List<String>): Boolean {
        for (id in ids) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            val node = nodes.firstOrNull()
            nodes.forEach { it.recycle() }
            if (node != null && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        }
        return false
    }
}
