// ScanScreen.kt
package com.example.hmi.ui.screens

import ScanViewModelFactory
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.hmi.utils.SpeechToTextHelper
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    onQuitClicked: () -> Unit,
    viewModel: ScanViewModel = viewModel(
        factory = ScanViewModelFactory(LocalContext.current)
    )
) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var speechText by remember { mutableStateOf("") }
    var sttActive by remember { mutableStateOf(false) }
    val sttHelper = remember {
        SpeechToTextHelper(context) { text ->
            speechText = text
        }
    }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) viewModel.onMicClicked()
    }

    var useFront by remember { mutableStateOf(false) }
    val imageAnalyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    HMITheme {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                uiState.chatReply.orEmpty().ifEmpty { "No results yet" },
                fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(16.dp)
            )
            Text(
                text = speechText,
                fontSize = 14.sp,
                color = Color.Blue,
                modifier = Modifier.padding(8.dp)
            )

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)) {
                // 1. Camera Preview
                AndroidView(
                    modifier = Modifier.matchParentSize(),
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }
                    },
                    update = { previewView ->
                        val providerFut = ProcessCameraProvider.getInstance(context)
                        providerFut.addListener({
                            val camProvider = providerFut.get()
                            val selector = if (useFront)
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            else
                                CameraSelector.DEFAULT_BACK_CAMERA

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            imageAnalyzer.setAnalyzer(cameraExecutor) { proxy ->
                                // Convert → Base64
                                val bmp = proxy.toBitmap()
                                val baos = ByteArrayOutputStream().apply {
                                    bmp.compress(Bitmap.CompressFormat.JPEG, 50, this)
                                }
                                viewModel.latestImageBase64 =
                                    Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                                proxy.close()
                            }

                            camProvider.unbindAll()
                            camProvider.bindToLifecycle(
                                lifecycleOwner, selector, preview, imageAnalyzer
                            )
                        }, ContextCompat.getMainExecutor(context))
                    }
                )

                // 2. Overlay boxes
                Canvas(modifier = Modifier.matchParentSize()) {
                    val previewW = size.width
                    val previewH = size.height

                    uiState.boxes.forEach { box ->
                        // Tính toạ độ tuyệt đối
                        val left = (box.x * previewW).coerceIn(0f, previewW)
                        val top = (box.y * previewH).coerceIn(0f, previewH)
                        val right = ((box.x + box.w) * previewW).coerceIn(0f, previewW)
                        val bottom = ((box.y + box.h) * previewH).coerceIn(0f, previewH)

                        // Nếu box còn có kích thước hợp lệ thì vẽ
                        if (right > left && bottom > top) {
                            drawRect(
                                color = Color.Red,
                                topLeft = Offset(left, top),
                                size = Size(right - left, bottom - top),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }

            }

            // Scan button
            Button(
                onClick = { viewModel.onScanClicked() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    if (uiState.isScanning) "Stop Scanning" else "Start Scanning",
                    color = Color.White
                )
            }

            // Controls
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { useFront = !useFront },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text(if (useFront) "Camera Sau" else "Camera Trước", color = Color.White)
                }
                Button(
                    onClick = { viewModel.onQuitClicked(); onQuitClicked() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("QUIT", color = Color.White)
                }
                Row {
                    IconButton(onClick = {
                        if (!hasMicPermission) micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        else viewModel.onMicClicked()
                    }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_btn_speak_now),
                            contentDescription = "Mic",
                            tint = if (uiState.isMicOn) Color.Green else Color.Black
                        )
                    }
                    IconButton(onClick = { viewModel.onSpeakerClicked() }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_lock_silent_mode_off),
                            contentDescription = "Speaker",
                            tint = if (uiState.isSpeakerOn) Color.Green else Color.Black
                        )
                    }
                    IconButton(
                        onClick = {
                            if (!sttActive) {
                                // Bắt đầu nghe
                                sttHelper.startListening()
                                sttActive = true
                            } else {
                                // Dừng nghe và gửi query
                                sttHelper.stopListening()
                                sttActive = false
                                viewModel.onQuestionChanged(speechText)
                                viewModel.onAskChat()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = if (sttActive) 2.dp else 0.dp,
                                color = if (sttActive) Color.Blue else Color.Transparent
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_edit),
                            contentDescription = "Speech-to-Text",
                            tint = if (sttActive) Color.Blue else Color.Gray
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
        }
    }
}
