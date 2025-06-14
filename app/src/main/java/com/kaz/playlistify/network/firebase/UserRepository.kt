package com.kaz.playlistify.network.firebase

import android.util.Log
import com.kaz.playlistify.api.CambiarRolRequest
import com.kaz.playlistify.api.PlaylistifyApi
import com.kaz.playlistify.api.RegistrarUsuarioRequest


object UserRepository {
    suspend fun ascenderAAnfitrionPersistente(
        sessionId: String,
        uid: String,
        api: PlaylistifyApi
    ): Result<String> {
        return try {
            val response = api.cambiarRolUsuario(
                sessionId = sessionId,
                uid = uid,
                body = CambiarRolRequest(
                    rol = "anfitrion_persistente",
                    adminUid = uid // El usuario que hace la petición
                )
            )

            // Debug log: response code y cuerpo
            Log.d("UserRepository", "cambiarRolUsuario code=${response.code()}")

            // Si el response no es exitoso, imprime el error
            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string() ?: "Respuesta no exitosa"
                Log.e("UserRepository", "ErrorBody: $errorMsg")
                return Result.failure(Exception("Error HTTP ${response.code()}: $errorMsg"))
            }

            // Si el body indica que no fue exitoso
            val responseBody = response.body()
            if (responseBody?.ok == true) {
                return Result.success("¡Ya eres anfitrión persistente!")
            } else {
                val backendMsg = responseBody?.message ?: "Respuesta inesperada del backend"
                Log.e("UserRepository", "Mensaje del backend: $backendMsg")
                return Result.failure(Exception(backendMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Excepción al cambiar rol", e)
            return Result.failure(Exception("Excepción: ${e.message}", e))
        }
    }


    suspend fun registrarUsuarioEnSesion(
        sessionId: String,
        uid: String,
        nombre: String,
        dispositivo: String,
        rol: String = "invitado",
        api: PlaylistifyApi
    ): Result<String> {
        return try {
            val response = api.registrarUsuario(  // Esto da error si no existe registrarUsuario en PlaylistifyApi
                sessionId = sessionId,
                body = RegistrarUsuarioRequest(uid, nombre, dispositivo, rol)
            )
            if (response.isSuccessful) {
                Result.success("Usuario registrado en la sesión")
            } else {
                Result.failure(Exception("Error al registrar usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
