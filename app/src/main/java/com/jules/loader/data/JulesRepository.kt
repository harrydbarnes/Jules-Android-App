package com.jules.loader.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.jules.loader.BuildConfig
import com.jules.loader.data.api.JulesService
import com.jules.loader.data.model.Session
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class JulesRepository(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            createEncryptedSharedPreferences()
        } catch (e: Exception) {
            // Handle corrupted or incompatible file (e.g., plain text file exists)
            // Delete the old file and recreate
            deleteSharedPreferences()
            createEncryptedSharedPreferences()
        }
    }

    private fun createEncryptedSharedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun deleteSharedPreferences() {
        try {
            // Clear data if possible first
            context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit().clear().commit()

            // Delete the file
            val file = File(context.filesDir.parent, "shared_prefs/$PREFS_FILE_NAME.xml")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://jules.googleapis.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(JulesService::class.java)

    companion object {
        private const val PREFS_FILE_NAME = "jules_prefs"
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
