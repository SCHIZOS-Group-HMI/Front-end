package com.example.hmi.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import com.example.hmi.utils.Base64Utils
import java.io.ByteArrayOutputStream

class ImageData {
    fun captureImage(imageProxy: ImageProxy): String {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 1280, 720, true)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val imageBytes = outputStream.toByteArray()

        return Base64Utils.encodeToBase64(imageBytes)
    }
}