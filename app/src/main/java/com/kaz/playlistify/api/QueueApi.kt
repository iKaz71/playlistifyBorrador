package com.kaz.playlistify.api

import com.kaz.playlistify.model.CancionRequest
import com.kaz.playlistify.model.EliminarCancionRequest
import com.kaz.playlistify.model.PlayNextRequest
import com.kaz.playlistify.model.PlayNextResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface QueueApi {
    @POST("queue/add")
    suspend fun agregarCancion(
        @Body body: CancionRequest
    ): Response<Unit>

    @POST("queue/playnext")
    suspend fun playNext(
        @Body body: PlayNextRequest
    ): Response<PlayNextResponse>

    @POST("queue/remove")
    suspend fun eliminarCancion(
        @Body body: EliminarCancionRequest
    ): Response<Unit>


}
