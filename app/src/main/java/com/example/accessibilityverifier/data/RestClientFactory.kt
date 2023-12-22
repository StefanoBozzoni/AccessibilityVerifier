package com.example.accessibilityverifier.data

import com.example.accessibilityverifier.ApiService
import retrofit2.Retrofit

object RestClientFactory {
    fun apiServiceClient(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}