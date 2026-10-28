package com.example.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.utils.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuraOverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var floatingToolbarView: View? = null
    private val targetPointerViews = mutableListOf<View>()

    private val _isOverlayShowing = MutableStateFlow(false)
    val isOverlayShowing = _isOverlayShowing.asStateFlow()

    private val _targetPoints = MutableStateFlow<List<Pair<Int, Int>>>(
        listOf(Pair(540, 960))
    )
    val targetPoints = _targetPoints.asStateFlow()

    var onPlayClicked: (() -> Unit)? = null
    var onPauseClicked: (() -> Unit)? = null
    var onStopClicked: (() -> Unit)? = null
    var onAddPointClicked: (() -> Unit)? = null
    var onSettingsClicked: (() -> Unit)? = null

    @SuppressLint("ClickableViewAccessibility")
    fun showOverlay() {
        if (_isOverlayShowing.value) return
        if (!PermissionHelper.hasOverlayPermission(context)) {
            return
        }

        try {
            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val toolbarParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 60
                y = 300
            }

            val toolbar = buildToolbarView(toolbarParams)
            floatingToolbarView = toolbar
            windowManager.addView(toolbar, toolbarParams)

            _isOverlayShowing.value = true
            refreshTargetPointers()
        } catch (e: Exception) {
            // Error adding window
            _isOverlayShowing.value = false
        }
    }

    fun hideOverlay() {
        try {
            floatingToolbarView?.let {
                windowManager.removeView(it)
                floatingToolbarView = null
            }
            targetPointerViews.forEach {
                windowManager.removeView(it)
            }
            targetPointerViews.clear()
            _isOverlayShowing.value = false
        } catch (e: Exception) {
            // ignore
        }
    }

    fun addTargetPoint(x: Int = 540, y: Int = 960) {
        val updated = _targetPoints.value.toMutableList()
        updated.add(Pair(x, y))
        _targetPoints.value = updated
        if (_isOverlayShowing.value) {
            refreshTargetPointers()
        }
    }

    fun removeTargetPoint(index: Int) {
        val updated = _targetPoints.value.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _targetPoints.value = updated
            if (_isOverlayShowing.value) {
                refreshTargetPointers()
            }
        }
    }

    private fun refreshTargetPointers() {
        if (!PermissionHelper.hasOverlayPermission(context)) return

        targetPointerViews.forEach {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // ignore
            }
        }
        targetPointerViews.clear()

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        _targetPoints.value.forEachIndexed { index, point ->
            val pointerParams = WindowManager.LayoutParams(
                120,
                120,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = point.first - 60
                y = point.second - 60
            }

            val pointerView = buildPointerView(index + 1, pointerParams, index)
            targetPointerViews.add(pointerView)
            try {
                windowManager.addView(pointerView, pointerParams)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun buildToolbarView(params: WindowManager.LayoutParams): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#EE0B0F19"))
            setPadding(16, 16, 16, 16)
        }

        var isCollapsed = false
        val actionRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val dragHandle = TextView(context).apply {
            text = "::: AURA :::"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 11f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 8)
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        dragHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    floatingToolbarView?.let { windowManager.updateViewLayout(it, params) }
                    true
                }
                else -> false
            }
        }

        val btnPlay = createToolbarButton("▶", "#00E676") { onPlayClicked?.invoke() }
        val btnPause = createToolbarButton("⏸", "#FFAB00") { onPauseClicked?.invoke() }
        val btnStop = createToolbarButton("⏹", "#FF5252") { onStopClicked?.invoke() }
        val btnAdd = createToolbarButton("➕", "#00E5FF") {
            addTargetPoint(540, 960)
            onAddPointClicked?.invoke()
        }
        val btnCollapse = createToolbarButton("◧", "#B388FF") {
            isCollapsed = !isCollapsed
            actionRow.visibility = if (isCollapsed) View.GONE else View.VISIBLE
        }

        actionRow.addView(btnPlay)
        actionRow.addView(btnPause)
        actionRow.addView(btnStop)
        actionRow.addView(btnAdd)

        container.addView(dragHandle)
        container.addView(actionRow)
        container.addView(btnCollapse)

        return container
    }

    private fun createToolbarButton(symbol: String, colorHex: String, onClick: () -> Unit): TextView {
        return TextView(context).apply {
            text = symbol
            textSize = 18f
            setTextColor(Color.parseColor(colorHex))
            setPadding(20, 16, 20, 16)
            setOnClickListener { onClick() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildPointerView(
        number: Int,
        params: WindowManager.LayoutParams,
        pointIndex: Int
    ): View {
        val root = FrameLayout(context)

        val circleView = TextView(context).apply {
            text = number.toString()
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#CC7C4DFF"))
            layoutParams = FrameLayout.LayoutParams(100, 100, Gravity.CENTER)
        }

        root.addView(circleView)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        root.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = initialX + (event.rawX - initialTouchX).toInt()
                    val newY = initialY + (event.rawY - initialTouchY).toInt()
                    params.x = newX
                    params.y = newY
                    windowManager.updateViewLayout(v, params)

                    // Update points state
                    val currentList = _targetPoints.value.toMutableList()
                    if (pointIndex in currentList.indices) {
                        currentList[pointIndex] = Pair(newX + 50, newY + 50)
                        _targetPoints.value = currentList
                    }
                    true
                }
                else -> false
            }
        }

        return root
    }
}
