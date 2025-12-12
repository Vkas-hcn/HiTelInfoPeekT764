package com.lecture.field.tell.net.demo

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    

    private val adminRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://ertu.devinfocupramstatus.com/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
    

    private val uploadRetrofit: Retrofit by lazy {
        // TODO up url
        Retrofit.Builder()
            .baseUrl("https://test-list.devinfocupramstatus.com/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
    

    val adminApiService: ApiService by lazy {
        adminRetrofit.create(ApiService::class.java)
    }
    

    val uploadApiService: ApiService by lazy {
        uploadRetrofit.create(ApiService::class.java)
    }
}
