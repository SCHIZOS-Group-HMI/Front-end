package com.example.hmi.network

import com.google.gson.annotations.SerializedName

// Response model sau khi cập nhật để ánh xạ đúng cấu trúc JSON trả về từ server
data class ScanResponse(
    @SerializedName("object_detection")
    val objectDetection: List<ObjectDetectionResult>?,

    @SerializedName("audio_detection")
    val audioDetection: AudioDetectionResponse?
)

data class AudioDetectionResponse(
    /**
     * Ngưỡng threshold mà server sử dụng để lọc kết quả
     */
    @SerializedName("threshold")
    val threshold: Float,

    /**
     * Bản đồ từ label → confidence
     */
    @SerializedName("results")
    val results: Map<String, Float>
)

data class ObjectDetectionResult(
    @SerializedName("class")
    val className: String,

    @SerializedName("confidence")
    val confidence: Float,

    @SerializedName("bbox")
    val bbox: BoundingBox
)

data class BoundingBox(
    @SerializedName("x") val x: Float,
    @SerializedName("y") val y: Float,
    @SerializedName("w") val w: Float,
    @SerializedName("h") val h: Float
)
