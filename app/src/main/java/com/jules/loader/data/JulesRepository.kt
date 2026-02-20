package com.jules.loader.data

import android.content.Context
import android.content.SharedPreferences
import com.jules.loader.data.api.JulesService
import com.jules.loader.data.model.Session
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class JulesRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("jules_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://jules.googleapis.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(JulesService::class.java)

    companion object {
        private const val KEY_API_KEY = "jules_api_key"
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key).apply()
    }

    fun getApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)
    }

    suspend fun getSessions(): List<Session> {
        val apiKey = getApiKey() ?: throw IllegalStateException("API Key not found")
        // Handle pagination later if needed, for now just get first page
        val response = service.listSessions(apiKey)
        return response.sessions ?: emptyList()
    }
}
