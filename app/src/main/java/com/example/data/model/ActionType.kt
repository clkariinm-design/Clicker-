package com.example.data.model

enum class ActionType(val displayName: String, val iconName: String) {
    CLICK("Click", "TouchApp"),
    LONG_CLICK("Long Click", "TouchHold"),
    DOUBLE_CLICK("Double Click", "DoubleTap"),
    SWIPE("Swipe", "Swipe"),
    DRAG("Drag", "OpenWith"),
    WAIT("Wait", "HourglassEmpty"),
    LOOP("Loop", "Loop"),
    REPEAT("Repeat", "Repeat"),
    OPEN_APP("Open App", "Apps"),
    BACK("System Back", "ArrowBack"),
    HOME("System Home", "Home"),
    RECENTS("Recent Apps", "ViewList"),
    CONDITION_TEXT("IF Text", "FindInPage"),
    CONDITION_IMAGE("IF Image", "ImageSearch")
}

enum class ConditionMatchMode {
    CONTAINS,
    EXACT,
    REGEX
}

enum class ConditionFailureAction {
    CONTINUE_NEXT,
    STOP_AUTOMATION,
    JUMP_TO_ACTION
}

enum class ProfileMode {
    SINGLE_TARGET,
    MULTI_TARGET,
    VISUAL_FLOW,
    GESTURE_REPLAY
}

enum class AutomationState {
    IDLE,
    RUNNING,
    PAUSED,
    ERROR
}
