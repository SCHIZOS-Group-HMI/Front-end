package com.example.hmi.data

import com.google.gson.annotations.SerializedName

data class Metadata(
    @SerializedName("sample_rate") val sampleRate: Int = 44100,
    @SerializedName("channels") val channels: Int = 1,
    @SerializedName("audio_format") val audioFormat: String = "PCM_16BIT",
    @SerializedName("image_format") val imageFormat: String = "JPEG",
    @SerializedName("resolution") val resolution: String = "1280x720"
)