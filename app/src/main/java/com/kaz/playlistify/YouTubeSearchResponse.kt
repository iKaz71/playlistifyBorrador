package com.kaz.playlistify

data class YouTubeSearchResponse(
    val items: List<Item>
)

data class Item(
    val id: VideoId,
    val snippet: Snippet
)

data class VideoId(val videoId: String?)
data class Snippet(
    val title: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val default: Thumbnail
)

data class Thumbnail(val url: String)
