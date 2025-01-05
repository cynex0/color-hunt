package com.cynex.colorhunt.core.coloranalyzer

import android.graphics.PixelFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.Color
import java.nio.ByteBuffer
import kotlin.math.sqrt

class ColorAnalyzer(private val listener: ColorChangeListener, private val averagingZone: Float): ImageAnalysis.Analyzer {
    private var prevColor: Color? = null

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
        var pixelCount = 0F;

        for (y in centerY - windowRadius until centerY + windowRadius) {
            for (x in centerX - windowRadius until centerX + windowRadius) {
                val pixelIndex = pixelStride * x + rowStride * y
                if (calculateEuclideanDistance(center, Pair(x, y)) > windowRadius) continue

                val r = buffer.get(pixelIndex).toInt() and 0xFF
                val g = buffer.get(pixelIndex + 1).toInt() and 0xFF
                val b = buffer.get(pixelIndex + 2).toInt() and 0xFF
                rTotal += r * r
                gTotal += g * g
                bTotal += b * b
                pixelCount++;
            }
        }

        val r = sqrt(rTotal / pixelCount)
        val g = sqrt(gTotal / pixelCount)
        val b = sqrt(bTotal / pixelCount)
        val color = Color(r.toInt(), g.toInt(), b.toInt())
        val delta = prevColor?.let { prev -> calculateColorDelta(color, prev) }

        if (delta != null && delta >= 1.0) {
            listener.onColorChanged(color)
        }
        // Log.d("ColorAnalyzer", "Color : $color, Prev: $prevColor")
        prevColor = color
        image.close()
    }
}


interface ColorChangeListener {
    fun onColorChanged(color: Color)
}