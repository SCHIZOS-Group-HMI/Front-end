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
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hmi.R
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
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6FA))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Chat reply or loading animation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // Giới hạn chiều cao để kích hoạt cuộn nếu cần
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),

            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp),
                            color = Color(0xFF1976D2),
                            strokeWidth = 4.dp
                        )
                    } else {
                        val scrollState = rememberScrollState()
                        Text(
                            text = uiState.chatReply.orEmpty().ifEmpty { "No results yet" },
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Text(
                text = speechText,
                fontSize = 16.sp,
                color = Color(0xFF1976D2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
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
                            val left = (box.x * previewW).coerceIn(0f, previewW)
                            val top = (box.y * previewH).coerceIn(0f, previewH)
                            val right = ((box.x + box.w) * previewW).coerceIn(0f, previewW)
                            val bottom = ((box.y + box.h) * previewH).coerceIn(0f, previewH)

                            if (right > left && bottom > top) {
                                drawRect(
                                    color = Color.Red.copy(alpha = 0.7f),
                                    topLeft = Offset(left, top),
                                    size = Size(right - left, bottom - top),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.onScanClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (uiState.isScanning) "Stop Scanning" else "Start Scanning",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = MaterialTheme.typography.headlineSmall.fontFamily
                )
            }

            // Controls
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.onQuitClicked(); onQuitClicked() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text("QUIT", color = Color.White, fontSize = 16.sp)
                }
                Button(
                    onClick = { useFront = !useFront },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(if (useFront) "Front" else "Rear", color = Color.White, fontSize = 16.sp)
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = {
                            if (!hasMicPermission) micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            else viewModel.onMicClicked()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .border(2.dp, if (uiState.isMicOn) Color.Green else Color.Gray, RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_btn_speak_now),
                            contentDescription = "Mic",
                            tint = if (uiState.isMicOn) Color.Green else Color.Black
                        )
                    }
                    IconButton(
                        onClick = {
                            if (!sttActive) {
                                sttHelper.startListening()
                                sttActive = true
                            } else {
                                sttHelper.stopListening()
                                sttActive = false
                                viewModel.onQuestionChanged(speechText)
                                viewModel.onAskChat()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .border(2.dp, if (sttActive) Color(0xFF1976D2) else Color.Gray, RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.smart_toy),
                            contentDescription = "Speech-to-Text",
                            tint = if (sttActive) Color(0xFF1976D2) else Color.Gray
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