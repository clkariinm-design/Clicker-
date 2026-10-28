package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import com.example.data.database.entity.ProfileEntity
import com.example.data.model.AutomationState
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSuccess
import com.example.ui.viewmodel.AuraMainViewModel

@Composable
fun SingleTargetScreen(
    viewModel: AuraMainViewModel
) {
    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val selectedProfileId by viewModel.selectedProfileId.collectAsStateWithLifecycle()
    val automationState by viewModel.automationState.collectAsStateWithLifecycle()

    val currentProfile = profiles.find { it.id == selectedProfileId }
        ?: profiles.firstOrNull { it.mode == "SINGLE_TARGET" }

    var intervalMs by remember(currentProfile) {
        mutableStateOf(currentProfile?.clickIntervalMs?.toString() ?: "100")
    }
    var startDelayMs by remember(currentProfile) {
        mutableStateOf(currentProfile?.startDelayMs?.toString() ?: "500")
    }
    var repeatCount by remember(currentProfile) {
        mutableStateOf(currentProfile?.repeatCount?.toString() ?: "100")
    }
    var isInfinite by remember(currentProfile) {
        mutableStateOf(currentProfile?.isInfinite ?: true)
    }
    var jitterRadius by remember(currentProfile) {
        mutableStateOf(currentProfile?.randomJitterRadius?.toFloat() ?: 0f)
    }

    val updateProfileSettings = {
        if (currentProfile != null) {
            val updated = currentProfile.copy(
                mode = "SINGLE_TARGET",
                clickIntervalMs = intervalMs.toLongOrNull() ?: 100L,
                startDelayMs = startDelayMs.toLongOrNull() ?: 500L,
                repeatCount = repeatCount.toIntOrNull() ?: 100,
                isInfinite = isInfinite,
                randomJitterRadius = jitterRadius.toInt()
            )
            viewModel.updateProfile(updated)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Presets
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Speed Presets",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf(
                        "Turbo (20ms)" to "20",
                        "Fast (50ms)" to "50",
                        "Normal (100ms)" to "100",
                        "Slow (500ms)" to "500"
                    )
                    presets.forEach { (label, value) ->
                        FilterChip(
                            selected = intervalMs == value,
                            onClick = {
                                intervalMs = value
                                updateProfileSettings()
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Click Interval & Timing
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Timing & Frequency",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = intervalMs,
                    onValueChange = {
                        intervalMs = it
                        updateProfileSettings()
                    },
                    label = { Text("Click Interval (Milliseconds)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("single_target_interval_input")
                )

                OutlinedTextField(
                    value = startDelayMs,
                    onValueChange = {
                        startDelayMs = it
                        updateProfileSettings()
                    },
                    label = { Text("Delay Before Starting (Milliseconds)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Repetitions & Infinite Mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Execution Repetition",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Infinite Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Clicks continuously until stopped manually",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isInfinite,
                        onCheckedChange = {
                            isInfinite = it
                            updateProfileSettings()
                        },
                        modifier = Modifier.testTag("single_target_infinite_switch")
                    )
                }

                if (!isInfinite) {
                    OutlinedTextField(
                        value = repeatCount,
                        onValueChange = {
                            repeatCount = it
                            updateProfileSettings()
                        },
                        label = { Text("Repeat Count (Times)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Anti-Detection & Random Position Jitter
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Random Position Jitter: ${jitterRadius.toInt()} px",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Applies randomized offset around the target to bypass bot detection",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = jitterRadius,
                    onValueChange = {
                        jitterRadius = it
                        updateProfileSettings()
                    },
                    valueRange = 0f..50f,
                    steps = 10,
                    modifier = Modifier.testTag("single_target_jitter_slider")
                )
            }
        }

        // Action Buttons: Start / Stop
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (automationState == AutomationState.RUNNING) {
                Button(
                    onClick = { viewModel.stopAutomation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("single_target_stop_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraError)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop Rapid Clicker", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Button(
                    onClick = {
                        updateProfileSettings()
                        viewModel.startAutomation()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("single_target_start_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraSuccess)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Rapid Clicker", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}
