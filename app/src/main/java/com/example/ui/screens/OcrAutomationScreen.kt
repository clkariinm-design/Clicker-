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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.ConditionMatchMode
import com.example.ocr.TextRecognitionHelper
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSuccess
import com.example.ui.viewmodel.AuraMainViewModel

@Composable
fun OcrAutomationScreen(
    viewModel: AuraMainViewModel
) {
    val selectedProfileId by viewModel.selectedProfileId.collectAsStateWithLifecycle()

    var targetText by remember { mutableStateOf("Claim") }
    var matchMode by remember { mutableStateOf(ConditionMatchMode.CONTAINS) }
    var failureAction by remember { mutableStateOf("CONTINUE_NEXT") }
    var testInputText by remember { mutableStateOf("Special Reward: Tap Claim to receive coins!") }
    var testResult by remember { mutableStateOf<Boolean?>(null) }
    var statusMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FindInPage, contentDescription = null, tint = AuraCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart OCR Text Trigger",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Automatically scans screen text and initiates clicks or custom flows whenever trigger words or phrases appear.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Trigger Word or Phrase",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Text") },
                    placeholder = { Text("e.g. Next, Start, Claim, Victory") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ocr_target_text_input")
                )

                Text(
                    text = "Match Mode",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConditionMatchMode.values().forEach { mode ->
                        FilterChip(
                            selected = matchMode == mode,
                            onClick = { matchMode = mode },
                            label = { Text(mode.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text(
                    text = "When Text Is Not Found",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "CONTINUE_NEXT" to "Continue",
                        "STOP_AUTOMATION" to "Stop",
                        "JUMP_TO_ACTION" to "Jump Step"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = failureAction == key,
                            onClick = { failureAction = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Test Scanner Simulation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AuraCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Test Pattern Matcher",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = testInputText,
                    onValueChange = { testInputText = it },
                    label = { Text("Simulated Screen Text") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val matched = TextRecognitionHelper.evaluateCondition(testInputText, targetText, matchMode)
                        testResult = matched
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraCyan)
                ) {
                    Text("Evaluate Condition", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                testResult?.let { result ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (result) "CONDITION MET: Target \"$targetText\" was found!" else "CONDITION FAILED: Target \"$targetText\" not found.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (result) AuraSuccess else AuraError
                        )
                    }
                }
            }
        }

        // Add as Action Step Button
        Button(
            onClick = {
                val profileId = selectedProfileId ?: 1L
                val action = ActionEntity(
                    profileId = profileId,
                    orderIndex = 0,
                    type = "CONDITION_TEXT",
                    conditionTarget = targetText,
                    conditionMatchMode = matchMode.name,
                    conditionFailureAction = failureAction,
                    label = "OCR: IF \"$targetText\""
                )
                viewModel.saveAction(action)
                statusMessage = "Added OCR Condition Step to Profile!"
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ocr_add_action_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AuraSuccess)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add OCR Condition Step to Active Profile", fontWeight = FontWeight.Bold, color = Color.Black)
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
