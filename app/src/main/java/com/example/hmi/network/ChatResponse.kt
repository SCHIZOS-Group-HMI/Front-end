package com.example.hmi.network

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @SerializedName("chat_response")
    val reply: String
)
