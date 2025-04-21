package com.kaz.playlistify.network.youtube

import android.util.Log
import com.google.gson.Gson
import com.kaz.playlistify.BuildConfig
import com.kaz.playlistify.model.YouTubeSearchResponse
import com.kaz.playlistify.ui.screens.components.VideoItem
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

object YouTubeApi {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun buscarVideos(
        query: String,
        onResult: (List<VideoItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val apiKey = BuildConfig.YOUTUBE_API_KEY
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url =
            "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=5&q=$encodedQuery&key=$apiKey"

        val request = Request.Builder().url(url).build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val result = gson.fromJson(body, YouTubeSearchResponse::class.java)
                        val videos = result.items.map {
                            VideoItem(
                                id = it.id.videoId ?: "",
                                title = it.snippet.title,
                                thumbnailUrl = it.snippet.thumbnails.default.url
                            )
                        }
                        onResult(videos)
                    } else {
                        onError(Exception("Error: ${response.code}"))
                    }
                }
            } catch (e: Exception) {
                Log.e("YouTubeApi", "Error: ${e.message}", e)
                onError(e)
            }
        }.start()
    }
}