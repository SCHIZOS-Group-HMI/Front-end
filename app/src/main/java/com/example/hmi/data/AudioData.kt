package com.example.hmi.data

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.hmi.utils.Base64Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.delay
import kotlin.math.abs

@SuppressLint("MissingPermission")
class AudioData {
    private var audioRecord: AudioRecord? = null
    private var bufferSize: Int = 0
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    init {
        initializeAudioRecord()
    }

    private fun initializeAudioRecord() {
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("AudioData", "AudioRecord initialization failed")
                audioRecord = null
            } else {
                Log.d("AudioData", "AudioRecord initialized successfully")
            }
        } catch (e: Exception) {
            Log.e("AudioData", "Error initializing AudioRecord", e)
            audioRecord = null
        }
    }

    suspend fun captureAudio(durationMs: Int = 5000): Pair<String, Double> {
        Log.d("AudioData", "captureAudio called with durationMs=$durationMs")
        if (audioRecord == null || audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.d("AudioData", "AudioRecord not initialized, attempting to reinitialize")
            initializeAudioRecord()
            if (audioRecord == null || audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("AudioData", "AudioRecord reinitialization failed")
                return Pair("", 0.0)
            }
        }

        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteBuffer.allocateDirect(numSamples * 2).order(ByteOrder.nativeOrder())

        try {
            Log.d("AudioData", "Adding 1500ms delay before starting recording")
            delay(1500)
            Log.d("AudioData", "Starting audio recording")
            audioRecord?.startRecording()
            val read = audioRecord?.read(buffer, numSamples * 2) ?: 0
            Log.d("AudioData", "Bytes read from audio: $read")

            if (read <= 0) {
                Log.e("AudioData", "No audio data read: $read")
                audioRecord?.stop()
                return Pair("", 0.0)
            }

            // Tính amplitude và lưu samples
            var sum = 0L
            val samples = ShortArray(read / 2)
            buffer.rewind()
            for (i in samples.indices) {
                samples[i] = buffer.short
                sum += (samples[i].toLong() * samples[i].toLong())
            }
            val amplitude = if (read > 0) Math.sqrt((sum / (read / 2)).toDouble()) else 0.0
            Log.d("AudioData", "Amplitude: $amplitude")

            // Log kiểm tra samples
            val firstFewSamples = samples.take(10).joinToString(",") { it.toString() }
            Log.d("AudioData", "First 10 samples (short): $firstFewSamples")
            if (samples.size >= 110) {
                val samples100to109 = samples.sliceArray(100..109).joinToString(",") { it.toString() }
                Log.d("AudioData", "Samples 100-109: $samples100to109")
            }
            if (samples.size >= 1010) {
                val samples1000to1009 = samples.sliceArray(1000..1009).joinToString(",") { it.toString() }
                Log.d("AudioData", "Samples 1000-1009: $samples1000to1009")
            }

            // Kiểm tra dữ liệu toàn số 0
            val isAllZeros = samples.all { it == 0.toShort() }
            Log.d("AudioData", "Is audio data all zeros? $isAllZeros")
            if (isAllZeros) {
                Log.e("AudioData", "Audio data is all zeros, check microphone or environment")
                audioRecord?.stop()
                return Pair("", 0.0)
            }

            // Kiểm tra tín hiệu mạnh
            val firstNonZeroIndex = samples.indexOfFirst { abs(it.toInt()) > 10 }
            Log.d("AudioData", "First non-zero sample (abs > 10) at index: $firstNonZeroIndex, value: ${if (firstNonZeroIndex >= 0) samples[firstNonZeroIndex] else "N/A"}")
            if (firstNonZeroIndex >= 0 && firstNonZeroIndex + 10 <= samples.size) {
                val samplesAroundNonZero = samples.sliceArray(firstNonZeroIndex until firstNonZeroIndex + 10).joinToString(",") { it.toString() }
                Log.d("AudioData", "Samples $firstNonZeroIndex to ${firstNonZeroIndex + 9}: $samplesAroundNonZero")
            }
            if (firstNonZeroIndex == -1) {
                Log.e("AudioData", "No samples with amplitude > 10, audio signal too weak")
                audioRecord?.stop()
                return Pair("", 0.0)
            }

            // Tạo audioBytes từ samples
            val audioBytes = ByteArray(read)
            val shortBuffer = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            shortBuffer.put(samples)
            shortBuffer.rewind()
            val checkSamples = ShortArray(read / 2)
            shortBuffer.get(checkSamples)
            val samplesMatch = samples.contentEquals(checkSamples)
            Log.d("AudioData", "Samples match after writing to audioBytes: $samplesMatch")

            // Log kiểm tra audioBytes
            val firstFewBytes = audioBytes.take(10).joinToString(",") { it.toString() }
            Log.d("AudioData", "First 10 bytes of audio: $firstFewBytes")
            if (firstNonZeroIndex >= 0) {
                val byteStartIndex = firstNonZeroIndex * 2
                val byteEndIndex = minOf(byteStartIndex + 20, audioBytes.size)
                if (byteStartIndex < audioBytes.size) {
                    val bytesAroundNonZero = audioBytes.sliceArray(byteStartIndex until byteEndIndex).joinToString(",") { it.toString() }
                    Log.d("AudioData", "Bytes ${byteStartIndex} to ${byteEndIndex - 1} (around sample $firstNonZeroIndex): $bytesAroundNonZero")
                }
            }

            // Chuyển PCM thành WAV
            val wavBytes = convertPcmToWav(audioBytes, sampleRate, 1, 16)
            val firstFewWavBytes = wavBytes.take(50).joinToString(",") { it.toString() }
            Log.d("AudioData", "First 50 bytes of WAV: $firstFewWavBytes")
            if (firstNonZeroIndex >= 0) {
                val wavByteStartIndex = 44 + firstNonZeroIndex * 2
                val wavByteEndIndex = minOf(wavByteStartIndex + 20, wavBytes.size)
                if (wavByteStartIndex < wavBytes.size) {
                    val wavBytesAroundNonZero = wavBytes.sliceArray(wavByteStartIndex until wavByteEndIndex).joinToString(",") { it.toString() }
                    Log.d("AudioData", "WAV bytes ${wavByteStartIndex} to ${wavByteEndIndex - 1} (around sample $firstNonZeroIndex): $wavBytesAroundNonZero")
                }
            }

            audioRecord?.stop()
            return Pair(Base64Utils.encodeToBase64(wavBytes), amplitude)
        } catch (e: Exception) {
            Log.e("AudioData", "Error capturing audio", e)
            audioRecord?.stop()
            return Pair("", 0.0)
        }
    }

    private fun convertPcmToWav(pcmData: ByteArray, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
        val dataLength = pcmData.size
        val totalLength = dataLength + 36

        val wavBytes = ByteArray(totalLength + 8)
        val buffer = ByteBuffer.wrap(wavBytes).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(totalLength)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))

        // fmt sub-chunk
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16)
        buffer.putShort(1.toShort())
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * channels * bitsPerSample / 8)
        buffer.putShort((channels * bitsPerSample / 8).toShort())
        buffer.putShort(bitsPerSample.toShort())

        // data sub-chunk
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(dataLength)
        buffer.put(pcmData)

        return wavBytes
    }

    fun release() {
        audioRecord?.release()
        audioRecord = null
        Log.d("AudioData", "AudioRecord released")
    }
}