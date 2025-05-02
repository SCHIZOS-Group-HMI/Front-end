// ScanViewModel.kt
package com.example.hmi.ui.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hmi.data.AudioData
import com.example.hmi.data.Metadata
import com.example.hmi.network.ApiClient
import com.example.hmi.network.ScanRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SCAN_INTERVAL_MS = 7000L

data class ScanUiState(
    val isScanning: Boolean = false,
    val isMicOn: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val scanResult: String = ""
)

class ScanViewModel(
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var audioData: AudioData? = null

    // Holds the latest Base64 image string
    var latestImageBase64: String? = null

    private var scanJob: Job? = null

    init {
        audioData = AudioData()
    }

    /** Start/stop continuous scanning */
    fun onScanClicked() {
        if (scanJob == null) {
            _uiState.value = _uiState.value.copy(isScanning = true)
            scanJob = viewModelScope.launch(Dispatchers.IO) {
                while (isActive) {
                    latestImageBase64?.let { imgB64 ->
                        latestImageBase64 = null
                        sendScanRequest(imgB64)
                    }
                    delay(SCAN_INTERVAL_MS)
                }
            }
        } else {
            scanJob?.cancel()
            scanJob = null
            _uiState.value = ScanUiState() // reset
        }
    }

    private fun sendScanRequest(imageB64: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    _uiState.value = _uiState.value.copy(
                        scanResult = "Error: RECORD_AUDIO permission required"
                    )
                    return@launch
                }

                // capture audio
                val (audioB64, amplitude) = audioData?.captureAudio() ?: Pair("", 0.0)

                val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    .format(Date())
                val request = ScanRequest(
                    timestamp     = timestamp,
                    image         = imageB64,
                    audio         = audioB64,
                    audioAmplitude= amplitude,
                    metadata      = Metadata()
                )

                val response = ApiClient.apiService.sendScanData(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    _uiState.value = _uiState.value.copy(
                        scanResult = "Objects: ${body?.objectDetection?.joinToString { it.className }} | " +
                                "Audio: ${body?.audioDetection}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        scanResult = "Failed: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("ScanViewModel", "Error", e)
                _uiState.value = _uiState.value.copy(
                    scanResult = "Error: ${e.message}"
                )
            }
        }
    }

    fun onQuitClicked() {
        scanJob?.cancel()
        scanJob = null
        _uiState.value = ScanUiState()
    }

    fun onMicClicked() {
        // toggle mic logic...
        _uiState.value = _uiState.value.copy(isMicOn = !_uiState.value.isMicOn)
    }

    fun onSpeakerClicked() {
        _uiState.value = _uiState.value.copy(isSpeakerOn = !_uiState.value.isSpeakerOn)
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
