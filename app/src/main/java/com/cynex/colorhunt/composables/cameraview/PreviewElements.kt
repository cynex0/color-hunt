package com.cynex.colorhunt.composables.cameraview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.min

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun Crosshair() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val len = 15.dp.toPx()

        // Crosshair x
        drawLine(
            color = Color.White,
            start = Offset(centerX - len / 2, centerY),
            end = Offset(centerX + len / 2, centerY),
        )

        // Crosshair y
        drawLine(
            color = Color.White,
            start = Offset(centerX, centerY - len / 2),
            end = Offset(centerX, centerY + len / 2),
        )
    }
}

@Composable
fun Bounds(averagingZone: Float) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val len = 15.dp.toPx()
        val boundSize = min(size.width, size.height) * averagingZone

        // Top left corner
        drawLine(
            color = Color.White,
            start = Offset(centerX - boundSize, centerY - boundSize),
            end = Offset(centerX - boundSize + len, centerY - boundSize),
        )
        drawLine(
            color = Color.White,
            start = Offset(centerX - boundSize, centerY - boundSize),
            end = Offset(centerX - boundSize, centerY - boundSize + len),
        )

        // Top right corner
        drawLine(
            color = Color.White,
            start = Offset(centerX + boundSize - len, centerY - boundSize),
            end = Offset(centerX + boundSize, centerY - boundSize),
        )
        drawLine(
            color = Color.White,
            start = Offset(centerX + boundSize, centerY - boundSize),
            end = Offset(centerX + boundSize, centerY - boundSize + len),
        )

        // Bottom right corner
        drawLine(
            color = Color.White,
            start = Offset(centerX + boundSize - len, centerY + boundSize),
            end = Offset(centerX + boundSize, centerY + boundSize),
        )
        drawLine(
            color = Color.White,
            start = Offset(centerX + boundSize, centerY + boundSize),
            end = Offset(centerX + boundSize, centerY + boundSize - len),
        )

        // Bottom left corner
        drawLine(
            color = Color.White,
            start = Offset(centerX - boundSize, centerY + boundSize),
            end = Offset(centerX - boundSize + len, centerY + boundSize),
        )
        drawLine(
            color = Color.White,
            start = Offset(centerX - boundSize, centerY + boundSize),
            end = Offset(centerX - boundSize, centerY + boundSize - len),
        )
    }
}
