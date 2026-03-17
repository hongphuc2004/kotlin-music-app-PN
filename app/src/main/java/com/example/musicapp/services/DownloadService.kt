package com.example.musicapp.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.musicapp.R
import com.example.musicapp.data.DownloadRepository
import com.example.musicapp.data.local.DownloadedSong
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: DownloadRepository
    private val downloadJobs = mutableMapOf<String, Job>()

    companion object {
        const val ACTION_DOWNLOAD = "ACTION_DOWNLOAD"
        const val ACTION_CANCEL = "ACTION_CANCEL"
        const val EXTRA_SONG_ID = "EXTRA_SONG_ID"
        const val EXTRA_SONG_TITLE = "EXTRA_SONG_TITLE"
        const val EXTRA_SONG_ARTIST = "EXTRA_SONG_ARTIST"
        const val EXTRA_SONG_URL = "EXTRA_SONG_URL"
        const val EXTRA_COVER_URL = "EXTRA_COVER_URL"
        
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID_BASE = 2000
    }

    override fun onCreate() {
        super.onCreate()
        repository = DownloadRepository(this)
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DOWNLOAD -> {
                val songId = intent.getStringExtra(EXTRA_SONG_ID) ?: return START_NOT_STICKY
                val title = intent.getStringExtra(EXTRA_SONG_TITLE) ?: "Unknown"
                val artist = intent.getStringExtra(EXTRA_SONG_ARTIST) ?: "Unknown"
                val url = intent.getStringExtra(EXTRA_SONG_URL) ?: return START_NOT_STICKY
                val coverUrl = intent.getStringExtra(EXTRA_COVER_URL) ?: ""
                
                startDownload(songId, title, artist, url, coverUrl)
            }
            ACTION_CANCEL -> {
                val songId = intent.getStringExtra(EXTRA_SONG_ID)
                songId?.let { cancelDownload(it) }
            }
        }
        return START_STICKY
    }

    private fun startDownload(songId: String, title: String, artist: String, url: String, coverUrl: String) {
        val job = serviceScope.launch {
            try {
                val existing = repository.getDownloadedSong(songId)
                if (existing != null) {
                    showNotification(songId, title, "Already downloaded", 100, true)
                    return@launch
                }

                val downloadsDir = File(getExternalFilesDir(null), "Downloads")
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val fileName = "${songId}.mp3"
                val outputFile = File(downloadsDir, fileName)

                showNotification(songId, title, "Starting download...", 0, false)

                val connection = URL(url).openConnection()
                connection.connect()
                
                val totalSize = connection.contentLength
                var downloadedSize = 0L

                connection.getInputStream().use { input ->
                    FileOutputStream(outputFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedSize += bytesRead

                            val progress = if (totalSize > 0) {
                                (downloadedSize * 100 / totalSize).toInt()
                            } else 0

                            showNotification(songId, title, "Downloading... ${downloadedSize / 1024 / 1024}MB", progress, false)
                        }
                    }
                }

                val downloadedSong = DownloadedSong(
                    songId = songId,
                    title = title,
                    artist = artist,
                    coverImageUrl = coverUrl,
                    localFilePath = outputFile.absolutePath,
                    fileSize = outputFile.length()
                )
                repository.insertDownloadedSong(downloadedSong)

                showNotification(songId, title, "Download complete", 100, true)

                sendBroadcast(Intent("com.example.musicapp.DOWNLOAD_COMPLETE").apply {
                    putExtra("songId", songId)
                })

            } catch (e: Exception) {
                e.printStackTrace()
                showNotification(songId, title, "Download failed: ${e.message}", 0, true)
            } finally {
                downloadJobs.remove(songId)
                if (downloadJobs.isEmpty()) stopSelf()
            }
        }

        downloadJobs[songId] = job
    }

    private fun cancelDownload(songId: String) {
        downloadJobs[songId]?.cancel()
        downloadJobs.remove(songId)
        
        val downloadsDir = File(getExternalFilesDir(null), "Downloads")
        val file = File(downloadsDir, "${songId}.mp3")
        if (file.exists()) file.delete()
    }

    private fun showNotification(songId: String, title: String, message: String, progress: Int, isComplete: Boolean) {
        val notificationId = NOTIFICATION_ID_BASE + songId.hashCode()
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music)  // 👈 Sử dụng ic_music mới tạo
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(!isComplete)
        
        if (!isComplete && progress > 0) {
            builder.setProgress(100, progress, false)
        }

        if (!isComplete) {
            val cancelIntent = Intent(this, DownloadService::class.java).apply {
                action = ACTION_CANCEL
                putExtra(EXTRA_SONG_ID, songId)
            }
            val cancelPendingIntent = PendingIntent.getService(
                this, 0, cancelIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ic_close, "Cancel", cancelPendingIntent)  // 👈 Sử dụng ic_close
        }

        val notification = builder.build()
        
        if (downloadJobs.size == 1 && !isComplete) {  // 👈 Fix: check size thay vì isEmpty
            startForeground(notificationId, notification)
        } else {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music download notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
