package com.example.musicapp.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.musicapp.R
import com.example.musicapp.models.songs.LyricLine

class LyricsFragment : Fragment() {

    private lateinit var lyricsView: LyricsView
    private var lyrics: ArrayList<LyricLine>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            lyrics = it.getSerializable(ARG_LYRICS) as? ArrayList<LyricLine>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lyrics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lyricsView = view.findViewById(R.id.lyricsView)
        lyrics?.let { lyricsView.setLyrics(it) }
    }

    fun updatePosition(positionMs: Long) {
        if (::lyricsView.isInitialized) {
            lyricsView.updatePosition(positionMs)
        }
    }

    companion object {
        private const val ARG_LYRICS = "lyrics"
        
        fun newInstance(lyrics: List<LyricLine>) = LyricsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_LYRICS, ArrayList(lyrics))
            }
        }
    }
}
