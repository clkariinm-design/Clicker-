package com.example.imagedetection

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

object ImageDetectionHelper {

    /**
     * Compares a template image with a source region to calculate similarity score (0.0 to 1.0).
     * Uses normalized pixel difference.
     */
    fun calculateSimilarity(
        source: Bitmap,
        template: Bitmap,
        startX: Int = 0,
        startY: Int = 0
    ): Float {
        val tWidth = template.width
        val tHeight = template.height

        if (startX + tWidth > source.width || startY + tHeight > source.height) {
            return 0f
        }

        var totalDifference = 0L
        var pixelCount = 0

        val step = 2 // Sample every 2nd pixel for performance

        for (y in 0 until tHeight step step) {
            for (x in 0 until tWidth step step) {
                val sPixel = source.getPixel(startX + x, startY + y)
                val tPixel = template.getPixel(x, y)

                val diffR = abs(Color.red(sPixel) - Color.red(tPixel))
                val diffG = abs(Color.green(sPixel) - Color.green(tPixel))
                val diffB = abs(Color.blue(sPixel) - Color.blue(tPixel))

                totalDifference += (diffR + diffG + diffB)
                pixelCount++
            }
        }

        if (pixelCount == 0) return 0f
        val maxPossibleDiff = pixelCount * 3 * 255.0
        val differenceRatio = totalDifference / maxPossibleDiff
        return (1.0 - differenceRatio).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Finds if template exists within the source image exceeding similarity threshold (e.g. 0.85f).
     */
    fun findTemplateMatch(
        source: Bitmap,
        template: Bitmap,
        threshold: Float = 0.80f
    ): Pair<Int, Int>? {
        val step = 20 // Step size for sliding window
        for (y in 0 until (source.height - template.height) step step) {
            for (x in 0 until (source.width - template.width) step step) {
                val score = calculateSimilarity(source, template, x, y)
                if (score >= threshold) {
                    return Pair(x + template.width / 2, y + template.height / 2)
                }
            }
        }
        return null
    }
}
