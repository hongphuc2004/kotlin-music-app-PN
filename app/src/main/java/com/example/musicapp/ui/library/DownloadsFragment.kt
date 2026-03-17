package com.example.musicapp.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.DownloadRepository
import com.example.musicapp.data.local.DownloadedSong
import com.example.musicapp.models.songs.Song
import com.example.musicapp.ui.player.PlayerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadsFragment : Fragment() {

    private lateinit var rvDownloads: RecyclerView
    private lateinit var tvEmptyState: View
    private lateinit var btnBack: ImageButton
    private lateinit var tvSongCount: TextView
    private lateinit var adapter: DownloadedSongsAdapter
    private lateinit var repository: DownloadRepository
    private val playerVM: PlayerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        rvDownloads = view.findViewById(R.id.rvDownloads)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        btnBack = view.findViewById(R.id.btnBack)
        tvSongCount = view.findViewById(R.id.tvSongCount)
        
        repository = DownloadRepository(requireContext())
        
        setupRecyclerView()
        setupClickListeners()
        observeDownloads()
    }

    private fun setupRecyclerView() {
        adapter = DownloadedSongsAdapter(
            onPlayClick = { downloadedSong ->
                playOfflineSong(downloadedSong)
            },
            onDeleteClick = { downloadedSong ->
                deleteDownload(downloadedSong)
            }
        )
        
        rvDownloads.layoutManager = LinearLayoutManager(requireContext())
        rvDownloads.adapter = adapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeDownloads() {
        lifecycleScope.launch {
            repository.getAllDownloadedSongs().collectLatest { songs ->
                if (songs.isEmpty()) {
                    showEmptyState()
                } else {
                    showSongsList()
                    tvSongCount.text = "${songs.size} downloaded songs"
                    adapter.submitList(songs)
                }
            }
        }
    }

    private fun playOfflineSong(downloadedSong: DownloadedSong) {
        // Convert DownloadedSong to Song object with local file path
        val offlineSong = Song(
            _id = downloadedSong.songId,
            title = downloadedSong.title,
            artist = listOf(com.example.musicapp.models.artists.Artist(
                _id = "",
                fullName = downloadedSong.artist
            )),
            fileUrl = "file://${downloadedSong.localFilePath}", // 👈 Local file path
            coverImage = downloadedSong.coverImageUrl
        )
        
        playerVM.play(offlineSong)
        Toast.makeText(requireContext(), "Playing offline", Toast.LENGTH_SHORT).show()
    }

    private fun deleteDownload(downloadedSong: DownloadedSong) {
        lifecycleScope.launch {
            try {
                repository.deleteDownload(downloadedSong.songId)
                Toast.makeText(requireContext(), "Deleted ${downloadedSong.title}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEmptyState() {
        rvDownloads.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }

    private fun showSongsList() {
        rvDownloads.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
    }

    companion object {
        fun newInstance() = DownloadsFragment()
    }
}
