package com.cynex.colorhunt.core

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
        val plane = image.planes[0]
        val buffer = plane.buffer
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride

        val centerX = image.width / 2
        val centerY = image.height / 2
        val centerIndex = centerY * rowStride + centerX * pixelStride


        val pixelValue = buffer.toByteArray()[centerIndex]
        val color = String.format("#%02x%02x%02x", pixelValue, pixelValue, pixelValue)
        listener.onColorChanged(color)
        image.close()
    }
}

interface ColorChangeListener {
    fun onColorChanged(color: String?)
}