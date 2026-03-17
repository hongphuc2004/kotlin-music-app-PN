package com.example.musicapp.ai

import com.example.musicapp.models.songs.Song
import java.util.Locale

data class QueryIntent(
    val type: QueryType,
    val keywords: List<String>,
    val mood: String? = null,
    val genre: String? = null,
    val artist: String? = null
)

enum class QueryType {
    SONG_TITLE,      // "tìm bài shape of you"
    ARTIST,          // "bài hát của ed sheeran"
    MOOD,            // "nhạc buồn", "bài hát vui"
    GENRE,           // "nhạc pop", "rock"
    MIXED            // "bài hát buồn của sơn tùng"
}

class MusicQueryProcessor {

    companion object {
        // Vietnamese mood keywords
        private val MOOD_KEYWORDS = mapOf(
            "buồn" to listOf("buồn", "sad", "melancholic", "tâm trạng", "u sầu"),
            "vui" to listOf("vui", "happy", "upbeat", "sôi động", "náo nhiệt"),
            "lãng mạn" to listOf("lãng mạn", "romantic", "tình yêu", "love", "ngọt ngào"),
            "tĩnh lặng" to listOf("tĩnh lặng", "calm", "thư giãn", "relax", "peaceful"),
            "năng động" to listOf("năng động", "energetic", "mạnh mẽ", "powerful", "gym")
        )

        private val GENRE_KEYWORDS = mapOf(
            "pop" to listOf("pop", "nhạc pop"),
            "rock" to listOf("rock", "nhạc rock"),
            "ballad" to listOf("ballad", "nhạc ballad"),
            "edm" to listOf("edm", "electronic", "dance"),
            "rap" to listOf("rap", "hip-hop", "hiphop")
        )

        // Command keywords
        private val ARTIST_INDICATORS = listOf("của", "by", "ca sĩ", "nghệ sĩ", "singer")
        private val SONG_INDICATORS = listOf("bài", "hát", "song", "nhạc", "tìm", "play", "phát")
    }

    fun processQuery(query: String): QueryIntent {
        val lowerQuery = query.lowercase(Locale.getDefault())
        
        // Detect mood
        val detectedMood = detectMood(lowerQuery)
        
        // Detect genre
        val detectedGenre = detectGenre(lowerQuery)
        
        // Detect if it's artist search
        val isArtistSearch = ARTIST_INDICATORS.any { lowerQuery.contains(it) }
        
        // Extract keywords (remove command words)
        val keywords = extractKeywords(lowerQuery)
        
        // Determine query type
        val queryType = when {
            detectedMood != null && detectedGenre != null -> QueryType.MIXED
            detectedMood != null -> QueryType.MOOD
            detectedGenre != null -> QueryType.GENRE
            isArtistSearch -> QueryType.ARTIST
            else -> QueryType.SONG_TITLE
        }
        
        return QueryIntent(
            type = queryType,
            keywords = keywords,
            mood = detectedMood,
            genre = detectedGenre,
            artist = if (isArtistSearch) keywords.firstOrNull() else null
        )
    }

    private fun detectMood(query: String): String? {
        MOOD_KEYWORDS.forEach { (mood, keywords) ->
            if (keywords.any { query.contains(it) }) {
                return mood
            }
        }
        return null
    }

    private fun detectGenre(query: String): String? {
        GENRE_KEYWORDS.forEach { (genre, keywords) ->
            if (keywords.any { query.contains(it) }) {
                return genre
            }
        }
        return null
    }

    private fun extractKeywords(query: String): List<String> {
        val stopWords = SONG_INDICATORS + ARTIST_INDICATORS + 
                        listOf("tìm", "kiếm", "cho", "tôi", "mình", "một", "bài")
        
        return query.split(" ")
            .filter { it.isNotEmpty() && !stopWords.contains(it) }
    }

    fun filterSongs(songs: List<Song>, intent: QueryIntent): List<Song> {
        return when (intent.type) {
            QueryType.SONG_TITLE -> filterBySongTitle(songs, intent.keywords)
            QueryType.ARTIST -> filterByArtist(songs, intent.keywords)
            QueryType.MOOD -> filterByMood(songs, intent.mood)
            QueryType.GENRE -> filterByGenre(songs, intent.genre)
            QueryType.MIXED -> filterByMixed(songs, intent)
        }
    }

    private fun filterBySongTitle(songs: List<Song>, keywords: List<String>): List<Song> {
        return songs.filter { song ->
            keywords.any { keyword ->
                song.title.contains(keyword, ignoreCase = true)
            }
        }
    }

    private fun filterByArtist(songs: List<Song>, keywords: List<String>): List<Song> {
        return songs.filter { song ->
            keywords.any { keyword ->
                song.artist.any { artist ->
                    artist.fullName.contains(keyword, ignoreCase = true)
                }
            }
        }
    }

    private fun filterByMood(songs: List<Song>, mood: String?): List<Song> {
        if (mood == null) return songs
        
        // Simple mood-based filtering (can be enhanced with ML)
        return when (mood) {
            "buồn" -> songs.filter { 
                it.title.contains("buồn", ignoreCase = true) ||
                it.topic.any { topic -> topic.contains("ballad", ignoreCase = true) }
            }
            "vui" -> songs.filter {
                it.topic.any { topic -> 
                    topic.contains("pop", ignoreCase = true) || 
                    topic.contains("dance", ignoreCase = true)
                }
            }
            else -> songs
        }.ifEmpty { songs.shuffled().take(10) }
    }

    private fun filterByGenre(songs: List<Song>, genre: String?): List<Song> {
        if (genre == null) return songs
        
        return songs.filter { song ->
            song.topic.any { it.contains(genre, ignoreCase = true) }
        }
    }

    private fun filterByMixed(songs: List<Song>, intent: QueryIntent): List<Song> {
        var filtered = songs
        
        intent.mood?.let { mood ->
            filtered = filterByMood(filtered, mood)
        }
        
        intent.genre?.let { genre ->
            filtered = filterByGenre(filtered, genre)
        }
        
        intent.artist?.let {
            filtered = filterByArtist(filtered, intent.keywords)
        }
        
        return filtered
    }
}
