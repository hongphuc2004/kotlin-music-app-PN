package com.example.musicapp.models.chat

data class AIChatMessage(
    val _id: String = "",
    val content: String,
    val sender: String, // "user" or "bot"
    val timestamp: Long = System.currentTimeMillis(),
    val playlistId: String? = null,
    val songs: List<String>? = null
)

data class ChatRequest(
    val message: String,
    val userId: String
)

data class ChatResponse(
    val success: Boolean,
    val message: String,
    val playlist: PlaylistSuggestion?,
    val cached: Boolean = false
)

data class ChatHistoryResponse(
    val success: Boolean,
    val data: List<AIChatMessage>?
)

data class SavePlaylistResponse(
    val success: Boolean,
    val message: String,
    val playlistId: String?
)

data class PlaylistSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val mood: String,
    val songs: List<com.example.musicapp.models.songs.Song>,
    val coverImage: String
)
