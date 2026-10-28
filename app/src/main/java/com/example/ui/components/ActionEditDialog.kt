package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.database.entity.ActionEntity
import com.example.data.model.ActionType
import com.example.data.model.ConditionFailureAction
import com.example.data.model.ConditionMatchMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionEditDialog(
    action: ActionEntity?,
    profileId: Long,
    orderIndex: Int,
    onDismiss: () -> Unit,
    onSave: (ActionEntity) -> Unit
) {
    var selectedType by remember {
        mutableStateOf(
            action?.let {
                try { ActionType.valueOf(it.type) } catch (e: Exception) { ActionType.CLICK }
            } ?: ActionType.CLICK
        )
    }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    var label by remember { mutableStateOf(action?.label ?: "") }
    var xCoord by remember { mutableStateOf(action?.x?.toString() ?: "540") }
    var yCoord by remember { mutableStateOf(action?.y?.toString() ?: "960") }
    var endXCoord by remember { mutableStateOf(action?.endX?.toString() ?: "540") }
    var endYCoord by remember { mutableStateOf(action?.endY?.toString() ?: "400") }
    var delayBefore by remember { mutableStateOf(action?.delayBeforeMs?.toString() ?: "100") }
    var duration by remember { mutableStateOf(action?.durationMs?.toString() ?: "50") }
    var targetPackage by remember { mutableStateOf(action?.targetPackage ?: "") }
    var conditionTarget by remember { mutableStateOf(action?.conditionTarget ?: "") }
    var conditionMatchMode by remember { mutableStateOf(action?.conditionMatchMode ?: "CONTAINS") }
    var conditionFailureAction by remember { mutableStateOf(action?.conditionFailureAction ?: "CONTINUE_NEXT") }
    var jumpTargetIndex by remember { mutableStateOf(action?.jumpTargetIndex?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (action == null) "Add New Action" else "Edit Action",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Action Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Action Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("action_type_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        ActionType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Action Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Action Label / Description") },
                    placeholder = { Text("e.g. Tap Accept Button") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_label_input")
                )

                // Coordinate fields for click, swipe, drag
                if (selectedType in listOf(ActionType.CLICK, ActionType.DOUBLE_CLICK, ActionType.LONG_CLICK, ActionType.SWIPE, ActionType.DRAG)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = xCoord,
                            onValueChange = { xCoord = it },
                            label = { Text("Target X (px)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_x_input")
                        )
                        OutlinedTextField(
                            value = yCoord,
                            onValueChange = { yCoord = it },
                            label = { Text("Target Y (px)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_y_input")
                        )
                    }
                }

                if (selectedType in listOf(ActionType.SWIPE, ActionType.DRAG)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = endXCoord,
                            onValueChange = { endXCoord = it },
                            label = { Text("End X (px)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endYCoord,
                            onValueChange = { endYCoord = it },
                            label = { Text("End Y (px)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Delays and Durations
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = delayBefore,
                        onValueChange = { delayBefore = it },
                        label = { Text("Delay Before (ms)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_delay_input")
                    )
                    if (selectedType in listOf(ActionType.CLICK, ActionType.LONG_CLICK, ActionType.SWIPE, ActionType.DRAG)) {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration (ms)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Special fields for Open App
                if (selectedType == ActionType.OPEN_APP) {
                    OutlinedTextField(
                        value = targetPackage,
                        onValueChange = { targetPackage = it },
                        label = { Text("Package Name") },
                        placeholder = { Text("com.example.app") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Condition Fields
                if (selectedType == ActionType.CONDITION_TEXT || selectedType == ActionType.CONDITION_IMAGE) {
                    OutlinedTextField(
                        value = conditionTarget,
                        onValueChange = { conditionTarget = it },
                        label = { Text(if (selectedType == ActionType.CONDITION_TEXT) "Target Text to Match" else "Template/Image Key") },
                        placeholder = { Text(if (selectedType == ActionType.CONDITION_TEXT) "e.g. Claim, Success" else "icon_key") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = conditionMatchMode,
                        onValueChange = { conditionMatchMode = it },
                        label = { Text("Match Mode (CONTAINS / EXACT / REGEX)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = conditionFailureAction,
                        onValueChange = { conditionFailureAction = it },
                        label = { Text("On Fail (CONTINUE_NEXT / STOP_AUTOMATION / JUMP_TO_ACTION)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Loop or Jump target
                if (selectedType in listOf(ActionType.LOOP, ActionType.REPEAT) || conditionFailureAction == "JUMP_TO_ACTION") {
                    OutlinedTextField(
                        value = jumpTargetIndex,
                        onValueChange = { jumpTargetIndex = it },
                        label = { Text("Jump Target Step (Index 0-based)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = ActionEntity(
                        id = action?.id ?: 0L,
                        profileId = profileId,
                        orderIndex = action?.orderIndex ?: orderIndex,
                        type = selectedType.name,
                        x = xCoord.toIntOrNull() ?: 540,
                        y = yCoord.toIntOrNull() ?: 960,
                        endX = endXCoord.toIntOrNull() ?: 540,
                        endY = endYCoord.toIntOrNull() ?: 400,
                        delayBeforeMs = delayBefore.toLongOrNull() ?: 100L,
                        durationMs = duration.toLongOrNull() ?: 50L,
                        repeatCount = 1,
                        targetPackage = targetPackage,
                        conditionTarget = conditionTarget,
                        conditionMatchMode = conditionMatchMode,
                        conditionFailureAction = conditionFailureAction,
                        jumpTargetIndex = jumpTargetIndex.toIntOrNull() ?: 0,
                        label = label.ifBlank { selectedType.displayName }
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("action_save_button")
            ) {
                Text("Save Action")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
