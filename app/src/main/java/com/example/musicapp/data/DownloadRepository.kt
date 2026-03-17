package com.example.musicapp.data

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.musicapp.data.local.DownloadedSong
import com.example.musicapp.models.songs.Song
import com.example.musicapp.services.DownloadService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class DownloadRepository(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("downloads", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val DOWNLOADS_KEY = "downloaded_songs_json"
    
    fun getAllDownloadedSongs(): Flow<List<DownloadedSong>> = flow {
        val json = prefs.getString(DOWNLOADS_KEY, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<DownloadedSong>>() {}.type
                val songs: List<DownloadedSong> = gson.fromJson(json, type)
                emit(songs)
            } catch (e: Exception) {
                emit(emptyList())
            }
        } else {
            emit(emptyList())
        }
    }
    
    suspend fun isDownloaded(songId: String): Boolean {
        val json = prefs.getString(DOWNLOADS_KEY, null) ?: return false
        return try {
            val type = object : TypeToken<List<DownloadedSong>>() {}.type
            val songs: List<DownloadedSong> = gson.fromJson(json, type)
            songs.any { it.songId == songId }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getDownloadedSong(songId: String): DownloadedSong? {
        val json = prefs.getString(DOWNLOADS_KEY, null) ?: return null
        return try {
            val type = object : TypeToken<List<DownloadedSong>>() {}.type
            val songs: List<DownloadedSong> = gson.fromJson(json, type)
            songs.find { it.songId == songId }
        } catch (e: Exception) {
            null
        }
    }
    
    fun insertDownloadedSong(song: DownloadedSong) {
        val json = prefs.getString(DOWNLOADS_KEY, null)
        val songs = if (json != null) {
            try {
                val type = object : TypeToken<MutableList<DownloadedSong>>() {}.type
                gson.fromJson<MutableList<DownloadedSong>>(json, type)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
        
        songs.removeAll { it.songId == song.songId }
        songs.add(song)
        
        prefs.edit().putString(DOWNLOADS_KEY, gson.toJson(songs)).apply()
    }
    
    fun startDownload(song: Song) {
        val intent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_DOWNLOAD
            putExtra(DownloadService.EXTRA_SONG_ID, song._id)
            putExtra(DownloadService.EXTRA_SONG_TITLE, song.title)
            putExtra(DownloadService.EXTRA_SONG_ARTIST, song.artist.joinToString(", ") { it.fullName })
            putExtra(DownloadService.EXTRA_SONG_URL, song.fileUrl)
            putExtra(DownloadService.EXTRA_COVER_URL, song.coverImage)
        }
        ContextCompat.startForegroundService(context, intent)
    }
    
    suspend fun deleteDownload(songId: String) {
        val json = prefs.getString(DOWNLOADS_KEY, null) ?: return
        try {
            val type = object : TypeToken<MutableList<DownloadedSong>>() {}.type
            val songs: MutableList<DownloadedSong> = gson.fromJson(json, type)
            
            val song = songs.find { it.songId == songId }
            if (song != null) {
                val file = File(song.localFilePath)
                if (file.exists()) file.delete()
                
                songs.removeAll { it.songId == songId }
                prefs.edit().putString(DOWNLOADS_KEY, gson.toJson(songs)).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun deleteAllDownloads() {
        val downloadsDir = File(context.getExternalFilesDir(null), "Downloads")
        if (downloadsDir.exists()) {
            downloadsDir.listFiles()?.forEach { it.delete() }
        }
        prefs.edit().remove(DOWNLOADS_KEY).apply()
    }
}
