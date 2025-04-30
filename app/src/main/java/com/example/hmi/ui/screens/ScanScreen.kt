package com.example.hmi.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hmi.ui.theme.HMITheme
import com.example.hmi.ui.viewmodel.ScanViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = viewModel(),
    onQuitClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.onMicClicked()
        }
    }

    // Thêm ImageAnalysis để lấy imageProxy
    val imageAnalyzer = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    var latestImageProxy by remember { mutableStateOf<ImageProxy?>(null) }

    HMITheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Hiển thị kết quả quét (nếu có)
            Text(
                text = uiState.scanResult.ifEmpty { "No results yet" },
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                update = { previewView ->
                    Log.d("ScanScreen", "Setting up camera preview")
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            // Thiết lập ImageAnalysis để lấy imageProxy
                            imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                                latestImageProxy = imageProxy
                                // Không đóng imageProxy ở đây, để sử dụng sau
                            }

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalyzer
                            )
                            Log.d("ScanScreen", "Camera preview bound successfully")
                        } catch (exc: Exception) {
                            Log.e("ScanScreen", "Failed to bind camera preview", exc)
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            )

            Button(
                onClick = {
                    if (!hasMicPermission) {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        latestImageProxy?.let { imageProxy ->
                            viewModel.onScanClicked(imageProxy)
                        } ?: Log.e("ScanScreen", "No image proxy available")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (uiState.isScanning) "Stop Scanning" else "Start Scanning",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        viewModel.onQuitClicked()
                        onQuitClicked()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "QUIT",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            if (!hasMicPermission) {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.onMicClicked()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = if (uiState.isMicOn) 2.dp else 0.dp,
                                color = if (uiState.isMicOn) Color.Green else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_btn_speak_now),
                            contentDescription = "Microphone",
                            tint = if (uiState.isMicOn) Color.Green else Color.Black
                        )
                    }

                    IconButton(
                        onClick = { viewModel.onSpeakerClicked() },
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = if (uiState.isSpeakerOn) 2.dp else 0.dp,
                                color = if (uiState.isSpeakerOn) Color.Green else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_lock_silent_mode_off),
                            contentDescription = "Speaker",
                            tint = if (uiState.isSpeakerOn) Color.Green else Color.Black
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(cameraExecutor) {
        onDispose {
            cameraExecutor.shutdown()
            imageAnalyzer.clearAnalyzer()
            latestImageProxy?.close()
            Log.d("ScanScreen", "Camera executor shut down")
        }
    }
}