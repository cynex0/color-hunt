package com.cynex.colorhunt.core.coloranalyzer

import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.Color
import java.nio.ByteBuffer
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

interface AveragingStrategy {
    fun calculateAverageColor(image: ImageProxy, averagingZone: Float): Color
    override fun toString(): String
}

class LinearAveraging: AveragingStrategy {
    override fun calculateAverageColor(image: ImageProxy, averagingZone: Float): Color {
        val plane = image.planes[0]
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val buffer: ByteBuffer = plane.buffer

        val centerX = image.width / 2
        val centerY = image.height / 2
        val center = Pair(centerX, centerY)

        val windowRadius = (minOf(image.width, image.height) * averagingZone).toInt()

        var rTotal = 0.0;
        var gTotal = 0.0;
        var bTotal = 0.0;
        var pixelCount = 0;

        for (y in centerY - windowRadius until centerY + windowRadius) {
            for (x in centerX - windowRadius until centerX + windowRadius) {
                val pixelIndex = pixelStride * x + rowStride * y
                val dist = calculateEuclideanDistance(center, Pair(x, y))
                if (dist > windowRadius) continue

                val r = buffer.get(pixelIndex).toInt() and 0xFF
                val g = buffer.get(pixelIndex + 1).toInt() and 0xFF
                val b = buffer.get(pixelIndex + 2).toInt() and 0xFF

                rTotal += r * r
                gTotal += g * g
                bTotal += b * g
                pixelCount++;
            }
        }

        val r = sqrt(rTotal / pixelCount)
        val g = sqrt(gTotal / pixelCount)
        val b = sqrt(bTotal / pixelCount)
        return Color(r.toInt(), g.toInt(), b.toInt())
    }

    override fun toString(): String {
        return "Linear Averaging"
    }
}

abstract class WeightedAveraging: AveragingStrategy {
    abstract fun getWeight(distance: Double, radius: Double): Double
    override fun calculateAverageColor(image: ImageProxy, averagingZone: Float): Color {
        val plane = image.planes[0]
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val buffer: ByteBuffer = plane.buffer

        val centerX = image.width / 2
        val centerY = image.height / 2
        val center = Pair(centerX, centerY)

        val windowRadius = (minOf(image.width, image.height) * averagingZone).toDouble()

        var rTotal = 0.0;
        var gTotal = 0.0;
        var bTotal = 0.0;
        var weightSum = 0.0;

        for (y in centerY - windowRadius.toInt() until centerY + windowRadius.toInt()) {
            for (x in centerX - windowRadius.toInt() until centerX + windowRadius.toInt()) {
                val pixelIndex = pixelStride * x + rowStride * y
                val dist = calculateEuclideanDistance(center, Pair(x, y))
                if (dist > windowRadius) continue

                val r = buffer.get(pixelIndex).toInt() and 0xFF
                val g = buffer.get(pixelIndex + 1).toInt() and 0xFF
                val b = buffer.get(pixelIndex + 2).toInt() and 0xFF
                val weight = this.getWeight(dist, windowRadius)

                rTotal += r * weight
                gTotal += g * weight
                bTotal += b * weight
                weightSum += weight;
            }
        }

        val r = rTotal / weightSum
        val g = gTotal / weightSum
        val b = bTotal / weightSum
        return Color(r.toInt(), g.toInt(), b.toInt())
    }
}

class PowerWeightedAveraging(private val power: Int): WeightedAveraging() {
    override fun getWeight(distance: Double, radius: Double): Double {
        return (1 - (distance / radius)).pow(power)
    }
    override fun toString(): String {
        return "Power Weighted Averaging (^$power)"
    }
}

class GaussianWeightedAveraging(private val factor: Double = 1.0): WeightedAveraging() {
    override fun getWeight(distance: Double, radius: Double): Double {
        val sigma = radius / factor
        return exp(-distance.pow(2) / (2 * sigma.pow(2)))
    }

    override fun toString(): String {
        return "Gaussian Weighted Averaging (σ = $factor)"
    }
}