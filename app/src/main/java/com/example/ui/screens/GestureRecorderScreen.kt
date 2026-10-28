package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.entity.RecordedGestureEntity
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraViolet
import com.example.ui.viewmodel.AuraMainViewModel

@Composable
fun GestureRecorderScreen(
    viewModel: AuraMainViewModel
) {
    val gestures by viewModel.allGestures.collectAsStateWithLifecycle()
    val isRecording by viewModel.isGestureRecording.collectAsStateWithLifecycle()

    val currentTouchPath = remember { mutableStateListOf<Offset>() }
    var gestureName by remember { mutableStateOf("") }
    var replayStatus by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gesture Recorder & Replay",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isRecording) "Recording live touch points..." else "Draw gesture on the canvas below",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRecording) AuraError else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isRecording) {
                Button(
                    onClick = {
                        viewModel.stopAndSaveGesture(gestureName)
                        currentTouchPath.clear()
                        gestureName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraError),
                    modifier = Modifier.testTag("gesture_stop_recording_button")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", color = Color.White)
                }
            } else {
                Button(
                    onClick = {
                        currentTouchPath.clear()
                        viewModel.startGestureRecording()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraCyan),
                    modifier = Modifier.testTag("gesture_start_recording_button")
                ) {
                    Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isRecording) {
            OutlinedTextField(
                value = gestureName,
                onValueChange = { gestureName = it },
                label = { Text("Gesture Name (Optional)") },
                placeholder = { Text("e.g. Diagonal Fast Swipe") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Touch Recording Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .border(
                    width = if (isRecording) 2.dp else 1.dp,
                    color = if (isRecording) AuraError else AuraCyan.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(Color(0xFF070B13), RoundedCornerShape(16.dp))
                .pointerInput(isRecording) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (isRecording) {
                                currentTouchPath.clear()
                                currentTouchPath.add(offset)
                                viewModel.addGesturePoint(offset.x, offset.y)
                            }
                        },
                        onDrag = { change, _ ->
                            if (isRecording) {
                                val pos = change.position
                                currentTouchPath.add(pos)
                                viewModel.addGesturePoint(pos.x, pos.y)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (currentTouchPath.isEmpty() && !isRecording) {
                Text(
                    text = "Press 'Record' and draw a swipe or gesture here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                if (currentTouchPath.size > 1) {
                    val path = Path().apply {
                        moveTo(currentTouchPath.first().x, currentTouchPath.first().y)
                        for (i in 1 until currentTouchPath.size) {
                            lineTo(currentTouchPath[i].x, currentTouchPath[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = if (isRecording) AuraError else AuraCyan,
                        style = Stroke(
                            width = 6f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        if (replayStatus.isNotBlank()) {
            Text(
                text = replayStatus,
                style = MaterialTheme.typography.bodySmall,
                color = AuraSuccess
            )
        }

        // Saved Gestures Section
        Text(
            text = "Saved Gestures (${gestures.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (gestures.isEmpty()) {
            Text(
                text = "No recorded gestures yet. Record a new gesture above.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gestures, key = { it.id }) { gesture ->
                    SavedGestureCard(
                        gesture = gesture,
                        onReplay = {
                            replayStatus = "Replaying ${gesture.name}..."
                            viewModel.replayGesture(gesture) { success ->
                                replayStatus = if (success) "Replayed ${gesture.name} successfully!" else "Replay cancelled or service inactive"
                            }
                        },
                        onDelete = {
                            viewModel.deleteGesture(gesture.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedGestureCard(
    gesture: RecordedGestureEntity,
    onReplay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = gesture.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${gesture.pointCount} points • ${gesture.durationMs}ms duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onReplay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Replay Gesture", tint = AuraSuccess)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Gesture", tint = AuraError)
                }
            }
        }
    }
}
