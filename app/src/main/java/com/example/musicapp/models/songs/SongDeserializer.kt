package com.example.musicapp.models.songs

import android.util.Log
import com.example.musicapp.models.artists.Artist
import com.google.gson.*
import java.lang.reflect.Type

class SongDeserializer : JsonDeserializer<Song> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Song {
        if (json == null || !json.isJsonObject) {
            throw JsonParseException("Invalid song JSON")
        }

        val obj = json.asJsonObject
        
        // Parse artist field (có thể là array hoặc object)
        val artistList = parseArtists(obj.get("artist"), context)
        
        return Song(
            _id = obj.get("_id")?.asString ?: "",
            title = obj.get("title")?.asString ?: "",
            artist = artistList,
            album = obj.get("album")?.asString ?: "",
            topic = parseStringList(obj.get("topic")),
            fileUrl = obj.get("fileUrl")?.asString ?: "",
            coverImage = obj.get("coverImage")?.asString ?: "",
            likes = parseStringList(obj.get("likes")),
            lyrics = obj.get("lyrics")?.asString,
            description = obj.get("description")?.asString,
            status = obj.get("status")?.asString ?: "active",
            deleted = obj.get("deleted")?.asBoolean ?: false,
            createdAt = obj.get("createdAt")?.asString ?: "",
            updatedAt = obj.get("updatedAt")?.asString ?: ""
        )
    }
    
    private fun parseArtists(
        element: JsonElement?,
        context: JsonDeserializationContext?
    ): List<Artist> {
        if (element == null || element.isJsonNull) {
            Log.w("SongDeserializer", "Artist is null")
            return emptyList()
        }
        
        return try {
            when {
                // Case 1: Array của artists
                element.isJsonArray -> {
                    Log.d("SongDeserializer", "Parsing artist as ARRAY, size=${element.asJsonArray.size()}")
                    val list = mutableListOf<Artist>()
                    element.asJsonArray.forEachIndexed { index, item ->
                        try {
                            // Parse từng artist trong array
                            val artist = parseSingleArtist(item, context)
                            if (artist != null) {
                                list.add(artist)
                                Log.d("SongDeserializer", "  [${index}] Parsed: ${artist.fullName}")
                            }
                        } catch (e: Exception) {
                            Log.e("SongDeserializer", "  [${index}] Failed to parse artist: ${e.message}")
                        }
                    }
                    list
                }
                
                // Case 2: Object đơn
                element.isJsonObject -> {
                    Log.d("SongDeserializer", "Parsing artist as OBJECT")
                    val artist = parseSingleArtist(element, context)
                    if (artist != null) listOf(artist) else emptyList()
                }
                
                // Case 3: String (ID)
                element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                    Log.d("SongDeserializer", "Parsing artist as STRING ID")
                    listOf(Artist(
                        _id = element.asString,
                        fullName = "Unknown Artist",
                        country = "",
                        coverImage = null
                    ))
                }
                
                else -> {
                    Log.w("SongDeserializer", "Unknown artist JSON type")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("SongDeserializer", "Error parsing artists: ${e.message}", e)
            emptyList()
        }
    }
    
    // 👇 Helper: Parse từng artist object (xử lý cả nested và flat)
    private fun parseSingleArtist(
        element: JsonElement?,
        context: JsonDeserializationContext?
    ): Artist? {
        if (element == null || element.isJsonNull) return null
        
        return try {
            when {
                // Nếu là object
                element.isJsonObject -> {
                    val obj = element.asJsonObject
                    
                    // Kiểm tra có phải nested artist không (có field "artist" bên trong)
                    val nestedArtist = obj.get("artist")
                    if (nestedArtist != null && nestedArtist.isJsonObject) {
                        Log.d("SongDeserializer", "Found nested artist object")
                        parseSingleArtist(nestedArtist, context)
                    } else {
                        // Parse trực tiếp
                        Artist(
                            _id = obj.get("_id")?.asString 
                                ?: obj.get("id")?.asString 
                                ?: "",
                            fullName = obj.get("fullName")?.asString 
                                ?: obj.get("name")?.asString 
                                ?: "Unknown Artist",
                            country = obj.get("country")?.asString ?: "",
                            coverImage = obj.get("coverImage")?.asString
                        )
                    }
                }
                
                // Nếu là string ID
                element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                    Artist(
                        _id = element.asString,
                        fullName = "Unknown Artist",
                        country = "",
                        coverImage = null
                    )
                }
                
                else -> null
            }
        } catch (e: Exception) {
            Log.e("SongDeserializer", "Error parsing single artist: ${e.message}", e)
            null
        }
    }
    
    private fun parseStringList(element: JsonElement?): List<String> {
        if (element == null || element.isJsonNull) return emptyList()
        if (!element.isJsonArray) return emptyList()
        
        return try {
            element.asJsonArray.mapNotNull { 
                if (it.isJsonPrimitive) it.asString else null 
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
