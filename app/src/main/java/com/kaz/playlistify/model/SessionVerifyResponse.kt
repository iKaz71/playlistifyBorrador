package com.kaz.playlistify.model

/**
 * Modelo de respuesta al verificar un código de sala.
 */
data class SessionVerifyResponse(
    val valid: Boolean,
    val sessionId: String
)
