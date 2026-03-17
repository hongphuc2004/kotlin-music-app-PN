package com.example.musicapp.network

import com.example.musicapp.models.artists.ArtistDetailResponse
import com.example.musicapp.models.artists.ArtistResponse
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.auth.LoginRequest
import com.example.musicapp.models.auth.SignUpRequest
import com.example.musicapp.models.playlists.AddToPlaylistRequest
import com.example.musicapp.models.playlists.AddToPlaylistResponse
import com.example.musicapp.models.playlists.CreatePlaylistRequest
import com.example.musicapp.models.playlists.CreatePlaylistResponse
import com.example.musicapp.models.playlists.PlaylistDetailResponse
import com.example.musicapp.models.playlists.PlaylistResponse
import com.example.musicapp.models.songs.ApiListResponse
import com.example.musicapp.models.favorites.FavoriteSongsResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.models.songs.SongListResponse
import com.example.musicapp.models.songs.SongResponse
import com.example.musicapp.models.users.ChangePasswordRequest
import com.example.musicapp.models.users.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import com.example.musicapp.models.AddCommentRequest
import com.example.musicapp.models.AddCommentResponse
import com.example.musicapp.models.CommentResponse
import com.example.musicapp.models.chat.ChatHistoryResponse
import com.example.musicapp.models.topic.TopicResponse
import com.example.musicapp.models.chat.ChatRequest
import com.example.musicapp.models.chat.ChatResponse
import com.example.musicapp.models.chat.AIChatMessage
import com.example.musicapp.models.chat.PlaylistSuggestion
import com.example.musicapp.models.chat.SavePlaylistResponse

interface ApiService {
    // Authentication
    @POST("auth/sign-up")
    fun signUp(@Body request: SignUpRequest): Call<ApiResponse>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse>
    @POST("auth/logout")
    fun logout(): Call<ApiResponse>

    // User profile
    @GET("user/me")
    fun getUserProfile(): Call<UserResponse>
    @Multipart
    @PUT("user/me")
    fun updateMe(
        @Part avatar: MultipartBody.Part?,
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody
    ): Call<UserResponse>

    @PUT("user/me/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<UserResponse>

    // Song
    @GET("/music/songs")
    fun getSongs(): Call<ApiListResponse<Song>>
    @GET("/music/songs/{id}")
    fun getSongDetail(@Path("id") id: String): Call<SongResponse>

    // Playlist
    @GET("user/me/playlists")
    suspend fun getMyPlaylists(): PlaylistResponse

    @POST("playlist/create-playlist")
    suspend fun createPlaylist(@Body body: CreatePlaylistRequest): CreatePlaylistResponse

    @PATCH("playlist/add-playlist")
    suspend fun addToPlaylist(@Body body: AddToPlaylistRequest): Response<AddToPlaylistResponse>

    @GET("playlist/{id}")
    fun getPlaylistDetail(@Path("id") playlistId: String): Call<PlaylistDetailResponse>

    // Artist
    @GET("music/artists")
    fun getHotArtists(): Call<ArtistResponse>

    @GET("music/random")
    fun getSuggestedSongs(): Call<SongListResponse>

    // Favorite Songs API
    @GET("favorite-songs")
    fun getFavoriteSongs(): Call<FavoriteSongsResponse>

    @GET("favorite-songs/{id}")
    fun getFavoriteSongById(@Path("id") songId: String): Call<SongResponse>

    @POST("favorite-songs/{id}")
    fun addFavoriteSong(@Path("id") songId: String): Call<ApiResponse>

    @DELETE("favorite-songs/{id}")
    fun removeFavoriteSong(@Path("id") songId: String): Call<ApiResponse>

    @DELETE("favorite-songs")
    fun removeAllFavoriteSongs(): Call<ApiResponse>

    // Artist Detail
    @GET("artist/{id}")
    fun getArtistDetail(@Path("id") id: String): Call<ArtistDetailResponse>

    // Comment endpoints
    @GET("comments/{songId}")
    suspend fun getComments(@Path("songId") songId: String): Response<CommentResponse>

    @POST("comments")
    suspend fun addComment(@Body request: AddCommentRequest): Response<AddCommentResponse>

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: String): Response<AddCommentResponse>

    @POST("comments/{commentId}/like")
    suspend fun likeComment(@Path("commentId") commentId: String): Response<AddCommentResponse>

    // Topics
    @GET("music/topics")
    fun getTopics(): Call<TopicResponse>

    // Chat
    @POST("api/chat/message")
    suspend fun sendChatMessage(@Body request: ChatRequest): ChatResponse

    @GET("api/chat/history")
    suspend fun getChatHistory(): ChatHistoryResponse

    @POST("api/chat/playlist/save")
    suspend fun saveSuggestedPlaylist(@Body playlist: PlaylistSuggestion): SavePlaylistResponse
}