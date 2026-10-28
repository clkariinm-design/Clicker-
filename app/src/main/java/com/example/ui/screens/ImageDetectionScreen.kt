package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.entity.ActionEntity
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraViolet
import com.example.ui.viewmodel.AuraMainViewModel

@Composable
fun ImageDetectionScreen(
    viewModel: AuraMainViewModel
) {
    val selectedProfileId by viewModel.selectedProfileId.collectAsStateWithLifecycle()

    var imageKey by remember { mutableStateOf("reward_chest_icon") }
    var similarityThreshold by remember { mutableStateOf(80f) }
    var conditionType by remember { mutableStateOf("IF_IMAGE_EXISTS") }
    var onFailAction by remember { mutableStateOf("CONTINUE_NEXT") }
    var statusMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Description Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ImageSearch, contentDescription = null, tint = AuraViolet)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Image & Icon Detection",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Detects icons, buttons, or graphics before clicking. Perfect for variable game elements and changing UI layouts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Image Target Config
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Detection Rules",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = imageKey,
                    onValueChange = { imageKey = it },
                    label = { Text("Target Icon Identifier / Name") },
                    placeholder = { Text("e.g. chest_icon, gold_button, close_x") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("image_detection_key_input")
                )

                Text(
                    text = "Condition Rule",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "IF_IMAGE_EXISTS" to "IF Image Exists",
                        "IF_CLICK_SUCCEEDS" to "IF Click Succeeds",
                        "IF_TIMEOUT" to "IF Timeout"
                    ).forEach { (type, label) ->
                        FilterChip(
                            selected = conditionType == type,
                            onClick = { conditionType = type },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text(
                    text = "Similarity Confidence Threshold: ${similarityThreshold.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Slider(
                    value = similarityThreshold,
                    onValueChange = { similarityThreshold = it },
                    valueRange = 50f..100f,
                    steps = 10,
                    modifier = Modifier.testTag("image_similarity_slider")
                )

                Text(
                    text = "Fallback Action On Failure",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "CONTINUE_NEXT" to "Continue",
                        "STOP_AUTOMATION" to "Stop",
                        "JUMP_TO_ACTION" to "Jump Step"
                    ).forEach { (action, label) ->
                        FilterChip(
                            selected = onFailAction == action,
                            onClick = { onFailAction = action },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Add to Active Profile Button
        Button(
            onClick = {
                val profileId = selectedProfileId ?: 1L
                val action = ActionEntity(
                    profileId = profileId,
                    orderIndex = 0,
                    type = "CONDITION_IMAGE",
                    conditionTarget = imageKey,
                    conditionFailureAction = onFailAction,
                    label = "Detect: $imageKey (${similarityThreshold.toInt()}%)"
                )
                viewModel.saveAction(action)
                statusMessage = "Added Image Detection Rule to Active Profile!"
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("image_detection_add_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AuraCyan)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Image Detection Step to Profile", fontWeight = FontWeight.Bold, color = Color.Black)
        }

        if (statusMessage.isNotBlank()) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = AuraSuccess
            )
        }
    }
}
