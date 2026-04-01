package com.example.contentblocker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.contentblocker.data.PrefsManager

class BlockerAccessibilityService : AccessibilityService() {

    companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        const val YOUTUBE_PACKAGE   = "com.google.android.youtube"

        // View IDs (peuvent changer avec les MAJ des apps)
        const val INSTAGRAM_REELS_TAB      = "com.instagram.android:id/clips_tab"
        const val YOUTUBE_SHORTS_CONTAINER = "com.google.android.youtube:id/shorts_container"

        // IDs exclusifs au viewer Reels plein écran (confirmés via dump UI en direct)
        val INSTAGRAM_REEL_PLAYER_IDS = listOf(
            "com.instagram.android:id/clips_viewer_action_bar",   // barre d'action du viewer Reels
            "com.instagram.android:id/clips_viewer_container"      // conteneur principal du viewer
        )

        // IDs possibles pour l'onglet Accueil Instagram (varie selon la version)
        val INSTAGRAM_HOME_TAB_IDS = listOf(
            "com.instagram.android:id/feed_tab",
            "com.instagram.android:id/tab_icon_home",
            "com.instagram.android:id/home_tab",
            "com.instagram.android:id/tab_icon_0"
        )

        // IDs possibles pour l'onglet Accueil YouTube (varie selon la version)
        val YOUTUBE_HOME_TAB_IDS = listOf(
            "com.google.android.youtube:id/home_button",
            "com.google.android.youtube:id/pivot_bar_item_home",
            "com.google.android.youtube:id/pivot_bar_button_home",
            "com.google.android.youtube:id/bottom_bar_item_0"
        )

        // IDs du lecteur Shorts YouTube (plein écran ou inline)
        val YOUTUBE_SHORTS_PLAYER_IDS = listOf(
            "com.google.android.youtube:id/shorts_container",
            "com.google.android.youtube:id/reel_watch_fragment_root",
            "com.google.android.youtube:id/shorts_player_view_pager"
        )
    }

    private lateinit var prefs: PrefsManager
    private var lastActionTime = 0L

    // ─── Cycle de vie ─────────────────────────────────────────────────────────

    override fun onServiceConnected() {
        prefs = PrefsManager(this)
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            packageNames = arrayOf(INSTAGRAM_PACKAGE, YOUTUBE_PACKAGE)
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags        = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                           AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 150L
        }
    }

    override fun onInterrupt() {}

    // ─── Point d'entrée ───────────────────────────────────────────────────────

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        if (!prefs.isWithinSchedule()) return

        val now = System.currentTimeMillis()
        if (now - lastActionTime < 500L) return

        val packageName = event.packageName?.toString() ?: return
        val root = rootInActiveWindow ?: return

        try {
            when (packageName) {
                INSTAGRAM_PACKAGE -> {
                    val isOnReelsTab = detectInstagramReels(root)
                    val detected =
                        (prefs.blockReels     && isOnReelsTab) ||
                        (prefs.blockReelsFeed && (isOnReelsTab || detectInstagramReelPlayer(root)))
                    if (detected) {
                        lastActionTime = now
                        if (!navigateToInstagramHome(root)) performGlobalAction(GLOBAL_ACTION_BACK)
                    }
                }
                YOUTUBE_PACKAGE -> {
                    val detected =
                        (prefs.blockShorts     && detectYouTubeShorts(root)) ||
                        (prefs.blockShortsFeed && detectYouTubeShortsAnywhere(root))
                    if (detected) {
                        lastActionTime = now
                        if (!navigateToYouTubeHome(root)) performGlobalAction(GLOBAL_ACTION_BACK)
                    }
                }
            }
        } finally {
            root.recycle()
        }
    }

    // ─── Détection ────────────────────────────────────────────────────────────

    private fun detectInstagramReels(root: AccessibilityNodeInfo): Boolean {
        val tabNodes = root.findAccessibilityNodeInfosByViewId(INSTAGRAM_REELS_TAB)
        val tabSelected = tabNodes.any { it.isSelected }
        tabNodes.forEach { it.recycle() }
        return tabSelected
    }

    /** Détecte le lecteur Reels ouvert depuis le fil ou l'explore (hors onglet Reels). */
    private fun detectInstagramReelPlayer(root: AccessibilityNodeInfo): Boolean =
        anyViewFound(root, INSTAGRAM_REEL_PLAYER_IDS)

    private fun navigateToInstagramHome(root: AccessibilityNodeInfo): Boolean =
        clickFirstFound(root, INSTAGRAM_HOME_TAB_IDS)

    // ─── YouTube ──────────────────────────────────────────────────────────────

    /** Détecte l'onglet Shorts sélectionné ou le lecteur Shorts plein écran. */
    private fun detectYouTubeShorts(root: AccessibilityNodeInfo): Boolean {
        if (anyViewFound(root, listOf(YOUTUBE_SHORTS_CONTAINER))) return true
        val tabNodes = root.findAccessibilityNodeInfosByText("Shorts")
        val selected = tabNodes.any { it.isSelected }
        tabNodes.forEach { it.recycle() }
        return selected
    }

    /** Détection élargie : inclut d'autres IDs du lecteur Shorts (inline, pager…). */
    private fun detectYouTubeShortsAnywhere(root: AccessibilityNodeInfo): Boolean =
        detectYouTubeShorts(root) || anyViewFound(root, YOUTUBE_SHORTS_PLAYER_IDS)

    /** Clique sur l'onglet Accueil de YouTube. Retourne true si le clic a réussi. */
    private fun navigateToYouTubeHome(root: AccessibilityNodeInfo): Boolean =
        clickFirstFound(root, YOUTUBE_HOME_TAB_IDS)

    // ─── Utilitaires ──────────────────────────────────────────────────────────

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
            val node  = nodes.firstOrNull()
            nodes.forEach { it.recycle() }
            if (node != null && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        }
        return false
    }
}