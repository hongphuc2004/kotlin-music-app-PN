package com.example.musicapp.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicapp.R
import com.example.musicapp.ui.player.PlayerHolder

class MusicService : Service() {

    private val player: ExoPlayer
        get() = PlayerHolder.player

    private var currentSongTitle: String = "PN Music"
    private var currentSongArtist: String = "Now playing..."
    private var currentSongCover: String? = null
    
    // Queue management
    private var songQueue: MutableList<String> = mutableListOf()
    private var currentIndex: Int = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val p = kotlin.runCatching { PlayerHolder.player }.getOrElse {
            PlayerHolder.player = ExoPlayer.Builder(this).build().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    true
                )
                setWakeMode(C.WAKE_MODE_NETWORK)
            }
            PlayerHolder.player
        }

        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                showMusicNotification()
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                mediaMetadata.title?.let { currentSongTitle = it.toString() }
                mediaMetadata.artist?.let { currentSongArtist = it.toString() }
                showMusicNotification()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "music_channel",
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for music playback controls"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    private fun getCurrentAlbumArt(): Bitmap? {
        // TODO: thay bằng ảnh tải từ network nếu bạn có coverImage
        return BitmapFactory.decodeResource(resources, R.drawable.ic_default_album_art)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val songTitleFromIntent = intent?.getStringExtra("SONG_TITLE")
        val songArtistFromIntent = intent?.getStringExtra("SONG_ARTIST")
        val songUrl = intent?.getStringExtra("SONG_URL")
        val songCover = intent?.getStringExtra("SONG_COVER")

        when (action) {
            MusicActions.ACTION_PLAY -> {
                if (!songUrl.isNullOrEmpty()) {
                    currentSongTitle = songTitleFromIntent ?: "Unknown"
                    currentSongArtist = songArtistFromIntent ?: "Unknown"
                    currentSongCover = songCover
                    playNewSong(songUrl, songTitleFromIntent, songArtistFromIntent)
                } else {
                    if (!player.isPlaying) player.play()
                }
            }
            MusicActions.ACTION_PAUSE -> {
                if (player.isPlaying) player.pause()
            }
            MusicActions.ACTION_NEXT -> {
                playNext()
            }
            MusicActions.ACTION_PREV -> {
                playPrevious()
            }
            else -> {
                if (!songUrl.isNullOrEmpty()) {
                    currentSongTitle = songTitleFromIntent ?: "Unknown"
                    currentSongArtist = songArtistFromIntent ?: "Unknown"
                    currentSongCover = songCover
                    playNewSong(songUrl, songTitleFromIntent, songArtistFromIntent)
                } else {
                    showMusicNotification()
                }
            }
        }

        showMusicNotification()
        return START_STICKY
    }

    private fun playNext() {
        // Broadcast to app to handle next song
        sendBroadcast(Intent("com.example.musicapp.ACTION_NEXT_SONG"))
    }

    private fun playPrevious() {
        // Broadcast to app to handle previous song
        sendBroadcast(Intent("com.example.musicapp.ACTION_PREV_SONG"))
    }

    private fun playNewSong(url: String, title: String?, artist: String?) {
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title ?: "Unknown")
                    .setArtist(artist ?: "Unknown")
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        // metadata sẽ trigger onMediaMetadataChanged -> update notification
    }

    private fun showMusicNotification() {
        val isPlayingNow = kotlin.runCatching { player.isPlaying }.getOrDefault(false)

        // Previous
        val prevIntent = Intent(this, MusicControlReceiver::class.java).apply {
            action = MusicActions.ACTION_PREV
        }
        val prevPendingIntent = PendingIntent.getBroadcast(
            this, 123, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next
        val nextIntent = Intent(this, MusicControlReceiver::class.java).apply {
            action = MusicActions.ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            this, 124, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play / Pause
        val playPauseIntent = Intent(this, MusicControlReceiver::class.java).apply {
            action = if (isPlayingNow) MusicActions.ACTION_PAUSE else MusicActions.ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getBroadcast(
            this, 125, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlayingNow) R.drawable.ic_pause else R.drawable.ic_play

        val notificationBuilder = NotificationCompat.Builder(this, "music_channel")
            .setContentTitle(currentSongTitle)
            .setContentText(currentSongArtist)
            .setSmallIcon(R.drawable.ic_default_album_art)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlayingNow)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(MediaStyle().setShowActionsInCompactView(0, 1, 2))
            .addAction(R.drawable.ic_prev, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, "Play/Pause", playPausePendingIntent)
            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)

        // Load album art if available
        if (!currentSongCover.isNullOrEmpty()) {
            // TODO: Load from URL using Glide/Coil
        }

        startForeground(1, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
