package com.example.hmi.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import com.example.hmi.utils.Base64Utils
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class ImageData {
    fun captureImage(imageProxy: ImageProxy): String {
        // Chuyển ImageProxy thành Bitmap
        val bitmap = imageProxy.toBitmap()
        if (bitmap == null) {
            android.util.Log.e("ImageData", "Failed to convert ImageProxy to Bitmap")
            return ""
        }

        // Thay đổi kích thước
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 1280, 720, true)

        // Chuyển Bitmap thành Base64
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val imageBytes = outputStream.toByteArray()

        // Giải phóng tài nguyên
        bitmap.recycle()
        resizedBitmap.recycle()

        return Base64Utils.encodeToBase64(imageBytes)
    }

    // Phương thức chuyển ImageProxy thành Bitmap
    private fun ImageProxy.toBitmap(): Bitmap? {
        if (this.format != ImageFormat.YUV_420_888) {
            android.util.Log.e("ImageData", "Unsupported image format: ${this.format}")
            return null
        }

        val yBuffer: ByteBuffer = this.planes[0].buffer // Y plane
        val uBuffer: ByteBuffer = this.planes[1].buffer // U plane
        val vBuffer: ByteBuffer = this.planes[2].buffer // V plane

        val ySize: Int = yBuffer.remaining()
        val uSize: Int = uBuffer.remaining()
        val vSize: Int = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copy Y plane
        yBuffer.get(nv21, 0, ySize)
        // Copy U và V planes vào định dạng NV21
        var pos = ySize
        for (i in 0 until uSize) {
            nv21[pos++] = vBuffer.get(i) // V
            nv21[pos++] = uBuffer.get(i) // U
        }

        // Chuyển NV21 thành JPEG
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, this.width, this.height), 100, out)
        val imageBytes = out.toByteArray()

        // Giải mã JPEG thành Bitmap
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}