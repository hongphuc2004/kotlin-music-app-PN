package com.example.musicapp.models.songs

import android.util.Log
import com.example.musicapp.models.artists.Artist
import com.google.gson.*
import java.lang.reflect.Type

class SongForArtistDeserializer : JsonDeserializer<SongForArtist> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SongForArtist {

        if (json == null || !json.isJsonObject)
            throw JsonParseException("Invalid SongForArtist JSON")

        val obj = json.asJsonObject

        // Parse artist từ JSON - luôn trả về List<Artist>
        val artistList = parseArtists(obj.get("artist"))

        return SongForArtist(
            _id = obj.get("_id")?.asString ?: "",
            title = obj.get("title")?.asString ?: "",
            artist = artistList,
            album = obj.get("album")?.asString,
            topic = parseStringList(obj.get("topic")),
            fileUrl = obj.get("fileUrl")?.asString,
            coverImage = obj.get("coverImage")?.asString,
            likes = parseStringList(obj.get("likes")),
            lyrics = obj.get("lyrics")?.asString,
            description = obj.get("description")?.asString,
            status = obj.get("status")?.asString,
            deleted = obj.get("deleted")?.asBoolean,
            createdAt = obj.get("createdAt")?.asString,
            updatedAt = obj.get("updatedAt")?.asString
        )
    }


    private fun parseArtists(element: JsonElement?): List<Artist> {
        if (element == null || element.isJsonNull) return emptyList()

        return try {
            when {
                // Case 1: Array
                element.isJsonArray -> {
                    element.asJsonArray.mapNotNull { parseSingleArtist(it) }
                }

                // Case 2: Object
                element.isJsonObject -> {
                    parseSingleArtist(element)?.let { listOf(it) } ?: emptyList()
                }

                // Case 3: String (ex: "A123")
                element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                    listOf(
                        Artist(
                            _id = element.asString,
                            fullName = "Unknown Artist",
                            country = "",
                            coverImage = null
                        )
                    )
                }

                else -> emptyList()
            }
        } catch (e: Exception) {
            Log.e("SongArtist", "Error parsing artists: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseSingleArtist(el: JsonElement?): Artist? {
        if (el == null || !el.isJsonObject) return null

        val obj = el.asJsonObject

        return Artist(
            _id = obj["_id"]?.asString
                ?: obj["id"]?.asString
                ?: "",

            fullName = obj["fullName"]?.asString
                ?: obj["name"]?.asString
                ?: "Unknown Artist",

            country = obj["country"]?.asString ?: "",
            coverImage = obj["coverImage"]?.asString
        )
    }

    private fun parseStringList(el: JsonElement?): List<String> {
        if (el == null || !el.isJsonArray) return emptyList()

        return el.asJsonArray.mapNotNull {
            if (it.isJsonPrimitive) it.asString else null
        }
    }
}
