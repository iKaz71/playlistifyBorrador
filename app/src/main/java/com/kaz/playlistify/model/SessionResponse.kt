package com.kaz.playlistify.model

/**
 * Modelo que representa la respuesta del backend para obtener información de una sesión.
 */
data class SessionResponse(
    val code: Int,                        // Código numérico de acceso (como 4 dígitos)
    val host: String,                     // ID de la TV o dispositivo owner
    val hosts: Map<String, Boolean>?,     // Anfitriones (ID de usuario -> true)
    val guests: Map<String, Boolean>?     // Invitados (ID de usuario -> true)
)

/**
 * Modelo para la respuesta al crear una nueva sesión.
 */
data class SessionCreateResponse(
    val sessionId: String,
    val code: Int
)
