package com.example.hmi.viewmodel

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ScanUiState(
    val isScanning: Boolean = false,
    val isMicOn: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val scanResult: String = ""
)

class ScanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    private var audioRecord: AudioRecord? = null
    private var isReadingAudio = false

    fun onScanClicked() {
        Log.d("ScanViewModel", "Scan clicked")
        _uiState.value = _uiState.value.copy(
            isScanning = !_uiState.value.isScanning
        )
        if (!_uiState.value.isScanning) {
            _uiState.value = _uiState.value.copy(
                scanResult = ""
            )
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
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        ).apply {
            try {
                startRecording()
                isReadingAudio = true
                viewModelScope.launch(Dispatchers.IO) {
                    val buffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
                    while (isReadingAudio) {
                        val read = read(buffer, bufferSize)
                        if (read > 0) {
                            buffer.rewind()
                            var sum = 0L
                            for (i in 0 until read / 2) {
                                val sample = buffer.short
                                sum += (sample * sample).toLong()
                            }
                            val amplitude = Math.sqrt((sum / (read / 2)).toDouble())
                            Log.d("ScanViewModel", "Audio amplitude: $amplitude")
                        }
                        buffer.clear()
                    }
                }
                Log.d("ScanViewModel", "Audio capture started")
            } catch (e: Exception) {
                Log.e("ScanViewModel", "Audio capture failed", e)
            }
        }
    }

    private fun stopAudioCapture() {
        isReadingAudio = false
        audioRecord?.apply {
            try {
                stop()
                release()
                Log.d("ScanViewModel", "Audio capture stopped")
            } catch (e: Exception) {
                Log.e("ScanViewModel", "Stop audio capture failed", e)
            }
        }
        audioRecord = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAudioCapture()
    }
}