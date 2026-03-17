package com.example.musicapp.ui.player

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import com.example.musicapp.R
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.models.songs.Song
import com.example.musicapp.models.songs.LyricLine
import com.example.musicapp.utils.LyricsParser
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: androidx.media3.exoplayer.ExoPlayer
    private lateinit var favoriteRepository: FavoriteSongsRepository
    
    private lateinit var tvSongTitle: TextView
    private lateinit var tvArtistName: TextView
    private lateinit var btnPlayPause: FloatingActionButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnComment: ImageButton  // 👈 Đổi từ LinearLayout thành ImageButton
    private lateinit var btnShare: ImageButton    // 👈 Đổi từ LinearLayout thành ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var tvHeaderTitle: TextView
    
    private var isPlaying = false
    private var isFavorite = false
    private val handler = Handler(Looper.getMainLooper())
    private var updateSeekBarRunnable: Runnable? = null
    private var currentSong: Song? = null
    private var currentLyrics: List<LyricLine> = emptyList()
    private var pagerAdapter: PlayerPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        player = PlayerHolder.player
        favoriteRepository = FavoriteSongsRepository()

        initViews()
        setupListeners()
        setupPlayerListener()
        
        PlayerHolder.currentSong?.let { song ->
            currentSong = song
            updateUI(song)
            
            isPlaying = player.isPlaying
            checkIfFavorite(song._id)
            updatePlayPauseButton(isPlaying)
            
            if (player.duration > 0) {
                seekBar.max = player.duration.toInt()
                tvTotalTime.text = formatTime(player.duration.toInt())
                seekBar.progress = player.currentPosition.toInt()
                tvCurrentTime.text = formatTime(player.currentPosition.toInt())
            }
            
            if (isPlaying) {
                startVinylRotation()
                startSeekBarUpdate()
            }
        }
    }

    private fun initViews() {
        tvSongTitle = findViewById(R.id.tvSongTitle)
        tvArtistName = findViewById(R.id.tvArtistName)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnFavorite = findViewById(R.id.btnFavorite)
        btnBack = findViewById(R.id.btnBack)
        btnComment = findViewById(R.id.btnComment)
        btnShare = findViewById(R.id.btnShare)
        seekBar = findViewById(R.id.seekBar)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnPlayPause.setOnClickListener { togglePlayPause() }
        btnPrevious.setOnClickListener { player.seekToPrevious() }
        btnNext.setOnClickListener { player.seekToNext() }
        btnFavorite.setOnClickListener { toggleFavorite() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) tvCurrentTime.text = formatTime(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopSeekBarUpdate()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    player.seekTo(it.progress.toLong())
                    if (isPlaying) startSeekBarUpdate()
                }
            }
        })

        btnComment.setOnClickListener {
            currentSong?.let { song ->
                CommentsBottomSheet.newInstance(song._id).show(supportFragmentManager, "CommentsBottomSheet")
            }
        }

        btnShare.setOnClickListener {
            currentSong?.let { song ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out: ${song.title} by ${song.artist.joinToString(", ") { it.fullName }}")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Share song via"))
            }
        }

        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener { showSongMenu(it) }
    }

    private fun setupPlayerListener() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                updatePlayPauseButton(playing)
                if (playing) {
                    startVinylRotation()
                    startSeekBarUpdate()
                } else {
                    pauseVinylRotation()
                    stopSeekBarUpdate()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    seekBar.max = player.duration.toInt()
                    tvTotalTime.text = formatTime(player.duration.toInt())
                }
            }
        })
    }

    private fun showSongMenu(view: android.view.View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.song_item_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add_playlist -> { 
                    Toast.makeText(this, "Add to playlist", Toast.LENGTH_SHORT).show()
                    true 
                }
                R.id.action_go_to_artist -> { 
                    currentSong?.let { song ->
                        // TODO: Navigate to artist
                        Toast.makeText(this, "Go to artist", Toast.LENGTH_SHORT).show()
                    }
                    true 
                }
                R.id.action_download -> { 
                    currentSong?.let { song ->
                        // TODO: Download song
                        Toast.makeText(this, "Download", Toast.LENGTH_SHORT).show()
                    }
                    true 
                }
                R.id.action_share -> { 
                    currentSong?.let { song ->
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check out: ${song.title}")
                            type = "text/plain"
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share song via"))
                    }
                    true 
                }
                R.id.action_favorite -> { 
                    toggleFavorite()
                    true 
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun checkIfFavorite(songId: String) {
        lifecycleScope.launch {
            try {
                favoriteRepository.getFavoriteSongs { _, _, favoriteSongIds ->
                    favoriteSongIds?.let {
                        isFavorite = it.contains(songId)
                        runOnUiThread { updateFavoriteButton() }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toggleFavorite() {
        currentSong?.let { song ->
            lifecycleScope.launch {
                try {
                    if (isFavorite) {
                        favoriteRepository.removeFavoriteSong(song._id) { success, _ ->
                            if (success == true) {
                                isFavorite = false
                                runOnUiThread {
                                    updateFavoriteButton()
                                    Toast.makeText(this@PlayerActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        favoriteRepository.addFavoriteSong(song._id) { success, _ ->
                            if (success == true) {
                                isFavorite = true
                                runOnUiThread {
                                    updateFavoriteButton()
                                    Toast.makeText(this@PlayerActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@PlayerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateFavoriteButton() {
        btnFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
        btnFavorite.alpha = if (isFavorite) 1.0f else 0.6f
    }

    private fun updateUI(song: Song) {
        tvSongTitle.text = song.title
        tvArtistName.text = song.artist.joinToString(", ") { it.fullName }
        
        // 👇 Update header title thành tên bài hát
        tvHeaderTitle.text = song.title
        
        loadLyrics(song)
        setupViewPager(song.coverImage)
    }

    private fun setupViewPager(coverUrl: String) {
        pagerAdapter = PlayerPagerAdapter(this, coverUrl, currentLyrics)
        viewPager.adapter = pagerAdapter
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Songs"  // 👈 Đổi từ "Cover" thành "Songs"
                1 -> "Lyrics"
                else -> ""
            }
        }.attach()
    }

    private fun loadLyrics(song: Song) {
        currentLyrics = if (!song.lyrics.isNullOrBlank()) {
            if (song.lyrics.contains("[") && song.lyrics.contains("]")) {
                LyricsParser.parseLRC(song.lyrics)
            } else {
                LyricsParser.parsePlainText(song.lyrics)
            }
        } else {
            emptyList()
        }
        
        android.util.Log.d("PlayerActivity", "Loaded ${currentLyrics.size} lyric lines")
    }

    private fun togglePlayPause() {
        if (isPlaying) player.pause() else player.play()
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }

    // ✅ Chỉ giữ 1 startVinylRotation() cho Fragment
    private fun startVinylRotation() {
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is AlbumCoverFragment) {
                fragment.startVinylRotation()
            }
        }
    }

    // ✅ Chỉ giữ 1 pauseVinylRotation() cho Fragment
    private fun pauseVinylRotation() {
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is AlbumCoverFragment) {
                fragment.pauseVinylRotation()
            }
        }
    }

    private fun startSeekBarUpdate() {
        if (updateSeekBarRunnable == null) {
            updateSeekBarRunnable = object : Runnable {
                override fun run() {
                    if (isPlaying && player.duration > 0) {
                        val position = player.currentPosition.toInt()
                        
                        if (!seekBar.isPressed) {
                            seekBar.max = player.duration.toInt()
                            seekBar.progress = position
                            tvCurrentTime.text = formatTime(position)
                            tvTotalTime.text = formatTime(player.duration.toInt())
                        }
                        
                        updateLyricsPosition(player.currentPosition)
                        handler.postDelayed(this, 50)
                    }
                }
            }
        }
        updateSeekBarRunnable?.let { handler.post(it) }
    }

    private fun updateLyricsPosition(positionMs: Long) {
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is LyricsFragment && fragment.isVisible) {
                fragment.updatePosition(positionMs)
            }
        }
    }

    private fun stopSeekBarUpdate() {
        updateSeekBarRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSeekBarUpdate()
    }

    override fun onResume() {
        super.onResume()
        PlayerHolder.currentSong?.let { song ->
            currentSong = song
            updateUI(song)
            checkIfFavorite(song._id)
        }
        isPlaying = player.isPlaying
        updatePlayPauseButton(isPlaying)
        if (isPlaying) {
            startVinylRotation()
            startSeekBarUpdate()
        }
    }
}
