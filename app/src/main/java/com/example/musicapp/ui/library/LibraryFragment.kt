package com.example.musicapp.ui.library

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.data.DownloadRepository
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.playlists.PlaylistDetailFragment
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private lateinit var cardFavorites: MaterialCardView
    private lateinit var tvFavoriteCount: TextView
    private lateinit var rvPlaylists: RecyclerView
    private lateinit var layoutEmptyPlaylists: View
    private lateinit var btnAddPlaylist: ImageButton
    private lateinit var tvViewAllPlaylists: TextView
    private lateinit var cardDownloads: MaterialCardView
    private lateinit var tvDownloadCount: TextView
    
    private lateinit var favoriteRepository: FavoriteSongsRepository
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var playlistAdapter: PlaylistGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerViews()
        setupClickListeners()
        loadData()
    }

    private fun initViews(view: View) {
        cardFavorites = view.findViewById(R.id.cardFavorites)
        tvFavoriteCount = view.findViewById(R.id.tvFavoriteCount)
        rvPlaylists = view.findViewById(R.id.rvPlaylists)
        layoutEmptyPlaylists = view.findViewById(R.id.layoutEmptyPlaylists)
        btnAddPlaylist = view.findViewById(R.id.btnAddPlaylist)
        tvViewAllPlaylists = view.findViewById(R.id.tvViewAllPlaylists)
        cardDownloads = view.findViewById(R.id.cardDownloads)
        tvDownloadCount = view.findViewById(R.id.tvDownloadCount)
        
        favoriteRepository = FavoriteSongsRepository()
        downloadRepository = DownloadRepository(requireContext())
    }

    private fun setupRecyclerViews() {
        rvPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        playlistAdapter = PlaylistGridAdapter(emptyList()) { playlist ->
            openPlaylistDetail(playlist._id)
        }
        rvPlaylists.adapter = playlistAdapter
    }

    private fun setupClickListeners() {
        cardFavorites.setOnClickListener { openFavoriteSongs() }
        
        btnAddPlaylist.setOnClickListener {
            Toast.makeText(requireContext(), "Create playlist", Toast.LENGTH_SHORT).show()
        }
        
        tvViewAllPlaylists.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AllPlaylistsFragment.newInstance())
                .addToBackStack("ALL_PLAYLISTS")
                .commit()
        }
        
        cardDownloads.setOnClickListener {
            openDownloads()
        }
    }

    private fun loadData() {
        loadFavoriteSongs()
        loadPlaylists()
        loadDownloads()
    }

    private fun loadFavoriteSongs() {
        favoriteRepository.getFavoriteSongs { songs, error, _ ->
            if (error == null && songs != null) {
                tvFavoriteCount.text = "${songs.size} songs"
            } else {
                tvFavoriteCount.text = "0 songs"
            }
        }
    }

    private fun loadPlaylists() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.getMyPlaylists()
                val playlists = response.data
                
                if (playlists.isNotEmpty()) {
                    rvPlaylists.visibility = View.VISIBLE
                    layoutEmptyPlaylists.visibility = View.GONE
                    tvViewAllPlaylists.visibility = View.VISIBLE
                    playlistAdapter.updateData(playlists.take(4))
                } else {
                    rvPlaylists.visibility = View.GONE
                    layoutEmptyPlaylists.visibility = View.VISIBLE
                    tvViewAllPlaylists.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                rvPlaylists.visibility = View.GONE
                layoutEmptyPlaylists.visibility = View.VISIBLE
                tvViewAllPlaylists.visibility = View.GONE
            }
        }
    }

    private fun loadDownloads() {
        lifecycleScope.launch {
            downloadRepository.getAllDownloadedSongs().collect { songs ->
                tvDownloadCount.text = "${songs.size} songs"
            }
        }
    }

    private fun openFavoriteSongs() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, com.example.musicapp.ui.favorites.FavoriteSongsFragment.newInstance())
            .addToBackStack("FAVORITES")
            .commit()
    }

    private fun openPlaylistDetail(playlistId: String) {
        val fragment = PlaylistDetailFragment().apply {
            arguments = Bundle().apply {
                putString("playlistId", playlistId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("PLAYLIST_DETAIL")
            .commit()
    }

    private fun openDownloads() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, DownloadsFragment.newInstance())
            .addToBackStack("DOWNLOADS")
            .commit()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}
