package com.example.musicapp.ui.player

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PlayerPagerAdapter(
    fa: FragmentActivity,
    private val coverUrl: String,
    private val lyrics: List<com.example.musicapp.models.songs.LyricLine>
) : FragmentStateAdapter(fa) {
    
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AlbumCoverFragment.newInstance(coverUrl)
            1 -> LyricsFragment.newInstance(lyrics)
            else -> Fragment()
        }
    }
}
