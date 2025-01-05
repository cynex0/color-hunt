package com.cynex.colorhunt.core.coloranalyzer

import android.graphics.PixelFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

class ColorAnalyzer(private val listener: ColorChangeListener, private val averagingZone: Float): ImageAnalysis.Analyzer {
    private var prevColor: Triple<Int,Int,Int>? = null

    override fun analyze(image: ImageProxy) {
        if (image.format != PixelFormat.RGBA_8888) {
            Log.e("ColorAnalyzer", "Unsupported image format: ${image.format}. RGBA_8888 expected")
            image.close()
            return
        }

        val plane = image.planes[0]
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val buffer: ByteBuffer = plane.buffer

        val centerX = image.width / 2
        val centerY = image.height / 2
        val center = Pair(centerX, centerY)

        val windowRadius = (minOf(image.width, image.height) * averagingZone).toInt()

        var rTotal = 0;
        var gTotal = 0;
        var bTotal = 0;
        var pixelCount = 0;

        for (y in centerY - windowRadius until centerY + windowRadius) {
            for (x in centerX - windowRadius until centerX + windowRadius) {
                val pixelIndex = pixelStride * x + rowStride * y
                if (calculateEuclideanDistance(center, Pair(x, y)) > windowRadius) continue

                rTotal += buffer.get(pixelIndex).toInt() and 0xFF
                gTotal += buffer.get(pixelIndex + 1).toInt() and 0xFF
                bTotal += buffer.get(pixelIndex + 2).toInt() and 0xFF
                pixelCount++;
            }
        }

        val r = rTotal / pixelCount
        val g = gTotal / pixelCount
        val b = bTotal / pixelCount
        val delta = prevColor?.let { prev -> calculateColorDelta(Triple(r, g, b), prev) }

        if (delta != null && delta >= 1.0) {
            val color = String.format("#%02x%02x%02x", r, g, b)
            listener.onColorChanged(color)
        }
        prevColor = Triple(r, g, b)
        image.close()
    }
}


interface ColorChangeListener {
    fun onColorChanged(color: String?)
}