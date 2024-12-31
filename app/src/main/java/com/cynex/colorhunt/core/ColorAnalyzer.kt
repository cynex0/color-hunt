package com.cynex.colorhunt.core

import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class ColorAnalyzer(private val listener: ColorChangeListener): ImageAnalysis.Analyzer {
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy) {
        if (image.format == PixelFormat.RGBA_8888) {
            val plane = image.planes[0]
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val centerPixel = pixelStride * (image.width / 2) + rowStride * (image.height / 2)
            val buffer: ByteBuffer = plane.buffer

            val r = buffer.get(centerPixel).toInt() and 0xFF
            val g = buffer.get(centerPixel + 1).toInt() and 0xFF
            val b = buffer.get(centerPixel + 2).toInt() and 0xFF

            val color = String.format("#%02x%02x%02x", r, g, b)
            listener.onColorChanged(color)
        } else {
            Log.e("ColorAnalyzer", "Unsupported image format: ${image.format}")
        }
        image.close()
    }
}

interface ColorChangeListener {
    fun onColorChanged(color: String?)
}