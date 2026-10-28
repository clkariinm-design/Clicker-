package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.utils.VisualClickEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

private class ActiveRipple(
    val x: Float,
    val y: Float,
    val color: Color
) {
    val radius = Animatable(10f)
    val alpha = Animatable(0.9f)
}

@Composable
fun AuraClickVisualizer(
    events: SharedFlow<VisualClickEvent>,
    rippleColorArgb: Int,
    effectType: String = "CYBER_RIPPLE",
    modifier: Modifier = Modifier
) {
    val activeRipples = remember { mutableStateListOf<ActiveRipple>() }
    val baseColor = Color(rippleColorArgb)

    LaunchedEffect(events) {
        events.collect { event ->
            val ripple = ActiveRipple(event.x, event.y, baseColor)
            activeRipples.add(ripple)

            launch {
                launch {
                    ripple.radius.animateTo(
                        targetValue = 90f,
                        animationSpec = tween(400, easing = FastOutLinearInEasing)
                    )
                }
                launch {
                    ripple.alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(400, easing = FastOutLinearInEasing)
                    )
                }
                // Cleanup finished ripple
                activeRipples.remove(ripple)
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        activeRipples.forEach { ripple ->
            val currentRadius = ripple.radius.value
            val currentAlpha = ripple.alpha.value

            when (effectType) {
                "PULSE_RETICLE", "CROSSHAIR" -> {
                    // Outer circle
                    drawCircle(
                        color = ripple.color.copy(alpha = currentAlpha),
                        radius = currentRadius,
                        center = Offset(ripple.x, ripple.y),
                        style = Stroke(width = 3f)
                    )
                    // Inner dot
                    drawCircle(
                        color = ripple.color.copy(alpha = currentAlpha),
                        radius = 6f,
                        center = Offset(ripple.x, ripple.y)
                    )
                    // Crosshair lines
                    drawLine(
                        color = ripple.color.copy(alpha = currentAlpha),
                        start = Offset(ripple.x - currentRadius - 10f, ripple.y),
                        end = Offset(ripple.x + currentRadius + 10f, ripple.y),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = ripple.color.copy(alpha = currentAlpha),
                        start = Offset(ripple.x, ripple.y - currentRadius - 10f),
                        end = Offset(ripple.x, ripple.y + currentRadius + 10f),
                        strokeWidth = 2f
                    )
                }
                else -> {
                    // CYBER_RIPPLE: Concentric glowing energy waves
                    drawCircle(
                        color = ripple.color.copy(alpha = currentAlpha),
                        radius = currentRadius,
                        center = Offset(ripple.x, ripple.y),
                        style = Stroke(width = 4f)
                    )
                    drawCircle(
                        color = ripple.color.copy(alpha = currentAlpha * 0.7f),
                        radius = currentRadius * 0.6f,
                        center = Offset(ripple.x, ripple.y),
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        color = ripple.color.copy(alpha = currentAlpha),
                        radius = 8f,
                        center = Offset(ripple.x, ripple.y)
                    )
                }
            }
        }
    }
}
