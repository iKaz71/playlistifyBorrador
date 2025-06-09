package com.kaz.playlistify.model

data class EliminarCancionRequest(
    val sessionId: String,
    val pushKey: String,
    val userId: String = "host"
)
