package com.kaz.playlistify.model

data class Usuario(
    val nombre: String = "",
    val dispositivo: String = "",
    val rol: String = "invitado",
    val lastSeen: Long = 0L
)
