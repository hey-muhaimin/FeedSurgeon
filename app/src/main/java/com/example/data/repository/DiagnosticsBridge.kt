package com.example.data.repository

import com.example.data.model.InspectedNode
import com.example.data.model.InterventionToastState
import com.example.data.model.LiveTreeSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DiagnosticsBridge {
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val _liveSnapshot = MutableStateFlow<LiveTreeSnapshot?>(null)
    val liveSnapshot: StateFlow<LiveTreeSnapshot?> = _liveSnapshot.asStateFlow()

    private val _recentToast = MutableStateFlow<InterventionToastState?>(null)
    val recentToast: StateFlow<InterventionToastState?> = _recentToast.asStateFlow()

    fun setServiceConnected(connected: Boolean) {
        _isServiceConnected.value = connected
    }

    fun publishSnapshot(snapshot: LiveTreeSnapshot) {
        _liveSnapshot.value = snapshot
    }

    fun publishToast(toast: InterventionToastState?) {
        _recentToast.value = toast
    }

    fun dismissToast() {
        _recentToast.value = null
    }

    fun countSelectorMatches(query: String, snapshot: LiveTreeSnapshot? = _liveSnapshot.value): Int {
        val trimmed = query.trim()
        if (trimmed.isEmpty() || snapshot == null) return 0
        val normalized = if (trimmed.startsWith("id/")) trimmed.removePrefix("id/") else trimmed
        return snapshot.nodes.count { node ->
            val id = node.viewId ?: ""
            val desc = node.contentDescription ?: ""
            id.equals(trimmed, ignoreCase = true) ||
                id.endsWith(":id/$normalized", ignoreCase = true) ||
                id.contains(normalized, ignoreCase = true) ||
                (trimmed.startsWith("desc=") && desc.equals(trimmed.removePrefix("desc=").trim('"'), ignoreCase = true))
        }
    }

    fun createDiagnosticSampleSnapshot(packageName: String = "com.google.android.youtube"): LiveTreeSnapshot {
        val isYoutube = packageName == "com.google.android.youtube"
        val nodes = if (isYoutube) {
            listOf(
                InspectedNode(0, "FrameLayout", null, null, isSelected = false),
                InspectedNode(1, "ViewPager", null, null, isSelected = false),
                InspectedNode(
                    2,
                    "RecyclerView",
                    "com.google.android.youtube:id/reel_recycler",
                    null,
                    isSelected = false,
                    isMatched = true
                ),
                InspectedNode(
                    2,
                    "FrameLayout",
                    "com.google.android.youtube:id/reel_watch_fragment_root",
                    null,
                    isSelected = false
                ),
                InspectedNode(1, "TabBar", null, null, isSelected = false),
                InspectedNode(2, "TabView", null, "Home", isSelected = false),
                InspectedNode(2, "TabView", null, "Shorts", isSelected = true, isMatched = true)
            )
        } else {
            listOf(
                InspectedNode(0, "FrameLayout", null, null, isSelected = false),
                InspectedNode(
                    1,
                    "ViewPager2",
                    "com.instagram.android:id/clips_viewer_view_pager",
                    null,
                    isSelected = false,
                    isMatched = true
                ),
                InspectedNode(
                    2,
                    "FrameLayout",
                    "com.instagram.android:id/clips_video_container",
                    null,
                    isSelected = false,
                    isMatched = true
                ),
                InspectedNode(1, "BottomNavBar", null, null, isSelected = false),
                InspectedNode(2, "TabButton", null, "Home", isSelected = false),
                InspectedNode(2, "TabButton", null, "Reels", isSelected = true, isMatched = true)
            )
        }
        return LiveTreeSnapshot(
            packageName = packageName,
            appDisplayName = if (isYoutube) "YouTube" else "Instagram",
            capturedAtMs = System.currentTimeMillis(),
            isDiagnosticSample = true,
            nodes = nodes
        )
    }
}
