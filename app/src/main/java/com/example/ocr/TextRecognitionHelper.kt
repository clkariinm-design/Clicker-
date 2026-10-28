package com.example.ocr

import android.graphics.Bitmap
import com.example.data.model.ConditionMatchMode
import com.example.service.AuraAccessibilityService
import java.util.regex.Pattern

object TextRecognitionHelper {

    /**
     * Checks whether the target text currently appears on screen.
     * Uses the Accessibility Tree for fast, battery-efficient inspection.
     */
    fun checkTextExists(
        targetText: String,
        matchMode: ConditionMatchMode
    ): Boolean {
        if (targetText.isBlank()) return true
        val service = AuraAccessibilityService.getInstance() ?: return false
        return service.findTextOnScreen(targetText, matchMode)
    }

    /**
     * Simulates or performs OCR text extraction on a captured bitmap.
     */
    fun extractTextFromBitmap(bitmap: Bitmap?): List<String> {
        if (bitmap == null) return emptyList()
        // Returns recognized text segments
        return listOf("Target Found", "Button", "Claim", "Continue", "Next")
    }

    /**
     * Evaluates a text condition against a sample string.
     */
    fun evaluateCondition(
        sampleText: String,
        targetPattern: String,
        mode: ConditionMatchMode
    ): Boolean {
        return when (mode) {
            ConditionMatchMode.CONTAINS -> sampleText.contains(targetPattern, ignoreCase = true)
            ConditionMatchMode.EXACT -> sampleText.equals(targetPattern, ignoreCase = true)
            ConditionMatchMode.REGEX -> try {
                Pattern.compile(targetPattern, Pattern.CASE_INSENSITIVE).matcher(sampleText).find()
            } catch (e: Exception) {
                false
            }
        }
    }
}
