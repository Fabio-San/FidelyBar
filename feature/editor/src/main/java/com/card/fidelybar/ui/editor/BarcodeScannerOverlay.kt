package com.card.fidelybar.ui.editor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.card.fidelybar.data.BarcodeFormatType
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine

private fun mapMlKitFormat(mlKitFormat: Int): BarcodeFormatType = when (mlKitFormat) {
    Barcode.FORMAT_EAN_13 -> BarcodeFormatType.EAN13
    Barcode.FORMAT_EAN_8 -> BarcodeFormatType.EAN8
    Barcode.FORMAT_UPC_A -> BarcodeFormatType.UPC_A
    Barcode.FORMAT_CODE_128 -> BarcodeFormatType.CODE_128
    Barcode.FORMAT_CODE_39 -> BarcodeFormatType.CODE_39
    Barcode.FORMAT_CODABAR -> BarcodeFormatType.CODABAR
    Barcode.FORMAT_ITF -> BarcodeFormatType.ITF
    Barcode.FORMAT_DATA_MATRIX -> BarcodeFormatType.DATA_MATRIX
    Barcode.FORMAT_QR_CODE -> BarcodeFormatType.QR_CODE
    else -> BarcodeFormatType.CODE_128
}

private suspend fun awaitCameraProvider(context: Context): ProcessCameraProvider =
    suspendCancellableCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                val result = runCatching { future.get() }
                if (cont.isActive) {
                    result.fold(
                        onSuccess = { cont.resume(it) },
                        onFailure = { cont.resumeWithException(it) }
                    )
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

@Composable
fun BarcodeScannerOverlay(
    onResult: (content: String, format: BarcodeFormatType) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val handled = remember { AtomicBoolean(false) }
    var bindAttempt by remember { mutableIntStateOf(0) }
    var bindError by remember { mutableStateOf<String?>(null) }

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(bindAttempt) {
        if (handled.get()) return@LaunchedEffect
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            bindError = "Permesso fotocamera non concesso. Riapri lo scanner e concedi il permesso."
            return@LaunchedEffect
        }
        bindError = null

        val provider = try {
            awaitCameraProvider(context)
        } catch (_: Exception) {
            bindError = "Impossibile accedere alla fotocamera. Chiudi altre app che usano la camera e riprova."
            return@LaunchedEffect
        }

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODABAR,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_QR_CODE
            )
            .build()
        val scanner = BarcodeScanning.getClient(options)
        val preview = Preview.Builder().build()
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        @Suppress("UnsafeOptInUsageError")
        analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
            if (handled.get()) {
                imageProxy.close()
                return@setAnalyzer
            }
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val detected = barcodes
                            .firstOrNull { !it.rawValue.isNullOrBlank() }
                            ?: barcodes.firstOrNull()
                        if (detected != null && handled.compareAndSet(false, true)) {
                            onResult(detected.rawValue.orEmpty(), mapMlKitFormat(detected.format))
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } else {
                imageProxy.close()
            }
        }

        // Su molti device (es. Poco/MIUI) il primo bind può fallire in modo
        // transitorio subito dopo la concessione del permesso: riproviamo.
        var lastError: Throwable? = null
        repeat(3) { attempt ->
            try {
                if (attempt > 0) delay(300L * attempt)
                provider.unbindAll()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                return@LaunchedEffect
            } catch (e: Exception) {
                lastError = e
            }
        }
        runCatching { provider.unbindAll() }
        bindError = lastError?.message?.takeIf { it.isNotBlank() }
            ?: "Fotocamera non disponibile in questo momento. Riprova."
    }

    DisposableEffect(Unit) {
        onDispose {
            if (handled.compareAndSet(false, true)) {
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener(
                    {
                        runCatching { future.get().unbindAll() }
                    },
                    ContextCompat.getMainExecutor(context)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.Black)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                text = "Inquadra il codice",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ComposeColor.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Lo riconosco e compilo il numero da solo",
                style = MaterialTheme.typography.bodyMedium,
                color = ComposeColor.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (bindError != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    text = bindError.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = ComposeColor.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                FilledTonalButton(onClick = { bindAttempt++ }) {
                    Text("Riprova")
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(240.dp)
                    .border(3.dp, ComposeColor.White.copy(alpha = 0.9f), RoundedCornerShape(28.dp))
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            TextButton(onClick = onCancel) {
                Text("Annulla", color = ComposeColor.White)
            }
        }
    }
}