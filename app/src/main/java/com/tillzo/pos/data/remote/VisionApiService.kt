package com.tillzo.pos.data.remote

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * M6.3 - AI Vision API Fallback
 * Retrofit interface to send compressed images to a cloud Vision API 
 * if local ML Kit OCR fails to extract the required data.
 */
interface VisionApiService {
    
    @Multipart
    @POST("vision/analyze") // Placeholder endpoint, assuming the backend has this
    suspend fun analyzeImage(
        @Part image: MultipartBody.Part,
        @Part("prompt") prompt: okhttp3.RequestBody
    ): Response<VisionApiResponse>
}

data class VisionApiResponse(
    val product_title: String,
    val weight: String,
    val suggested_price: Double
)
