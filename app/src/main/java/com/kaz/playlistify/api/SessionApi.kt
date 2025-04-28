package com.kaz.playlistify.api

import com.kaz.playlistify.model.SessionCreateResponse
import com.kaz.playlistify.model.SessionResponse
import com.kaz.playlistify.model.SessionVerifyResponse
import com.kaz.playlistify.model.VerifyRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SessionApi {
    @GET("session/{sessionId}")
    suspend fun getSession(@Path("sessionId") sessionId: String): Response<SessionResponse>

    @POST("session/create")
    suspend fun createSession(@Body body: Map<String, String>): Response<SessionCreateResponse>

    @POST("session/verify")
    suspend fun verifyCode(@Body body: VerifyRequest): Response<SessionVerifyResponse>
}
