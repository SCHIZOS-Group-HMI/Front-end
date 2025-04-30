package com.example.hmi.network

import com.google.gson.annotations.SerializedName

// Trong file RequestModel.kt hoặc một file mới
data class ScanResponse(
    @SerializedName("object_detection") val objectDetection: List<ObjectDetectionResult>?,
    @SerializedName("audio_detection") val audioDetection: Map<String, Float>?
)

data class ObjectDetectionResult(
    @SerializedName("class") val className: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("bbox") val bbox: BoundingBox
)

data class BoundingBox(
    @SerializedName("x") val x: Float,
    @SerializedName("y") val y: Float,
    @SerializedName("w") val w: Float,
    @SerializedName("h") val h: Float
)