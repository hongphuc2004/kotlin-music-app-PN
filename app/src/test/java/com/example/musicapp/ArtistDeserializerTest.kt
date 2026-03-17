package com.example.musicapp

import com.example.musicapp.models.artists.Artist
import com.example.musicapp.models.songs.ArtistDeserializer
import com.example.musicapp.models.songs.Song
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Test
import org.junit.Assert.*

class ArtistDeserializerTest {
    
    @Test
    fun testArrayArtist() {
        val json = """
        {
            "_id": "1",
            "title": "Test Song",
            "artist": [{"id": "a1", "fullName": "Artist 1"}]
        }
        """
        
        val gson = GsonBuilder()
            .registerTypeAdapter(
                object : TypeToken<List<Artist>>() {}.type,
                ArtistDeserializer()
            )
            .create()
        
        val song = gson.fromJson(json, Song::class.java)
        assertEquals(1, song.artist.size)
        assertEquals("Artist 1", song.artist[0].fullName)
    }
    
    @Test
    fun testObjectArtist() {
        val json = """
        {
            "_id": "2",
            "title": "Test Song 2",
            "artist": {"id": "a2", "fullName": "Artist 2"}
        }
        """
        
        val gson = GsonBuilder()
            .registerTypeAdapter(
                object : TypeToken<List<Artist>>() {}.type,
                ArtistDeserializer()
            )
            .create()
        
        val song = gson.fromJson(json, Song::class.java)
        assertEquals(1, song.artist.size)
        assertEquals("Artist 2", song.artist[0].fullName)
    }
}
