package com.example.musicapp.models.songs

import android.util.Log
import com.example.musicapp.models.artists.Artist
import com.google.gson.*;
import java.lang.reflect.Type

class ArtistDeserializer : JsonDeserializer<List<Artist>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<Artist> {
        if (json == null || json.isJsonNull) {
            Log.w("ArtistDeserializer", "JSON is null")
            return emptyList()
        }

        return try {
            when {
                // Case 1: Array of artists
                json.isJsonArray -> {
                    Log.d("ArtistDeserializer", "Parsing ARRAY: ${json.asJsonArray.size()} items")
                    val list = mutableListOf<Artist>()
                    json.asJsonArray.forEach { element ->
                        try {
                            context?.deserialize<Artist>(element, Artist::class.java)?.let {
                                list.add(it)
                            }
                        } catch (e: Exception) {
                            Log.e("ArtistDeserializer", "Failed to parse artist: ${e.message}")
                        }
                    }
                    list
                }
                
                // Case 2: Single artist object
                json.isJsonObject -> {
                    Log.d("ArtistDeserializer", "Parsing OBJECT")
                    try {
                        val artist = context?.deserialize<Artist>(json, Artist::class.java)
                        if (artist != null) listOf(artist) else emptyList()
                    } catch (e: Exception) {
                        Log.e("ArtistDeserializer", "Failed to parse single artist: ${e.message}")
                        emptyList()
                    }
                }
                
                // Case 3: String (artist ID)
                json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                    Log.d("ArtistDeserializer", "Parsing STRING ID: ${json.asString}")
                    listOf(Artist(
                        _id = json.asString,
                        fullName = "Unknown Artist",
                        country = "",
                        coverImage = null
                    ))
                }
                
                else -> {
                    Log.w("ArtistDeserializer", "Unknown JSON type: $json")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("ArtistDeserializer", "Parsing error: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }
}
