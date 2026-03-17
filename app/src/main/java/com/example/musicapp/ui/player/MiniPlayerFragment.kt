package com.example.musicapp.ui.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.songs.Song

class MiniPlayerFragment : Fragment() {

    private val playerVM: PlayerViewModel by activityViewModels()
    private lateinit var imgCover: ImageView
    private lateinit var txtTitle: TextView
    private lateinit var txtArtist: TextView
    private lateinit var btnToggle: ImageButton
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_mini_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imgCover = view.findViewById(R.id.imgMiniCover)
        txtTitle = view.findViewById(R.id.txtMiniTitle)
        txtArtist = view.findViewById(R.id.txtMiniArtist)
        btnToggle = view.findViewById(R.id.btnMiniToggle)
        rootView = view.findViewById(R.id.miniRoot)

        // 1) Ẩn/hiện mini theo currentSong
        playerVM.currentSong.observe(viewLifecycleOwner) { song ->
            view.isVisible = song != null
            if (song != null) {
                updateUI(song)
            }
        }

        // 2) Đổi icon theo trạng thái play/pause
        playerVM.isPlaying.observe(viewLifecycleOwner) { playing ->
            if (playing) {
                btnToggle.setImageResource(R.drawable.ic_pause)
            } else {
                btnToggle.setImageResource(R.drawable.ic_play)
            }
        }

        // 3) Nhấn play/pause
        btnToggle.setOnClickListener { playerVM.toggle() }

        // 4) Click vào mini player để mở PlayerActivity
        rootView.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            startActivity(intent)
        }

        // Optional: Next button
        view.findViewById<ImageButton>(R.id.btnMiniNext)?.setOnClickListener {
            playerVM.playNext()
        }

        // Optional: Like button
        view.findViewById<ImageButton>(R.id.btnMiniLike)?.setOnClickListener {
            // TODO: Add to favorites
        }
    }

    private fun updateUI(song: Song) {
        txtTitle.text = song.title
        txtArtist.text = song.artist.firstOrNull()?.fullName ?: "Unknown Artist"
        Glide.with(this)
            .load(song.coverImage)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.drawable.img_error)
            .into(imgCover)
    }
}
