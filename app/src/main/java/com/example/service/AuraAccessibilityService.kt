package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.model.ConditionMatchMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.regex.Pattern

class AuraAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: AuraAccessibilityService? = null

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected = _isServiceConnected.asStateFlow()

        fun isConnected(): Boolean = instance != null

        fun getInstance(): AuraAccessibilityService? = instance
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceConnected.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Monitored for window transitions and dynamic node hierarchy updates
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceConnected.value = false
    }

    fun dispatchClick(x: Float, y: Float, durationMs: Long = 50L, onComplete: ((Boolean) -> Unit)? = null) {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs.coerceAtLeast(10L))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onComplete?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                onComplete?.invoke(false)
            }
        }, null)
    }

    fun dispatchSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 300L,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs.coerceAtLeast(50L))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onComplete?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                onComplete?.invoke(false)
            }
        }, null)
    }

    fun dispatchMultiPointGesture(points: List<Pair<Float, Float>>, durationMs: Long = 400L, onComplete: ((Boolean) -> Unit)? = null) {
        if (points.isEmpty()) {
            onComplete?.invoke(false)
            return
        }
        val path = Path().apply {
            moveTo(points[0].first, points[0].second)
            for (i in 1 until points.size) {
                lineTo(points[i].first, points[i].second)
            }
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs.coerceAtLeast(50L))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onComplete?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                onComplete?.invoke(false)
            }
        }, null)
    }

    fun performBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)

    fun performHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    fun performRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)

    /**
     * Inspects active window accessibility nodes to find text based on condition matching.
     */
    fun findTextOnScreen(targetText: String, matchMode: ConditionMatchMode): Boolean {
        val root = rootInActiveWindow ?: return false
        val found = searchNodeHierarchyForText(root, targetText, matchMode)
        root.recycle()
        return found
    }

    private fun searchNodeHierarchyForText(
        node: AccessibilityNodeInfo,
        target: String,
        matchMode: ConditionMatchMode
    ): Boolean {
        val text = node.text?.toString() ?: node.contentDescription?.toString()
        if (!text.isNullOrEmpty()) {
            val matches = when (matchMode) {
                ConditionMatchMode.CONTAINS -> text.contains(target, ignoreCase = true)
                ConditionMatchMode.EXACT -> text.equals(target, ignoreCase = true)
                ConditionMatchMode.REGEX -> try {
                    Pattern.compile(target, Pattern.CASE_INSENSITIVE).matcher(text).find()
                } catch (e: Exception) {
                    false
                }
            }
            if (matches) return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = searchNodeHierarchyForText(child, target, matchMode)
            child.recycle()
            if (found) return true
        }
        return false
    }
}
