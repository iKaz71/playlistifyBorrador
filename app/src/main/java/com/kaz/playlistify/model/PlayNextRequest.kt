package com.kaz.playlistify.model

data class PlayNextRequest(
    val sessionId: String,
    val pushKey: String
)
