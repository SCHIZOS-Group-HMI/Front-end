// ScanViewModel.kt
package com.example.hmi.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hmi.data.AudioData
import com.example.hmi.data.Metadata
import com.example.hmi.network.ScanRequest
import com.example.hmi.network.ObjectDetectionResult
import com.example.hmi.network.BoundingBox
import com.example.hmi.network.ApiClient
import com.example.hmi.network.ChatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val SCAN_INTERVAL_MS = 2000L

data class ScanUiState(
    val isScanning: Boolean = false,
    val isMicOn: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val scanResult: List<String> = emptyList(),
    val boxes: List<BoundingBox> = emptyList(),
    val userQuestion : String = "",
    val chatReply : String? = null
)

class ScanViewModel(
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var audioData: AudioData? = null

    /** Giữ Base64 của ảnh mới nhất từ UI */
    var latestImageBase64: String? = null

    private var scanJob: Job? = null

    init {
        audioData = AudioData()
    }
    /** Nhận câu hỏi từ UI */
    fun onQuestionChanged(q: String) {
        _uiState.update { it.copy(userQuestion = q) }
    }

    fun onAskChat() {
        viewModelScope.launch(Dispatchers.IO) {
            val prompt = buildString {
                appendLine("User asked: \"${_uiState.value.userQuestion}\"")
                appendLine("Here are scan results:")
                _uiState.value.scanResult.forEachIndexed { i, result ->
                    appendLine("- Result[$i]: $result")
                }
            }
            try {
                val resp = ApiClient.apiService.sendChat(ChatRequest(prompt))
                if (resp.isSuccessful) {
                    val reply = resp.body()?.reply.orEmpty()
                    _uiState.update { it.copy(chatReply = reply) }
                } else {
                    _uiState.update { it.copy(chatReply = "Error: ${resp.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(chatReply = "Error: ${e.message}") }
            }
        }
    }
    /** Bật/tắt chế độ scan liên tục */
    fun onScanClicked() {
        if (scanJob == null) {
            _uiState.update { it.copy(isScanning = true) }
            scanJob = viewModelScope.launch(Dispatchers.IO) {
                while (isActive) {
                    val imgB64 = latestImageBase64
                    latestImageBase64 = null
                    if (imgB64 != null) {
                        sendScanRequest(imgB64)
                    }
                    delay(SCAN_INTERVAL_MS)
                }
            }
        } else {
            scanJob?.cancel()
            scanJob = null
            _uiState.value = ScanUiState()
        }
    }

    private suspend fun sendScanRequest(imageB64: String) {
        // Kiểm tra quyền RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(scanResult = it.scanResult + "Lỗi: Yêu cầu quyền RECORD_AUDIO") }
            return
        }

        // Capture audio
        val (audioB64, amplitude) = audioData?.captureAudio() ?: ("" to 0.0)

        // Tạo request
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .format(Date())
        val request = ScanRequest(
            timestamp = timestamp,
            image = imageB64,
            audio = audioB64,
            audioAmplitude = amplitude,
            metadata = Metadata()
        )

        try {
            val resp = ApiClient.apiService.sendScanData(request)
            if (resp.isSuccessful) {
                val body = resp.body()
                // Extract boxes
                val dets: List<ObjectDetectionResult> = body?.objectDetection ?: emptyList()
                val bboxes: List<BoundingBox> = dets.map { it.bbox }
                // Update UI state
                val txt = "Objects: ${dets.joinToString { it.className }} | Audio: ${body?.audioDetection}"
                _uiState.update {
                    val newResults = (it.scanResult + txt).takeLast(10)
                    it.copy(scanResult = newResults, boxes = bboxes)
                }
            } else {
                _uiState.update { it.copy(scanResult = it.scanResult + "Thất bại: ${resp.code()} | Thời gian: $timestamp") }
            }
        } catch (e: Exception) {
            Log.e("ScanViewModel", "Error sending request", e)
            _uiState.update { it.copy(scanResult = it.scanResult + "Lỗi: ${e.message} | Thời gian: $timestamp") }
        }
    }

    fun onQuitClicked() {
        scanJob?.cancel()
        scanJob = null
        _uiState.value = ScanUiState()
    }

    fun onMicClicked() {
        _uiState.update { it.copy(isMicOn = !it.isMicOn) }
    }

    fun onSpeakerClicked() {
        _uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
