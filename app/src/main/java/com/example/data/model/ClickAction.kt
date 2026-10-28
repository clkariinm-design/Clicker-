package com.example.data.model

data class ClickAction(
    val id: Long = 0,
    val profileId: Long = 0,
    val orderIndex: Int = 0,
    val type: ActionType = ActionType.CLICK,
    val x: Int = 540,
    val y: Int = 960,
    val endX: Int = 540,
    val endY: Int = 500,
    val delayBeforeMs: Long = 100,
    val durationMs: Long = 50,
    val repeatCount: Int = 1,
    val targetPackage: String = "",
    val conditionTarget: String = "",
    val conditionMatchMode: ConditionMatchMode = ConditionMatchMode.CONTAINS,
    val conditionFailureAction: ConditionFailureAction = ConditionFailureAction.CONTINUE_NEXT,
    val jumpTargetIndex: Int = 0,
    val label: String = ""
)

data class AutomationStats(
    val totalClicks: Long = 0L,
    val completedCycles: Long = 0L,
    val elapsedTimeSeconds: Long = 0L,
    val currentStepIndex: Int = 0,
    val currentStepName: String = "Idle"
)

data class RecordedGesturePoint(
    val x: Float,
    val y: Float,
    val timestampMs: Long
)

data class FlowNode(
    val id: String,
    val actionId: Long,
    val title: String,
    val subtitle: String,
    val type: ActionType,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val connectedNextId: String? = null
)
