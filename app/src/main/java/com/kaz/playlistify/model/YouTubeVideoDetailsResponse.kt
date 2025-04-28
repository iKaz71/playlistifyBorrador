package com.kaz.playlistify.model

data class YouTubeVideoDetailsResponse(
    val items: List<YouTubeVideoDetailsItem>
)

data class YouTubeVideoDetailsItem(
    val id: String,
    val contentDetails: ContentDetails
)

data class ContentDetails(
    val duration: String
)
