package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.musicapp.models.songs.Song
import com.example.musicapp.services.MediaService
import com.example.musicapp.ui.home.HomeFragment
import com.example.musicapp.ui.library.LibraryFragment
import com.example.musicapp.ui.player.MiniPlayerFragment
import com.example.musicapp.ui.player.PlayerViewModel
import com.example.musicapp.ui.search.SearchFragment
import com.example.musicapp.utils.PreferenceHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val playerVM: PlayerViewModel by viewModels()

    private fun applyUiFor(fragment: Fragment?) {
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        when (fragment) {
            is HomeFragment, is SearchFragment, is com.example.musicapp.ui.chat.AIChatFragment, is LibraryFragment -> {
                bottom.visibility = View.VISIBLE
            }
            else -> {
                bottom.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        supportFragmentManager.addOnBackStackChangedListener {
            val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            applyUiFor(current)
        }

        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), "HOME")
        }

        if (intent.getBooleanExtra("NAVIGATE_HOME", false)) {
            loadFragment(HomeFragment(), "HOME")
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.miniPlayerContainer, MiniPlayerFragment())
            .commit()

        playerVM.currentSong.observe(this) { song ->
            val miniPlayer = findViewById<View>(R.id.miniPlayerContainer)
            miniPlayer.visibility = if (song != null) View.VISIBLE else View.GONE
        }
        
        setupBottomNavigation()
    }

    private fun loadFragment(fragment: Fragment, tag: String = "") {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment != null && currentFragment::class == fragment::class) {
            return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment(), "HOME")
                    true
                }
                R.id.nav_search -> {
                    loadFragment(SearchFragment(), "SEARCH")
                    true
                }
                R.id.nav_ai_chat -> {
                    loadFragment(com.example.musicapp.ui.chat.AIChatFragment(), "AI_CHAT")
                    true
                }
                R.id.nav_library -> {
                    loadFragment(LibraryFragment(), "LIBRARY")
                    true
                }
                else -> false
            }
        }
    }

    fun showMiniPlayer(song: Song) {
        playerVM.play(song, listOf(song))
        
        val artistName = song.artist.firstOrNull()?.fullName ?: "Unknown Artist"
        
        val intent = Intent(this, MediaService::class.java).apply {
            action = MediaService.ACTION_PLAY
            putExtra("SONG_TITLE", song.title)
            putExtra("SONG_ARTIST", artistName)
            putExtra("SONG_URL", song.fileUrl)
            putExtra("SONG_COVER", song.coverImage)
            putExtra("SONG_ID", song._id)
        }
        ContextCompat.startForegroundService(this, intent)
    }
}
