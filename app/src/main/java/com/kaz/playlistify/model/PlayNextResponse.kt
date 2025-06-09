package com.kaz.playlistify.model

data class PlayNextResponse(
    val ok: Boolean,
    val message: String,
    val newOrder: List<String>? = null
)
