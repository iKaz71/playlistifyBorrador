package com.kaz.playlistify.model


data class RegistrarUsuarioRequest(
    val uid: String,
    val nombre: String,
    val dispositivo: String,
    val rol: String
)
