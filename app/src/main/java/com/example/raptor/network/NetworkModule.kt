package com.example.raptor.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network Module - Configureert Retrofit en OkHttp
 * Komt overeen met iOS APIService configuratie
 */
object NetworkModule {
    
    // Base URL - wordt automatisch gedetecteerd (zoals iOS)
    // Voor Android Emulator: 10.0.2.2 = localhost
    // Voor fysiek apparaat: gebruik je computer's IP-adres (bijv. 192.168.1.x)
    // 
    // XAMPP moet draaien met Apache + MySQL
    // Backend bestanden in: C:\xampp\htdocs\raptor\Backend\
    //
    private const val DEFAULT_BASE_URL = "http://10.0.2.2/raptor/Backend/" // Android Emulator
    // private const val DEFAULT_BASE_URL = "http://192.168.1.100/raptor/Backend/" // Fysiek apparaat - vervang met jouw IP
    
    // Timeout (komt overeen met iOS: 10 seconden)
    private const val TIMEOUT_SECONDS = 10L
    
    private var baseUrl: String = DEFAULT_BASE_URL
    
    /**
     * Set base URL (voor server detectie)
     */
    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/') + "/"
        // Recreate retrofit instance
        retrofit = createRetrofit()
        apiService = retrofit.create(RaptorApiService::class.java)
    }
    
    /**
     * Get current base URL
     */
    fun getBaseUrl(): String = baseUrl
    
    /**
     * Create Gson instance
     */
    private fun createGson(): Gson {
        return GsonBuilder()
            .setLenient() // Voor PHP responses die niet altijd perfect JSON zijn
            .setDateFormat("yyyy-MM-dd HH:mm:ss") // PHP date format
            .create()
    }
    
    /**
     * Create OkHttpClient with logging
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // In debug mode
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Create Retrofit instance
     */
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }
    
    // Lazy initialization
    private var retrofit: Retrofit = createRetrofit()
    var apiService: RaptorApiService = retrofit.create(RaptorApiService::class.java)
}

