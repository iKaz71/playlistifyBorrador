package com.kaz.playlistify.model

data class Cancion(
    val id: String,             // ID del video en YouTube
    val titulo: String,         // Título del video
    val usuario: String,        // Quién lo agregó (nickname o ID)
    val thumbnailUrl: String    // URL del thumbnail
)
