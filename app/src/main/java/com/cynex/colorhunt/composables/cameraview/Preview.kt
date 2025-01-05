package com.cynex.colorhunt.composables.cameraview

import android.util.Log
import android.view.ScaleGestureDetector
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.cynex.colorhunt.core.ColorAnalyzer

@Composable
fun ColorAnalyzerPreviewView(colorAnalyzer: ColorAnalyzer) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = {
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }.also { previewView ->
                setupPreviewView(previewView, context, lifecycleOwner, colorAnalyzer)
            }
        },
    )
}

private fun setupPreviewView(
    previewView: PreviewView,
    context: android.content.Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    analyzer: ImageAnalysis.Analyzer
) {
    val imageAnalysis = ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .apply {
            setAnalyzer(
                ContextCompat.getMainExecutor(context),
                analyzer
            )
        }

    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )

            val scaleGestureDetector = ScaleGestureDetector(context,
                object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        val currentZoomRatio = camera.cameraInfo.zoomState.value!!.zoomRatio
                        val delta = detector.scaleFactor
                        camera.cameraControl.setZoomRatio(currentZoomRatio * delta)
                        // Log.d("CameraPreview", "Scale factor: $delta, Zoom ratio: ${currentZoomRatio}, Set to: ${currentZoomRatio * delta}")
                        return true
                    }
                }
            )

            previewView.setOnTouchListener {_, event ->
                scaleGestureDetector.onTouchEvent(event)
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    previewView.performClick()
                }
                return@setOnTouchListener true
            }
        } catch (e: Exception) {
            Log.e("CameraPreview", "Use case binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}
