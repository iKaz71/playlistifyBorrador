package com.kaz.playlistify.network.youtube

import android.util.Log
import com.google.gson.Gson
import com.kaz.playlistify.BuildConfig
import com.kaz.playlistify.model.*
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
        if (query.isBlank()) {
            onError(Exception("La consulta no puede estar vacía"))
            return
        }

        val apiKey = BuildConfig.YOUTUBE_API_KEY
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=5&q=$encodedQuery&key=$apiKey"

        Log.d("YouTubeApi", "URL: $url")

        val request = Request.Builder().url(url).build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val searchResult = gson.fromJson(body, YouTubeSearchResponse::class.java)
                        val videoIds = searchResult.items.map { it.id.videoId }

                        obtenerDetallesDeVideos(searchResult.items, videoIds, onResult, onError)
                    } else {
                        onError(Exception("Error en búsqueda: ${response.code}"))
                    }
                }
            } catch (e: Exception) {
                Log.e("YouTubeApi", "❌ Error en búsqueda", e)
                onError(e)
            }
        }.start()
    }

    private fun obtenerDetallesDeVideos(
        searchItems: List<YouTubeSearchResult>,
        videoIds: List<String>,
        onResult: (List<VideoItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val apiKey = BuildConfig.YOUTUBE_API_KEY
        val ids = videoIds.filterNotNull().joinToString(",")
        val url = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id=$ids&key=$apiKey"

        Log.d("YouTubeApi", "Detalles URL: $url")

        val request = Request.Builder().url(url).build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val detailsResult = gson.fromJson(body, YouTubeVideoDetailsResponse::class.java)

                        val videos = searchItems
                            .filter { it.id.videoId != null }
                            .mapIndexedNotNull { index, item ->
                                val videoId = item.id.videoId ?: return@mapIndexedNotNull null
                                val duration = detailsResult.items.getOrNull(index)?.contentDetails?.duration ?: "PT0S"

                                try {
                                    VideoItem(
                                        id = videoId,
                                        title = item.snippet.title ?: "Sin título",
                                        thumbnailUrl = item.snippet.thumbnails?.default?.url ?: "",
                                        duration = duration
                                    )
                                } catch (e: Exception) {
                                    Log.e("YouTubeApi", "❌ Error al crear VideoItem", e)
                                    null
                                }
                            }

                        onResult(videos)
                    } else {
                        onError(Exception("Error al obtener detalles: ${response.code}"))
                    }
                }
            } catch (e: Exception) {
                Log.e("YouTubeApi", "❌ Error al obtener detalles", e)
                onError(e)
            }
        }.start()
    }

}
