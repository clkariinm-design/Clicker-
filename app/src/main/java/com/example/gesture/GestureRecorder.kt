package com.example.gesture

import com.example.data.model.RecordedGesturePoint
import com.example.service.AuraAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class GestureRecorder {

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val recordedPoints = mutableListOf<RecordedGesturePoint>()
    private var startTimeMs: Long = 0L

    fun startRecording() {
        recordedPoints.clear()
        startTimeMs = System.currentTimeMillis()
        _isRecording.value = true
    }

    fun addPoint(x: Float, y: Float) {
        if (!_isRecording.value) return
        val elapsed = System.currentTimeMillis() - startTimeMs
        recordedPoints.add(RecordedGesturePoint(x, y, elapsed))
    }

    fun stopRecording(): List<RecordedGesturePoint> {
        _isRecording.value = false
        return recordedPoints.toList()
    }

    fun serializePoints(points: List<RecordedGesturePoint>): String {
        val array = JSONArray()
        points.forEach { pt ->
            val obj = JSONObject().apply {
                put("x", pt.x.toDouble())
                put("y", pt.y.toDouble())
                put("t", pt.timestampMs)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializePoints(jsonString: String): List<RecordedGesturePoint> {
        val result = mutableListOf<RecordedGesturePoint>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    RecordedGesturePoint(
                        x = obj.getDouble("x").toFloat(),
                        y = obj.getDouble("y").toFloat(),
                        timestampMs = obj.getLong("t")
                    )
                )
            }
        } catch (e: Exception) {
            // handle parse error
        }
        return result
    }

    fun replayGesture(
        points: List<RecordedGesturePoint>,
        accessibilityService: AuraAccessibilityService?,
        onComplete: (Boolean) -> Unit
    ) {
        if (points.isEmpty()) {
            onComplete(false)
            return
        }

        val totalDuration = if (points.size > 1) {
            (points.last().timestampMs - points.first().timestampMs).coerceIn(50L, 5000L)
        } else {
            100L
        }

        val coordinatePairs = points.map { Pair(it.x, it.y) }
        accessibilityService?.dispatchMultiPointGesture(coordinatePairs, totalDuration, onComplete)
            ?: onComplete(false)
    }
}
