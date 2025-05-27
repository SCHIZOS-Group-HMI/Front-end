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

private const val SCAN_INTERVAL_MS = 500L
private const val SCAN_DURATION_MS = 10_000L // 10 giây

data class ScanUiState(
    val isScanning: Boolean = false,
    val isMicOn: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val scanResult: List<String> = emptyList(),
    val boxes: List<BoundingBox> = emptyList(),
    val userQuestion: String = "",
    val chatReply: String? = null,
    val isLoading: Boolean = false
)

class ScanViewModel(
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var audioData: AudioData? = null
    var latestImageBase64: String? = null

    private var scanJob: Job? = null
    private var scanStartTime: Long = 0L
    private val scanResults = mutableListOf<String>()

    init {
        audioData = AudioData()
    }

    fun onQuestionChanged(q: String) {
        _uiState.update { it.copy(userQuestion = q) }
    }

    fun onAskChat() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val prompt = buildString {
                appendLine("Người dùng hỏi: \"${_uiState.value.userQuestion}\"")
                appendLine("Dưới đây là kết quả quét tổng hợp:")
                scanResults.forEachIndexed { i, result ->
                    appendLine("- Kết quả[$i]: $result")
                }
            }
            try {
                val resp = ApiClient.apiService.sendChat(ChatRequest(prompt))
                if (resp.isSuccessful) {
                    val reply = resp.body()?.reply.orEmpty()
                    _uiState.update { it.copy(chatReply = reply, isLoading = false) }
                } else {
                    _uiState.update { it.copy(chatReply = "Lỗi: ${resp.code()}", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(chatReply = "Lỗi: ${e.message}", isLoading = false) }
            } finally {
                scanResults.clear() // Xóa kết quả sau khi gửi
            }
        }
    }

    fun onScanClicked() {
        if (scanJob == null) {
            _uiState.update { it.copy(isScanning = true, scanResult = emptyList(), chatReply = null) }
            scanStartTime = System.currentTimeMillis()
            scanResults.clear()
            scanJob = viewModelScope.launch(Dispatchers.IO) {
                while (isActive && System.currentTimeMillis() - scanStartTime < SCAN_DURATION_MS) {
                    val imgB64 = latestImageBase64
                    latestImageBase64 = null
                    if (imgB64 != null) {
                        sendScanRequest(imgB64)
                    }
                    delay(SCAN_INTERVAL_MS)
                }
                // Kết thúc quét tự động hoặc khi dừng, gửi kết quả cho chatbot
                if (isActive) {
                    onAskChat()
                }
                _uiState.update { it.copy(isScanning = false) }
                scanJob = null
            }
        } else {
            scanJob?.cancel()
            scanJob = null
            onAskChat() // Gửi kết quả tổng hợp khi dừng thủ công
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    private suspend fun sendScanRequest(imageB64: String) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val errorMsg = "Lỗi: Yêu cầu quyền RECORD_AUDIO"
            scanResults.add(errorMsg)
            _uiState.update { it.copy(scanResult = scanResults.takeLast(10)) }
            return
        }

        val (audioB64, amplitude) = audioData?.captureAudio() ?: ("" to 0.0)

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
                val dets: List<ObjectDetectionResult> = body?.objectDetection ?: emptyList()
                val txt = "Đối tượng: ${dets.joinToString { it.className }} | Âm thanh: ${body?.audioDetection}"
                scanResults.add(txt)
                _uiState.update { it.copy(scanResult = scanResults.takeLast(10)) }
            } else {
                val errorMsg = "Thất bại: ${resp.code()} | Thời gian: $timestamp"
                scanResults.add(errorMsg)
                _uiState.update { it.copy(scanResult = scanResults.takeLast(10)) }
            }
        } catch (e: Exception) {
            Log.e("ScanViewModel", "Lỗi khi gửi yêu cầu", e)
            val errorMsg = "Lỗi: ${e.message} | Thời gian: $timestamp"
            scanResults.add(errorMsg)
            _uiState.update { it.copy(scanResult = scanResults.takeLast(10)) }
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