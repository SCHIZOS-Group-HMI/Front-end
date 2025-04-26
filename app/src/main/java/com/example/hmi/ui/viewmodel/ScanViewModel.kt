package com.example.hmi.ui.viewmodel

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hmi.data.AudioData
import com.example.hmi.data.ImageData
import com.example.hmi.data.Metadata
import com.example.hmi.network.ApiClient
import com.example.hmi.network.ScanRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScanUiState(
    val isScanning: Boolean = false,
    val isMicOn: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val scanResult: String = ""
)

class ScanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    private var audioData: AudioData? = null
    private var imageData: ImageData? = null
    private var isReadingAudio = false

    init {
        audioData = AudioData()
        imageData = ImageData()
    }

    fun onScanClicked(imageProxy: ImageProxy? = null) {
        Log.d("ScanViewModel", "Scan clicked")
        _uiState.value = _uiState.value.copy(
            isScanning = !_uiState.value.isScanning
        )
        if (_uiState.value.isScanning && imageProxy != null) {
            sendScanRequest(imageProxy)
        }
        if (!_uiState.value.isScanning) {
            _uiState.value = _uiState.value.copy(
                scanResult = ""
            )
        }
    }

    private fun sendScanRequest(imageProxy: ImageProxy) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
// Capture audio and image
                val (audioBase64, amplitude) = audioData?.captureAudio() ?: Pair("", 0.0)
                val imageBase64 = imageData?.captureImage(imageProxy) ?: ""

// Prepare request data
                val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                val metadata = Metadata()
                val request = ScanRequest(
                    timestamp = timestamp,
                    image = imageBase64,
                    audio = audioBase64,
                    audioAmplitude = amplitude,
                    metadata = metadata
                )

// Send request to API
                val response = ApiClient.apiService.sendScanData(request)
                if (response.isSuccessful) {
                    Log.d("ScanViewModel", "Request sent successfully")
                    _uiState.value = _uiState.value.copy(
                        scanResult = "Data sent successfully"
                    )
                } else {
                    Log.e("ScanViewModel", "Request failed: ${response.code()}")
                    _uiState.value = _uiState.value.copy(
                        scanResult = "Failed to send data"
                    )
                }
            } catch (e: Exception) {
                Log.e("ScanViewModel", "Error sending request", e)
                _uiState.value = _uiState.value.copy(
                    scanResult = "Error: ${e.message}"
                )
            }
        }
    }

    fun onQuitClicked() {
        Log.d("ScanViewModel", "Quit clicked")
        stopAudioCapture()
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            isMicOn = false,
            isSpeakerOn = false,
            scanResult = ""
        )
    }

    fun onMicClicked() {
        Log.d("ScanViewModel", "Microphone clicked")
        if (!_uiState.value.isMicOn) {
            startAudioCapture()
        } else {
            stopAudioCapture()
        }
        _uiState.value = _uiState.value.copy(
            isMicOn = !_uiState.value.isMicOn
        )
        Log.d("ScanViewModel", "Mic is ${if (_uiState.value.isMicOn) "ON" else "OFF"}")
    }

    fun onSpeakerClicked() {
        Log.d("ScanViewModel", "Speaker clicked")
        _uiState.value = _uiState.value.copy(
            isSpeakerOn = !_uiState.value.isSpeakerOn
        )
        Log.d("ScanViewModel", "Speaker is ${if (_uiState.value.isSpeakerOn) "ON" else "OFF"}")
    }

    private fun startAudioCapture() {
        isReadingAudio = true
        Log.d("ScanViewModel", "Audio capture started")
    }

    private fun stopAudioCapture() {
        isReadingAudio = false
        audioData?.release()
        Log.d("ScanViewModel", "Audio capture stopped")
    }

    override fun onCleared() {
        super.onCleared()
        stopAudioCapture()
    }
}