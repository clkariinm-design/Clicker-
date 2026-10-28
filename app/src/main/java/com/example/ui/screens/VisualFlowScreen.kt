package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.entity.ActionEntity
import com.example.data.model.ActionType
import com.example.data.model.AutomationState
import com.example.ui.components.ActionEditDialog
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraViolet
import com.example.ui.theme.AuraWarning
import com.example.ui.viewmodel.AuraMainViewModel

@Composable
fun VisualFlowScreen(
    viewModel: AuraMainViewModel
) {
    val actions by viewModel.actions.collectAsStateWithLifecycle()
    val selectedProfileId by viewModel.selectedProfileId.collectAsStateWithLifecycle()
    val automationState by viewModel.automationState.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var actionToEdit by remember { mutableStateOf<ActionEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Flow Title & Quick Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Visual Flow Pipeline",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Start → Actions → Conditions → Loops → End",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        actionToEdit = null
                        showEditDialog = true
                    },
                    modifier = Modifier.testTag("visual_flow_add_node")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AuraCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Node", color = AuraCyan)
                }

                if (automationState == AutomationState.RUNNING) {
                    Button(
                        onClick = { viewModel.stopAutomation() },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraError)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                    }
                } else {
                    Button(
                        onClick = { viewModel.startAutomation() },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraSuccess)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    }
                }
            }
        }

        // Interactive Flow Visualizer Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, AuraCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // START NODE
                FlowNodeItem(
                    title = "START",
                    subtitle = "Initialize Trigger",
                    color = AuraSuccess,
                    isPulsing = automationState == AutomationState.RUNNING && stats.currentStepIndex == 0,
                    onClick = {}
                )

                // Actions Sequence Nodes
                actions.forEachIndexed { index, action ->
                    FlowConnectorArrow()

                    val actionType = try {
                        ActionType.valueOf(action.type)
                    } catch (e: Exception) {
                        ActionType.CLICK
                    }

                    val nodeColor = when (actionType) {
                        ActionType.CLICK, ActionType.DOUBLE_CLICK -> AuraCyan
                        ActionType.LONG_CLICK, ActionType.SWIPE, ActionType.DRAG -> AuraViolet
                        ActionType.WAIT -> AuraWarning
                        ActionType.CONDITION_TEXT, ActionType.CONDITION_IMAGE -> AuraMagenta
                        ActionType.LOOP, ActionType.REPEAT -> AuraSuccess
                        else -> MaterialTheme.colorScheme.primary
                    }

                    val isCurrentStep = automationState == AutomationState.RUNNING &&
                            stats.currentStepName.contains(action.label.ifBlank { actionType.displayName }, ignoreCase = true)

                    FlowNodeItem(
                        title = "${index + 1}. ${action.label.ifBlank { actionType.displayName }}",
                        subtitle = when (actionType) {
                            ActionType.CLICK -> "Tap @ (${action.x}, ${action.y})"
                            ActionType.SWIPE -> "Swipe to (${action.endX}, ${action.endY})"
                            ActionType.WAIT -> "Pause for ${action.delayBeforeMs}ms"
                            ActionType.CONDITION_TEXT -> "IF text \"${action.conditionTarget}\""
                            ActionType.LOOP -> "Loop to Step #${action.jumpTargetIndex + 1}"
                            else -> actionType.displayName
                        },
                        color = nodeColor,
                        isPulsing = isCurrentStep,
                        onClick = {
                            actionToEdit = action
                            showEditDialog = true
                        }
                    )
                }

                FlowConnectorArrow()

                // END NODE
                FlowNodeItem(
                    title = "END / REPEAT",
                    subtitle = "Cycle Complete",
                    color = Color.Gray,
                    isPulsing = false,
                    onClick = {}
                )
            }
        }
    }

    if (showEditDialog) {
        ActionEditDialog(
            action = actionToEdit,
            profileId = selectedProfileId ?: 1L,
            orderIndex = actions.size,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.saveAction(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun FlowNodeItem(
    title: String,
    subtitle: String,
    color: Color,
    isPulsing: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = if (isPulsing) 0.35f else 0.12f))
            .border(
                width = if (isPulsing) 2.dp else 1.dp,
                color = if (isPulsing) AuraCyan else color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun FlowConnectorArrow() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(14.dp)
                .background(AuraCyan.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(AuraCyan)
        )
    }
}
