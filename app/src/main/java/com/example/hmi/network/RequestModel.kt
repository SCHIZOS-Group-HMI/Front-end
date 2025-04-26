package com.example.hmi.network

import com.example.hmi.data.Metadata
import com.google.gson.annotations.SerializedName

data class ScanRequest(
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("image") val image: String,
    @SerializedName("audio") val audio: String,
    @SerializedName("audio_amplitude") val audioAmplitude: Double,
    @SerializedName("metadata") val metadata: Metadata
)