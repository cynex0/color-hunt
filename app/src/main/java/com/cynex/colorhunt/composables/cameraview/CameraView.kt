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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cynex.colorhunt.core.coloranalyzer.AveragingStrategy
import com.cynex.colorhunt.core.coloranalyzer.ColorAnalyzer
import com.cynex.colorhunt.core.coloranalyzer.ColorChangeListener
import com.cynex.colorhunt.core.coloranalyzer.GaussianWeightedAveraging
import com.cynex.colorhunt.core.coloranalyzer.LinearAveraging
import com.cynex.colorhunt.core.coloranalyzer.PowerWeightedAveraging
import com.cynex.colorhunt.core.coloranalyzer.calculateColorDelta
import com.cynex.colorhunt.core.coloranalyzer.toRgb255

@Composable
fun CameraView(averagingZone: Float = 0.1f) {
    val currentColor = remember { mutableStateOf<Color?>(null) }
    val targetColor = Color(255, 255, 255)
    val delta = remember { mutableStateOf<Double?>(null) }

    val strategyIndex = remember { mutableIntStateOf(0) }
    val strategies: List<AveragingStrategy> =
        listOf(
            LinearAveraging(),
            PowerWeightedAveraging(2),
            PowerWeightedAveraging(3),
            PowerWeightedAveraging(4),
            GaussianWeightedAveraging(2.0),
            GaussianWeightedAveraging(3.0),
            GaussianWeightedAveraging(4.0)
        )

    val colorChangeListener = object: ColorChangeListener {
        override fun onColorChanged(color: Color) {
            currentColor.value = color
            delta.value = currentColor.value?.let { calculateColorDelta(it, targetColor) }
            // Log.d("ColorAnalyzer", "Color changed: ${currentColor.value}")
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
                key(strategyIndex.intValue) {
                    ColorAnalyzerPreviewView(
                        colorAnalyzer = ColorAnalyzer(colorChangeListener, averagingZone, strategies[strategyIndex.intValue])
                    )
                }
                Crosshair()
                CircleBound(averagingZone)
            }

            ColorsCompare(currentColor, targetColor, delta)
            Button(onClick = {
                strategyIndex.intValue = (strategyIndex.intValue + 1) % strategies.size
                Log.d("CameraView", "Strategy changed to: ${strategies[strategyIndex.intValue]}")
            }) {
                Text(strategyIndex.intValue.toString())
            }
        }
    }
}



@Composable
fun ColorsCompare(currentColor: MutableState<Color?>, targetColor: Color, delta: MutableState<Double?>) {
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
fun ColorBoxTitled(color: Color?, title: String) {
    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        ColorBox(color, true)
   }
}

@Composable
fun ColorBox(color: Color?, showValue: Boolean = false) {
    val bgColor: Color
    if (color == null) {
        Log.e("ColorBox", "Invalid color")
        bgColor = Color.Black
    } else {
        bgColor = color
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
            val textColor = if (bgColor.luminance() > 0.5) Color.Black else Color.White
            val rgbValues = color?.toRgb255()
            Text(
                text = if (rgbValues != null) String.format("#%02x%02x%02x", rgbValues.first, rgbValues.second, rgbValues.third) else "N/A",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

