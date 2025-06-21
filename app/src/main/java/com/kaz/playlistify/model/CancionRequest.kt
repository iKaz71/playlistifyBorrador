package com.kaz.playlistify.model

data class CancionRequest(
    val sessionId: String,
    val id: String,
    val titulo: String,
    val usuario: String,
    val thumbnailUrl: String,
    val duration: String,
    val uid: String
)


