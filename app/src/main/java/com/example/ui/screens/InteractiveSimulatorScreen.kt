package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AutomationState
import com.example.ui.components.AuraClickVisualizer
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraViolet
import com.example.ui.theme.AuraWarning
import com.example.ui.viewmodel.AuraMainViewModel
import kotlin.math.roundToInt

data class SimTargetPin(
    val id: Int,
    var offset: Offset
)

@Composable
fun InteractiveSimulatorScreen(
    viewModel: AuraMainViewModel
) {
    val automationState by viewModel.automationState.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val rippleColor by viewModel.rippleColor.collectAsStateWithLifecycle()
    val clickEffect by viewModel.clickEffect.collectAsStateWithLifecycle()

    // Simulated Floating Toolbar Position
    var toolbarOffset by remember { mutableStateOf(Offset(60f, 150f)) }

    // Simulated Target Pins (Movable)
    val targetPins = remember {
        mutableStateListOf(
            SimTargetPin(1, Offset(200f, 400f)),
            SimTargetPin(2, Offset(600f, 800f)),
            SimTargetPin(3, Offset(400f, 1200f))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B13))
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    // Add click point on double tap or long press
                }
            }
    ) {
        // Active click ripple layer
        AuraClickVisualizer(
            events = viewModel.visualClickEvents,
            rippleColorArgb = rippleColor,
            effectType = clickEffect
        )

        // Top Status HUD
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xDD131B2E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "LIVE SIMULATOR",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = AuraCyan
                    )
                    Text(
                        text = "Drag toolbar & targets to test overlay mechanics",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Clicks: ${stats.totalClicks}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AuraSuccess
                    )
                    Text(
                        text = "Time: ${stats.elapsedTimeSeconds}s",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AuraViolet
                    )
                }
            }
        }

        // Draggable Target Pins
        targetPins.forEachIndexed { index, pin ->
            Box(
                modifier = Modifier
                    .offset { IntOffset(pin.offset.x.roundToInt(), pin.offset.y.roundToInt()) }
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xDD7C4DFF))
                    .border(2.dp, AuraCyan, CircleShape)
                    .pointerInput(pin.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            pin.offset = Offset(
                                pin.offset.x + dragAmount.x,
                                pin.offset.y + dragAmount.y
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        // Draggable Floating Overlay Toolbar
        Box(
            modifier = Modifier
                .offset { IntOffset(toolbarOffset.x.roundToInt(), toolbarOffset.y.roundToInt()) }
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xF00B0F19))
                .border(1.dp, AuraCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        toolbarOffset = Offset(
                            toolbarOffset.x + dragAmount.x,
                            toolbarOffset.y + dragAmount.y
                        )
                    }
                }
                .padding(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "::: DRAG AURA :::",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = AuraCyan,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (automationState == AutomationState.RUNNING) {
                        IconButton(
                            onClick = { viewModel.pauseAutomation() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = AuraWarning)
                        }
                        IconButton(
                            onClick = { viewModel.stopAutomation() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = AuraError)
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.startAutomation() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraSuccess)
                        }
                    }

                    IconButton(
                        onClick = {
                            val newId = targetPins.size + 1
                            targetPins.add(SimTargetPin(newId, Offset(300f, 600f + (newId * 60))))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Pin", tint = AuraCyan)
                    }
                }
            }
        }
    }
}
