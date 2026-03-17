package com.example.musicapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicapp.data.SongRepository
import com.example.musicapp.models.songs.Song

class SongViewModel : ViewModel() {
    private val repo = SongRepository()

    private val _songs = MutableLiveData<List<Song>>(emptyList())
    val songs: LiveData<List<Song>> = _songs

    private val _filteredSongs = MutableLiveData<List<Song>>(emptyList())
    val filteredSongs: LiveData<List<Song>> = _filteredSongs

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Hàm cũ: load tất cả bài hát
    fun loadSongs() {
        repo.fetchSongs { list, err ->
            if (list != null) {
                _songs.postValue(list)
                _filteredSongs.postValue(list)
            }
            if (err != null) _error.postValue(err)
        }
    }

    fun fetchSongs(query: String? = null) {
        repo.fetchSongs { list, err ->
            if (list != null) {
                _songs.postValue(list)
                if (!query.isNullOrEmpty()) {
                    val filtered = list.filter { song ->
                        song.title.contains(query, true) ||
                        song.album.contains(query, true) ||
                        song.artist.any { it.fullName.contains(query, true) }
                    }
                    _filteredSongs.postValue(filtered)
                } else {
                    _filteredSongs.postValue(list)
                }
            }
            if (err != null) _error.postValue(err)
        }
    }

    // Để search realtime
    fun updateFilteredSongs(newList: List<Song>) {
        _filteredSongs.postValue(newList)
    }
}
