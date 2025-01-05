package com.cynex.colorhunt.composables.cameraview

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cynex.colorhunt.core.ColorAnalyzer
import com.cynex.colorhunt.core.ColorChangeListener
import com.cynex.colorhunt.core.calculateColorDelta

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun CameraView(averagingZone: Float = 0.1f) {
    val currentColor = remember { mutableStateOf<String?>(null) }
    val targetColor = "#FFFFFF"
    val delta = remember { mutableStateOf<Double?>(null) }

    val colorChangeListener = object: ColorChangeListener {
        override fun onColorChanged(color: String?) {
            currentColor.value = color
            delta.value = currentColor.value?.let { calculateColorDelta(it, targetColor) }
            // TODO: error message if null
        }
    }

    Scaffold { paddingValues: PaddingValues ->
        Column (
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        {
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .aspectRatio(4f / 3f),
            ) {
                ColorAnalyzerPreviewView(
                    colorAnalyzer = ColorAnalyzer(colorChangeListener, averagingZone)
                )
                Crosshair()
                Bounds(averagingZone)
            }

            ColorsCompare(currentColor, targetColor, delta)
        }
    }
}



@Composable
fun ColorsCompare(currentColor: MutableState<String?>, targetColor: String, delta: MutableState<Double?>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ColorBoxTitled(currentColor.value, "Current")
        ColorBoxTitled(targetColor, "Target")
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = "Delta: ${delta.value?: "N/A"}")
}

@Composable
fun ColorBoxTitled(color: String?, title: String) {
    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        ColorBox(color)
    }
}

@Composable
fun ColorBox(color: String?, showValue: Boolean = false) {
    val bgColor: Color
    if (color == null) {
        Log.e("ColorBox", "Invalid color: $color")
        bgColor = Color.Black
    } else {
        bgColor = Color(android.graphics.Color.parseColor(color))
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .wrapContentSize(Alignment.Center)
    ) {
        if (showValue) {
            Text(
                text = color ?: "N/A",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

