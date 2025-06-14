package com.kaz.playlistify.util

fun generarNombreAleatorio(): String {
    val adj = listOf(
        "Luau", "Lani", "Moana", "Hoku", "Makani", "Nalu", "Pua", "Lilo", "Koa",
        "Leilani", "Paniolo", "Honi", "Laka", "Nohea", "Kailani", "Kai",
        "Aloha", "Keanu", "Mana", "Malie"
    )
    val num = (1000..9999).random()
    return "${adj.random()}$num"
}
