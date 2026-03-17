package com.example.musicapp.models.artists

import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName(value = "_id", alternate = ["id"]) // 👈 Chấp nhận cả "_id" và "id"
    val _id: String = "",

    @SerializedName(value = "fullName", alternate = ["name"])
    val fullName: String = "Unknown Artist",
    
    @SerializedName("country")
    val country: String = "",
    
    @SerializedName("coverImage")
    val coverImage: String? = null
) {
    // Helper property để lấy ID dù là "_id" hay "id"
    val artistId: String
        get() = _id ?: ""
}