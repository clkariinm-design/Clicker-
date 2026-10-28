package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutomationState
import com.example.data.model.AutomationStats
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraViolet
import com.example.ui.theme.AuraWarning

@Composable
fun StatusCard(
    state: AutomationState,
    stats: AutomationStats,
    profileName: String,
    isOverlayActive: Boolean,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onToggleOverlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val statusColor = when (state) {
        AutomationState.RUNNING -> AuraSuccess
        AutomationState.PAUSED -> AuraWarning
        AutomationState.IDLE -> AuraCyan
        AutomationState.ERROR -> AuraError
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(statusColor.copy(alpha = 0.6f), AuraViolet.copy(alpha = 0.3f))),
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Status + Profile Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .then(if (state == AutomationState.RUNNING) Modifier.scale(pulseScale) else Modifier)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (state) {
                            AutomationState.RUNNING -> "AUTOMATION RUNNING"
                            AutomationState.PAUSED -> "AUTOMATION PAUSED"
                            AutomationState.IDLE -> "SYSTEM READY"
                            AutomationState.ERROR -> "SYSTEM ERROR"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = statusColor
                    )
                }

                Text(
                    text = profileName.ifBlank { "No Profile" },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Metrics row: Total Clicks, Completed Cycles, Elapsed Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Total Clicks",
                    value = stats.totalClicks.toString(),
                    color = AuraCyan
                )
                MetricItem(
                    label = "Cycles",
                    value = stats.completedCycles.toString(),
                    color = AuraViolet
                )
                MetricItem(
                    label = "Elapsed",
                    value = "${stats.elapsedTimeSeconds}s",
                    color = AuraSuccess
                )
            }

            if (stats.currentStepName.isNotBlank() && state == AutomationState.RUNNING) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Current Step: ${stats.currentStepName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (state) {
                    AutomationState.IDLE, AutomationState.ERROR -> {
                        Button(
                            onClick = onStartClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_start_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AuraSuccess)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                    AutomationState.RUNNING -> {
                        FilledTonalButton(
                            onClick = onPauseClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_pause_button")
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pause")
                        }
                        Button(
                            onClick = onStopClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_stop_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AuraError)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop", color = Color.White)
                        }
                    }
                    AutomationState.PAUSED -> {
                        Button(
                            onClick = onResumeClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_resume_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AuraSuccess)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume", color = Color.Black)
                        }
                        Button(
                            onClick = onStopClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_stop_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AuraError)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop", color = Color.White)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onToggleOverlayClick,
                    modifier = Modifier.testTag("status_overlay_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (isOverlayActive) AuraCyan else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOverlayActive) "Overlay On" else "Overlay",
                        color = if (isOverlayActive) AuraCyan else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
