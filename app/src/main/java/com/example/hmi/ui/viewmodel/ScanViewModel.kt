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
    val userQuestion: String = "",
    val chatReply: String? = null,
    val isLoading: Boolean = false // Added loading state
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
    private var frameCounter: Int = 0 // Đếm số frame đã quét

    init {
        audioData = AudioData()
    }

    /** Nhận câu hỏi từ UI */
    fun onQuestionChanged(q: String) {
        _uiState.update { it.copy(userQuestion = q) }
    }

    fun onAskChat() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) } // Set loading to true
            val prompt = buildString {
                appendLine("Người dùng hỏi: \"${_uiState.value.userQuestion}\"")
                appendLine("Dưới đây là kết quả quét:")
                _uiState.value.scanResult.forEachIndexed { i, result ->
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
            frameCounter = 0 // Đặt lại frameCounter khi dừng quét
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

        // Ghi âm
        val (audioB64, amplitude) = audioData?.captureAudio() ?: ("" to 0.0)

        // Tạo yêu cầu
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
                // Lấy danh sách các hộp bao
                val dets: List<ObjectDetectionResult> = body?.objectDetection ?: emptyList()
                val bboxes: List<BoundingBox> = dets.map { it.bbox }
                // Tạo chuỗi kết quả quét
                val txt = "Đối tượng: ${dets.joinToString { it.className }} | Âm thanh: ${body?.audioDetection}"
                _uiState.update {
                    val newResults = (it.scanResult + txt).takeLast(10)
                    it.copy(scanResult = newResults, boxes = bboxes)
                }
                // Tăng frameCounter và kiểm tra gửi chatbot
                frameCounter++
                if (frameCounter >= 3) {
                    onAskChat() // Gửi kết quả tới chatbot sau 10 frame
                    frameCounter = 0 // Đặt lại bộ đếm
                }
            } else {
                _uiState.update { it.copy(scanResult = it.scanResult + "Thất bại: ${resp.code()} | Thời gian: $timestamp") }
            }
        } catch (e: Exception) {
            Log.e("ScanViewModel", "Lỗi khi gửi yêu cầu", e)
            _uiState.update { it.copy(scanResult = it.scanResult + "Lỗi: ${e.message} | Thời gian: $timestamp") }
        }
    }

    fun onQuitClicked() {
        scanJob?.cancel()
        scanJob = null
        frameCounter = 0 // Đặt lại frameCounter khi thoát
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
        frameCounter = 0 // Đặt lại frameCounter khi ViewModel bị xóa
    }
}