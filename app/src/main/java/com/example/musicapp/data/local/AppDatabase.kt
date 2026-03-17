// ❌ FILE NÀY KHÔNG CÒN DÙNG - ĐÃ CHUYỂN SANG SHAREDPREFERENCES
// XÓA FILE NÀY HOẶC DELETE TOÀN BỘ NỘI DUNG

/*
package com.example.musicapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DownloadedSong::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun downloadedSongDao(): DownloadedSongDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
*/
