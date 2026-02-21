package com.jules.loader.data.api

import com.jules.loader.data.model.*
import retrofit2.http.*

interface JulesService {
    @GET("v1alpha/sessions")
    suspend fun listSessions(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("pageToken") pageToken: String? = null
    ): ListSessionsResponse

    @POST("v1alpha/sessions")
    suspend fun createSession(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Body request: CreateSessionRequest
    ): Session

    @GET("v1alpha/sessions/{sessionId}/activities")
    suspend fun listActivities(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Path("sessionId") sessionId: String
    ): ListActivitiesResponse

    @GET("v1alpha/sources")
    suspend fun listSources(
        @Header("X-Goog-Api-Key") apiKey: String
    ): ListSourcesResponse
}
