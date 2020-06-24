package com.ryeslim.confuciusretrofitcoroutines.retrofit

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ServiceFactory {
    companion object {
        fun <T> createRetrofitService(
            clazz: Class<T>,
            baseURL: String): T {
            val httpClient = OkHttpClient.Builder()
            httpClient.readTimeout(20, TimeUnit.SECONDS)
            httpClient.connectTimeout(20, TimeUnit.SECONDS)
            val client = httpClient.build()
            val restAdapter = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .baseUrl(baseURL)
                .client(client)
                .build()
            return restAdapter.create(clazz)
        }
    }
}