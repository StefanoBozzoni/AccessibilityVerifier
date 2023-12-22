package com.example.accessibilityverifier.data

import com.example.accessibilityverifier.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {
    var sharedRetrofit: Retrofit? = null
    fun getRetrofitClient(): Retrofit {

        if (sharedRetrofit == null) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            sharedRetrofit= retrofit
        }

        return  sharedRetrofit!!
    }
}