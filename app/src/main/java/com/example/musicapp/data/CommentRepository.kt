package com.example.musicapp.data

import android.util.Log
import com.example.musicapp.models.AddCommentRequest
import com.example.musicapp.models.AddCommentResponse
import com.example.musicapp.models.Comment
import com.example.musicapp.models.CommentResponse
import com.example.musicapp.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.*

interface CommentApiService {
    @GET("comments/{songId}")
    suspend fun getComments(@Path("songId") songId: String): Response<CommentResponse>
    
    @POST("comments")
    suspend fun addComment(@Body request: AddCommentRequest): Response<AddCommentResponse>
    
    @DELETE("comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: String): Response<AddCommentResponse>
    
    @POST("comments/{commentId}/like")
    suspend fun likeComment(@Path("commentId") commentId: String): Response<AddCommentResponse>
}

class CommentRepository {
    private val apiService = ApiClient.api
    
    suspend fun getComments(songId: String): Result<List<Comment>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getComments(songId)
            
            // Log raw response body
            val rawBody = response.body()
            Log.d("CommentRepository", "Raw response: $rawBody")
            Log.d("CommentRepository", "Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    // Log each comment's userId
                    body.comments.forEach { comment ->
                        Log.d("CommentRepository", "Comment ${comment._id}:")
                        Log.d("CommentRepository", "  userId: ${comment.userId}")
                        Log.d("CommentRepository", "  userId._id: ${comment.userId?._id}")
                        Log.d("CommentRepository", "  userId.fullName: ${comment.userId?.fullName}")
                        Log.d("CommentRepository", "  userId.avatar: ${comment.userId?.avatar}")
                    }
                    Result.success(body.comments)
                } else {
                    Result.failure(Exception("API returned success=false or body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CommentRepository", "Error body: $errorBody")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("CommentRepository", "Exception: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun addComment(songId: String, content: String): Result<Comment> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.addComment(AddCommentRequest(songId, content))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.comment != null) {
                    Result.success(body.comment!!)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to add comment"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun deleteComment(commentId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteComment(commentId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun likeComment(commentId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.likeComment(commentId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to like comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
