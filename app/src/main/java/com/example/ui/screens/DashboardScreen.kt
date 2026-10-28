package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AutomationState
import com.example.navigation.Screen
import com.example.ui.components.StatusCard
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraViolet
import com.example.ui.theme.AuraWarning
import com.example.ui.viewmodel.AuraMainViewModel
import com.example.utils.PermissionHelper

@Composable
fun DashboardScreen(
    viewModel: AuraMainViewModel,
    onNavigateTo: (String) -> Unit
) {
    val context = LocalContext.current
    val automationState by viewModel.automationState.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val selectedProfileId by viewModel.selectedProfileId.collectAsStateWithLifecycle()
    val isOverlayShowing by viewModel.isOverlayShowing.collectAsStateWithLifecycle()
    val permissions by viewModel.permissionsState.collectAsStateWithLifecycle()

    val activeProfile = profiles.find { it.id == selectedProfileId }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        StatusCard(
            state = automationState,
            stats = stats,
            profileName = activeProfile?.name ?: "No Profile Selected",
            isOverlayActive = isOverlayShowing,
            onStartClick = { viewModel.startAutomation() },
            onPauseClick = { viewModel.pauseAutomation() },
            onResumeClick = { viewModel.resumeAutomation() },
            onStopClick = { viewModel.stopAutomation() },
            onToggleOverlayClick = { viewModel.toggleOverlay(context) }
        )

        // Permission Status Section
        PermissionsSection(
            context = context,
            accessibilityGranted = permissions.isAccessibilityGranted,
            overlayGranted = permissions.isOverlayGranted,
            batteryIgnored = permissions.isBatteryOptimizationIgnored,
            onRefresh = { viewModel.checkPermissions(context) }
        )

        // Mode Launchers (Feature 2, 3, 5, 7)
        Text(
            text = "Automation Modes",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeLauncherCard(
                title = "Single Target",
                subtitle = "Rapid auto-tap, interval & jitter",
                icon = Icons.Default.TouchApp,
                accentColor = AuraCyan,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo(Screen.SingleTarget.route) }
            )
            ModeLauncherCard(
                title = "Multi Target",
                subtitle = "Sequences, swipes & delays",
                icon = Icons.Default.FormatListNumbered,
                accentColor = AuraViolet,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo(Screen.MultiTarget.route) }
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeLauncherCard(
                title = "Visual Flow",
                subtitle = "Interactive node automation editor",
                icon = Icons.Default.AccountTree,
                accentColor = AuraSuccess,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo(Screen.VisualFlow.route) }
            )
            ModeLauncherCard(
                title = "Gesture Recorder",
                subtitle = "Record & replay swipe paths",
                icon = Icons.Default.Gesture,
                accentColor = AuraWarning,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo(Screen.GestureRecorder.route) }
            )
        }

        // Live In-App Simulator Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateTo(Screen.Simulator.route) }
                .testTag("dashboard_simulator_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AuraCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = AuraCyan)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Interactive Screen Simulator",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Test floating targets, canvas clicks & gestures live",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = AuraCyan)
            }
        }
    }
}

@Composable
private fun PermissionsSection(
    context: Context,
    accessibilityGranted: Boolean,
    overlayGranted: Boolean,
    batteryIgnored: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "System Permissions",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            PermissionRow(
                title = "Accessibility Service",
                description = "Required to dispatch touches, clicks & swipes",
                isGranted = accessibilityGranted,
                onGrantClick = { PermissionHelper.openAccessibilitySettings(context) }
            )

            PermissionRow(
                title = "Display Over Other Apps",
                description = "Required for floating movable control panel",
                isGranted = overlayGranted,
                onGrantClick = { PermissionHelper.openOverlaySettings(context) }
            )

            PermissionRow(
                title = "Battery Optimization",
                description = "Prevents Android from killing clicks in background",
                isGranted = batteryIgnored,
                onGrantClick = { PermissionHelper.requestIgnoreBatteryOptimizations(context) }
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) AuraSuccess else AuraWarning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!isGranted) {
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = AuraCyan.copy(alpha = 0.2f)),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text("Enable", color = AuraCyan, style = MaterialTheme.typography.labelSmall)
            }
        } else {
            Text(
                text = "Active",
                color = AuraSuccess,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun ModeLauncherCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() }
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}
