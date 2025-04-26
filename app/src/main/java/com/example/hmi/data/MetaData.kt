package com.example.hmi.data

data class Metadata(
    val sampleRate: Int = 44100,
    val channels: Int = 1,
    val audioFormat: String = "PCM_16BIT",
    val imageFormat: String = "JPEG",
    val resolution: String = "1280x720"
)