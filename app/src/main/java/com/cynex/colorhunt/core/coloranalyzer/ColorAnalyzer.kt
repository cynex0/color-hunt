package com.cynex.colorhunt.core.coloranalyzer

import android.graphics.PixelFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.Color

class ColorAnalyzer(private val listener: ColorChangeListener, private val averagingZone: Float, private val strategy: AveragingStrategy): ImageAnalysis.Analyzer {
    private var prevColor: Color? = null

    override fun analyze(image: ImageProxy) {
        if (image.format != PixelFormat.RGBA_8888) {
            Log.e("ColorAnalyzer", "Unsupported image format: ${image.format}. RGBA_8888 expected")
            image.close()
            return
        }

        val color = strategy.calculateAverageColor(image, averagingZone)
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
