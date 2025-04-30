package com.example.hmi.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/scan")
    suspend fun sendScanData(@Body request: ScanRequest): Response<ScanResponse>
}