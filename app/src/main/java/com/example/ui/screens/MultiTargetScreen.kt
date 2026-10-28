package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraViolet
import com.example.ui.viewmodel.AuraMainViewModel

@Composable
fun MultiTargetScreen(
    viewModel: AuraMainViewModel
) {
    val actions by viewModel.actions.collectAsStateWithLifecycle()
    val selectedProfileId by viewModel.selectedProfileId.collectAsStateWithLifecycle()
    val automationState by viewModel.automationState.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var actionToEdit by remember { mutableStateOf<ActionEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    actionToEdit = null
                    showEditDialog = true
                },
                containerColor = AuraCyan,
                contentColor = Color.Black,
                modifier = Modifier.testTag("multi_target_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Action Point")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Action Bar: Run / Stop & Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sequence Actions (${actions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Each action executes in sequence with custom delays",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (automationState == AutomationState.RUNNING) {
                    Button(
                        onClick = { viewModel.stopAutomation() },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraError),
                        modifier = Modifier.testTag("multi_target_stop_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { viewModel.startAutomation() },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraSuccess),
                        modifier = Modifier.testTag("multi_target_start_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run", color = Color.Black)
                    }
                }
            }

            if (actions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No action steps defined yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap the + button below to add clicks, swipes or conditions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(actions, key = { _, item -> item.id }) { index, action ->
                        ActionCardItem(
                            action = action,
                            index = index,
                            canMoveUp = index > 0,
                            canMoveDown = index < actions.size - 1,
                            onMoveUp = {
                                val mutable = actions.toMutableList()
                                val temp = mutable[index]
                                mutable[index] = mutable[index - 1]
                                mutable[index - 1] = temp
                                viewModel.reorderActions(mutable)
                            },
                            onMoveDown = {
                                val mutable = actions.toMutableList()
                                val temp = mutable[index]
                                mutable[index] = mutable[index + 1]
                                mutable[index + 1] = temp
                                viewModel.reorderActions(mutable)
                            },
                            onEdit = {
                                actionToEdit = action
                                showEditDialog = true
                            },
                            onDelete = {
                                viewModel.deleteAction(action.id)
                            }
                        )
                    }
                }
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
private fun ActionCardItem(
    action: ActionEntity,
    index: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val actionType = try {
        ActionType.valueOf(action.type)
    } catch (e: Exception) {
        ActionType.CLICK
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AuraCyan.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index number badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AuraViolet),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.label.ifBlank { actionType.displayName },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val details = buildString {
                    append(actionType.displayName)
                    if (actionType in listOf(ActionType.CLICK, ActionType.DOUBLE_CLICK, ActionType.LONG_CLICK)) {
                        append(" @ (${action.x}, ${action.y})")
                    } else if (actionType == ActionType.SWIPE || actionType == ActionType.DRAG) {
                        append(" (${action.x},${action.y}) → (${action.endX},${action.endY})")
                    } else if (actionType == ActionType.CONDITION_TEXT) {
                        append(" IF \"${action.conditionTarget}\"")
                    } else if (actionType == ActionType.OPEN_APP) {
                        append(" \"${action.targetPackage}\"")
                    }
                    append(" • Delay: ${action.delayBeforeMs}ms")
                }

                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Up, Down, Edit, Delete Buttons
            Row {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (canMoveUp) AuraCyan else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (canMoveDown) AuraCyan else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Action",
                        tint = AuraViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Action",
                        tint = AuraError,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
