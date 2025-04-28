package com.kaz.playlistify.model

data class YouTubeSearchResponse(
    val items: List<YouTubeSearchResult>
)

data class YouTubeSearchResult(
    val id: YouTubeSearchResultId,
    val snippet: Snippet
)

data class YouTubeSearchResultId(
    val videoId: String
)

data class Snippet(
    val title: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val default: Thumbnail
)

data class Thumbnail(
    val url: String
)
