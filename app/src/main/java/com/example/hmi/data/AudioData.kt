package com.example.hmi.data

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.hmi.utils.Base64Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder

@SuppressLint("MissingPermission")
class AudioData {
    private var audioRecord: AudioRecord? = null
    private var bufferSize: Int = 0
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    init {
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )
    }

    fun captureAudio(durationMs: Int = 1000): Pair<String, Double> {
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteBuffer.allocateDirect(numSamples * 2).order(ByteOrder.nativeOrder())

        audioRecord?.startRecording()
        val read = audioRecord?.read(buffer, numSamples * 2) ?: 0

        var sum = 0L
        for (i in 0 until read / 2) {
            val sample = buffer.short
            sum += (sample * sample).toLong()
        }
        val amplitude = Math.sqrt((sum / (read / 2)).toDouble())

        val audioBytes = ByteArray(read)
        buffer.rewind()
        buffer.get(audioBytes)

        audioRecord?.stop()
        return Pair(Base64Utils.encodeToBase64(audioBytes), amplitude)
    }

    fun release() {
        audioRecord?.release()
        audioRecord = null
    }
}