package com.example.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AuraClickVisualizer
import com.example.ui.components.AuraTopBar
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GestureRecorderScreen
import com.example.ui.screens.ImageDetectionScreen
import com.example.ui.screens.InteractiveSimulatorScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.MultiTargetScreen
import com.example.ui.screens.OcrAutomationScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.SchedulerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SingleTargetScreen
import com.example.ui.screens.VisualFlowScreen
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraViolet
import com.example.ui.viewmodel.AuraMainViewModel
import kotlinx.coroutines.launch

data class DrawerMenuItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val category: String = "Main"
)

@Composable
fun AuraMainApp(viewModel: AuraMainViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val automationState by viewModel.automationState.collectAsStateWithLifecycle()
    val rippleColor by viewModel.rippleColor.collectAsStateWithLifecycle()
    val clickEffect by viewModel.clickEffect.collectAsStateWithLifecycle()

    val menuItems = listOf(
        DrawerMenuItem("Dashboard", Screen.Dashboard.route, Icons.Default.Dashboard, "Home"),
        DrawerMenuItem("Single Target", Screen.SingleTarget.route, Icons.Default.TouchApp, "Automation"),
        DrawerMenuItem("Multi Target", Screen.MultiTarget.route, Icons.Default.FormatListNumbered, "Automation"),
        DrawerMenuItem("Visual Flow", Screen.VisualFlow.route, Icons.Default.AccountTree, "Automation"),
        DrawerMenuItem("Gesture Recorder", Screen.GestureRecorder.route, Icons.Default.Gesture, "Automation"),
        DrawerMenuItem("OCR Trigger", Screen.OcrAutomation.route, Icons.Default.FindInPage, "Smart Logic"),
        DrawerMenuItem("Image Detection", Screen.ImageDetection.route, Icons.Default.ImageSearch, "Smart Logic"),
        DrawerMenuItem("Live Simulator", Screen.Simulator.route, Icons.Default.PlayCircle, "Testing"),
        DrawerMenuItem("Macro Profiles", Screen.Profiles.route, Icons.Default.Layers, "Management"),
        DrawerMenuItem("Scheduler", Screen.Scheduler.route, Icons.Default.Alarm, "Management"),
        DrawerMenuItem("Logs & History", Screen.Logs.route, Icons.Default.History, "Management"),
        DrawerMenuItem("Settings", Screen.Settings.route, Icons.Default.Settings, "Configuration")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header Brand
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AuraCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, tint = AuraCyan)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Aura Auto Clicker",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cyber Macro Engine",
                                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.8.sp),
                                color = AuraViolet
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                    menuItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationDrawerItem(
                            label = { Text(item.title) },
                            selected = isSelected,
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) AuraCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(Screen.Dashboard.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = AuraCyan.copy(alpha = 0.15f),
                                unselectedContainerColor = Color.Transparent,
                                selectedTextColor = AuraCyan,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    ) {
        val currentScreenTitle = menuItems.find { it.route == currentRoute }?.title ?: "Aura Auto Clicker"
        val canNavigateBack = currentRoute != Screen.Dashboard.route

        Scaffold(
            topBar = {
                AuraTopBar(
                    title = currentScreenTitle,
                    state = automationState,
                    canNavigateBack = canNavigateBack,
                    onNavigateBack = { navController.popBackStack() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onStartClick = { viewModel.startAutomation() },
                    onStopClick = { viewModel.stopAutomation() }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Live global click ripple visualizer
                AuraClickVisualizer(
                    events = viewModel.visualClickEvents,
                    rippleColorArgb = rippleColor,
                    effectType = clickEffect
                )

                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route
                ) {
                    composable(Screen.Dashboard.route) {
                        DashboardScreen(viewModel = viewModel, onNavigateTo = { route -> navController.navigate(route) })
                    }
                    composable(Screen.SingleTarget.route) {
                        SingleTargetScreen(viewModel = viewModel)
                    }
                    composable(Screen.MultiTarget.route) {
                        MultiTargetScreen(viewModel = viewModel)
                    }
                    composable(Screen.VisualFlow.route) {
                        VisualFlowScreen(viewModel = viewModel)
                    }
                    composable(Screen.GestureRecorder.route) {
                        GestureRecorderScreen(viewModel = viewModel)
                    }
                    composable(Screen.OcrAutomation.route) {
                        OcrAutomationScreen(viewModel = viewModel)
                    }
                    composable(Screen.ImageDetection.route) {
                        ImageDetectionScreen(viewModel = viewModel)
                    }
                    composable(Screen.Simulator.route) {
                        InteractiveSimulatorScreen(viewModel = viewModel)
                    }
                    composable(Screen.Profiles.route) {
                        ProfilesScreen(viewModel = viewModel)
                    }
                    composable(Screen.Scheduler.route) {
                        SchedulerScreen(viewModel = viewModel)
                    }
                    composable(Screen.Logs.route) {
                        LogsScreen(viewModel = viewModel)
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
