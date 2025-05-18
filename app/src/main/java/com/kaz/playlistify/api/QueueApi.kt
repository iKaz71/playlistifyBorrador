package com.kaz.playlistify.api

import com.kaz.playlistify.model.CancionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface QueueApi {
    @POST("queue/add")
    suspend fun agregarCancion(
        @Body body: CancionRequest
    ): Response<Unit>
}
