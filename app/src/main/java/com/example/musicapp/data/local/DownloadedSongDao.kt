// ❌ FILE NÀY KHÔNG CÒN DÙNG - ĐÃ CHUYỂN SANG SHAREDPREFERENCES
// XÓA FILE NÀY HOẶC DELETE TOÀN BỘ NỘI DUNG

/*
package com.example.musicapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSongDao {
    
    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getAllDownloadedSongs(): Flow<List<DownloadedSong>>
    
    @Query("SELECT * FROM downloaded_songs WHERE songId = :songId")
    suspend fun getDownloadedSong(songId: String): DownloadedSong?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedSong(song: DownloadedSong)
    
    @Delete
    suspend fun deleteDownloadedSong(song: DownloadedSong)
    
    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    suspend fun deleteDownloadedSongById(songId: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE songId = :songId)")
    suspend fun isDownloaded(songId: String): Boolean
}
*/
