package com.jules.loader.data.api

import com.jules.loader.data.model.ListSessionsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface JulesService {
    @GET("v1alpha/sessions")
    suspend fun listSessions(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("pageToken") pageToken: String? = null
    ): ListSessionsResponse
}
