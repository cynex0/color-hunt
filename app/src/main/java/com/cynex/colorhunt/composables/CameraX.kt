package com.cynex.colorhunt.composables

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.cynex.colorhunt.core.ColorAnalyzer
import com.cynex.colorhunt.core.ColorChangeListener
import kotlin.math.min

@Composable
fun CameraPreviewScreen(averagingZone: Float) {
    val currentColor = remember { mutableStateOf<String?>(null) }
    val colorChangeListener = object: ColorChangeListener {
        override fun onColorChanged(color: String?) {
            currentColor.value = color
        }
    }

    Scaffold { paddingValues: PaddingValues ->
        Column (
            modifier = Modifier.fillMaxSize().padding(paddingValues),
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

            currentColor.value?.let {
                Spacer(modifier = Modifier.height(16.dp))
                ColorBox(color = it)
            }
        }
    }
}

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

@Composable
fun ColorAnalyzerPreviewView(colorAnalyzer: ColorAnalyzer) {
    val context = LocalContext.current
    val imageAnalysis = ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also { analyzer ->
            analyzer.setAnalyzer(
                ContextCompat.getMainExecutor(context),
                colorAnalyzer
            )
        }
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

        AndroidView(
            factory = {
                PreviewView(context).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }.also { previewView ->
                    previewView.controller = cameraController

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            },
        )
}

@Composable
fun ColorBox(color: String) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(android.graphics.Color.parseColor(color)))
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = color,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}