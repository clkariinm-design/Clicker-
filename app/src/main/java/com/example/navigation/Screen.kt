package com.example.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object SingleTarget : Screen("single_target", "Single Target")
    object MultiTarget : Screen("multi_target", "Multi Target")
    object VisualFlow : Screen("visual_flow", "Visual Flow")
    object GestureRecorder : Screen("gesture_recorder", "Gesture Recorder")
    object OcrAutomation : Screen("ocr_automation", "OCR Automation")
    object ImageDetection : Screen("image_detection", "Image Detection")
    object Profiles : Screen("profiles", "Profiles")
    object Scheduler : Screen("scheduler", "Scheduler")
    object Logs : Screen("logs", "Logs & History")
    object Settings : Screen("settings", "Settings")
    object Simulator : Screen("simulator", "Live Simulator")
}
