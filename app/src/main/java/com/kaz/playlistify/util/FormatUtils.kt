package com.kaz.playlistify.util

fun formatDuration(isoDuration: String): String {
    val regex = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""")
    val matchResult = regex.matchEntire(isoDuration) ?: return "Desconocida"

    val hours = matchResult.groupValues[1].toIntOrNull() ?: 0
    val minutes = matchResult.groupValues[2].toIntOrNull() ?: 0
    val seconds = matchResult.groupValues[3].toIntOrNull() ?: 0

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
