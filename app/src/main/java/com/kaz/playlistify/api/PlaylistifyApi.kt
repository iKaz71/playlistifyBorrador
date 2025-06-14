package com.kaz.playlistify.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.kaz.playlistify.model.SessionCreateResponse
import com.kaz.playlistify.model.SessionResponse
import com.kaz.playlistify.model.SessionVerifyResponse
import com.kaz.playlistify.model.VerifyRequest



interface PlaylistifyApi {
    // Métodos de sesión
    @GET("session/{sessionId}")
    suspend fun getSession(@Path("sessionId") sessionId: String): Response<SessionResponse>

    @POST("session/create")
    suspend fun createSession(@Body body: Map<String, String>): Response<SessionCreateResponse>

    @POST("session/verify")
    suspend fun verifyCode(@Body body: VerifyRequest): Response<SessionVerifyResponse>

    // Registrar usuario en sesión
    @POST("session/{sessionId}/user")
    suspend fun registrarUsuario(
        @Path("sessionId") sessionId: String,
        @Body body: RegistrarUsuarioRequest
    ): Response<Unit>

    // Cambiar rol de usuario
    @POST("session/{sessionId}/user/{uid}/role")
    suspend fun cambiarRolUsuario(
        @Path("sessionId") sessionId: String,
        @Path("uid") uid: String,
        @Body body: CambiarRolRequest
    ): Response<CambiarRolResponse>
}

// Data classes:
data class RegistrarUsuarioRequest(
    val uid: String,
    val nombre: String,
    val dispositivo: String,
    val rol: String
)

data class CambiarRolRequest(
    val rol: String,
    val adminUid: String
)

data class CambiarRolResponse(
    val ok: Boolean,
    val message: String
)
