package com.example.hmi.network

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("prompt")
    val prompt: String
)
