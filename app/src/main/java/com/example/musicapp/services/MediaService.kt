package com.example.musicapp.services

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.models.songs.Song

class MediaService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlaylist: List<Song> = emptyList()
    private var currentIndex: Int = 0
    private var currentSong: Song? = null

    inner class LocalBinder : Binder() {
        fun getService(): MediaService = this@MediaService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val url = intent.getStringExtra("SONG_URL") ?: ""
                val title = intent.getStringExtra("SONG_TITLE") ?: ""
                val artist = intent.getStringExtra("SONG_ARTIST") ?: ""
                val cover = intent.getStringExtra("SONG_COVER") ?: ""
                playSong(url, title, artist, cover)
            }
            ACTION_PAUSE -> mediaPlayer?.pause()
            ACTION_RESUME -> mediaPlayer?.start()
            ACTION_NEXT -> playNext()
            ACTION_PREV -> playPrevious()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun playSong(url: String, title: String, artist: String, coverUrl: String) {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepareAsync()

            mediaPlayer?.setOnPreparedListener {
                it.start()
                showNotification(title, artist, coverUrl)
            }

            mediaPlayer?.setOnCompletionListener {
                playNext()
            }
        } catch (e: Exception) {
            Log.e("MediaService", "Error playing song: ${e.message}")
        }
    }

    private fun playNext() {
        if (currentPlaylist.isEmpty()) {
            Log.d("MediaService", "No playlist available")
            return
        }

        if (currentIndex < currentPlaylist.size - 1) {
            currentIndex++
            val nextSong = currentPlaylist[currentIndex]
            currentSong = nextSong
            playSong(
                nextSong.fileUrl,
                nextSong.title,
                nextSong.artist.joinToString(", ") { it.fullName },
                nextSong.coverImage
            )
            sendBroadcast(Intent("com.example.musicapp.SONG_CHANGED"))
        }
    }

    private fun playPrevious() {
        if (currentPlaylist.isEmpty()) {
            Log.d("MediaService", "No playlist available")
            return
        }

        if (currentIndex > 0) {
            currentIndex--
            val prevSong = currentPlaylist[currentIndex]
            currentSong = prevSong
            playSong(
                prevSong.fileUrl,
                prevSong.title,
                prevSong.artist.joinToString(", ") { it.fullName },
                prevSong.coverImage
            )
            sendBroadcast(Intent("com.example.musicapp.SONG_CHANGED"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Player",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Music playback controls"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String, artist: String, coverUrl: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = Intent(this, MediaService::class.java).apply {
            action = ACTION_PREV
        }
        val prevPendingIntent = PendingIntent.getService(
            this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, MediaService::class.java).apply {
            action = ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, MediaService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this, 2, nextIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent) // 👈 Dùng icon mặc định
            .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent) // 👈 Dùng icon mặc định
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent) // 👈 Dùng icon mặc định
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    fun setPlaylist(playlist: List<Song>, startIndex: Int = 0) {
        currentPlaylist = playlist
        currentIndex = startIndex
    }

    fun getCurrentSong(): Song? = currentSong

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val CHANNEL_ID = "music_playback_channel"
        private const val NOTIFICATION_ID = 1

        const val ACTION_PLAY = "com.example.musicapp.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.musicapp.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.musicapp.ACTION_RESUME"
        const val ACTION_NEXT = "com.example.musicapp.ACTION_NEXT"
        const val ACTION_PREV = "com.example.musicapp.ACTION_PREV"
        const val ACTION_STOP = "com.example.musicapp.ACTION_STOP"
    }
}
