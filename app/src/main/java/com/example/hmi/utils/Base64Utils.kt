package com.example.hmi.utils

import android.util.Base64

object Base64Utils {
    fun encodeToBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.DEFAULT)
    }
}