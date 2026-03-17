package com.example.musicapp.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.models.songs.Song
import com.example.musicapp.ui.common.UniversalSongAdapter
import com.example.musicapp.ui.player.PlayerViewModel
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.musicapp.models.playlists.AddToPlaylistRequest
import com.example.musicapp.models.playlists.CreatePlaylistRequest
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.playlists.PlaylistAdapter
import kotlinx.coroutines.launch

class FavoriteSongsFragment : Fragment() {
    
    private lateinit var rvFavoriteSongs: RecyclerView
    private lateinit var tvEmptyState: View
    private lateinit var btnClearAll: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnPlayAll: Button
    private lateinit var btnShuffle: Button
    private lateinit var tvSongCount: TextView
    private lateinit var adapter: UniversalSongAdapter
    private lateinit var repository: FavoriteSongsRepository
    private val playerVM: PlayerViewModel by activityViewModels()
    
    private var favoriteSongs: List<Song> = emptyList()
    private var favoriteSongIds: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite_songs, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadFavoriteSongs()
    }
    
    private fun initViews(view: View) {
        rvFavoriteSongs = view.findViewById(R.id.rvFavoriteSongs)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        btnClearAll = view.findViewById(R.id.btnClearAll)
        btnBack = view.findViewById(R.id.btnBack)
        btnPlayAll = view.findViewById(R.id.btnPlayAll)
        btnShuffle = view.findViewById(R.id.btnShuffle)
        tvSongCount = view.findViewById(R.id.tvSongCount)
        repository = FavoriteSongsRepository()
    }
    
    private fun setupRecyclerView() {
        adapter = UniversalSongAdapter(
            items = emptyList(),
            onClick = { song ->
                com.example.musicapp.ui.player.PlayerHolder.currentSong = song
                playerVM.play(song)
            },
            onAddToPlaylist = { song ->
                showPlaylistDialog(song)
            },
            onToggleFavorite = { song ->
                toggleFavorite(song)
            }
        )
        
        rvFavoriteSongs.layoutManager = LinearLayoutManager(requireContext())
        rvFavoriteSongs.adapter = adapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        btnClearAll.setOnClickListener {
            showClearAllDialog()
        }
        
        btnPlayAll.setOnClickListener {
            if (favoriteSongs.isNotEmpty()) {
                // 👇 Update PlayerHolder + play
                com.example.musicapp.ui.player.PlayerHolder.currentSong = favoriteSongs.first()
                playerVM.play(favoriteSongs.first())
                Toast.makeText(requireContext(), "Playing all songs", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnShuffle.setOnClickListener {
            if (favoriteSongs.isNotEmpty()) {
                val shuffled = favoriteSongs.shuffled()
                // 👇 Update PlayerHolder + play
                com.example.musicapp.ui.player.PlayerHolder.currentSong = shuffled.first()
                playerVM.play(shuffled.first())
                Toast.makeText(requireContext(), "Shuffle play", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFavoriteSongs() {
        repository.getFavoriteSongs { songs, error, ids ->
            if (error == null && songs != null) {
                favoriteSongs = songs
                favoriteSongIds = ids ?: emptySet()
                
                adapter.updateFavoriteIds(favoriteSongIds)
                adapter.updateData(favoriteSongs)
                
                updateUI()
            } else {
                showEmptyState()
            }
        }
    }

    
    private fun toggleFavorite(song: Song) {
        repository.removeFavoriteSong(song._id) { success, _ ->
            if (success == true) {
                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
                loadFavoriteSongs()
            }
        }
    }

    private fun showClearAllDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Clear all favorites?")
            .setMessage("This will remove all songs from your liked songs.")
            .setPositiveButton("Clear") { _, _ ->
                clearAllFavorites()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllFavorites() {
        repository.removeAllFavoriteSongs { success, _ ->
            if (success == true) {
                Toast.makeText(requireContext(), "All favorites cleared", Toast.LENGTH_SHORT).show()
                loadFavoriteSongs()
            }
        }
    }
    
    private fun updateUI() {
        if (favoriteSongs.isEmpty()) {
            showEmptyState()
        } else {
            showSongsList()
            adapter.updateData(favoriteSongs)
        }
    }
    
    private fun showEmptyState() {
        rvFavoriteSongs.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
        btnClearAll.visibility = View.GONE
    }
    
    private fun showSongsList() {
        rvFavoriteSongs.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        btnClearAll.visibility = View.VISIBLE
    }
    
    private fun showPlaylistDialog(song: Song) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_playlists, null)
        val rvPlaylists = dialogView.findViewById<RecyclerView>(R.id.rvPlaylists)
        val btnCreatePlaylist = dialogView.findViewById<Button>(R.id.btnCreatePlaylist)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Chọn playlist")
            .setView(dialogView)
            .setNegativeButton("Đóng", null)
            .create()

        // Load playlists
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.getMyPlaylists()
                val playlists = response.data

                val playlistAdapter = PlaylistAdapter(playlists)
                rvPlaylists.layoutManager = LinearLayoutManager(requireContext())
                rvPlaylists.adapter = playlistAdapter

                playlistAdapter.setOnItemClickListener { playlist ->
                    lifecycleScope.launch {
                        try {
                            val body = AddToPlaylistRequest(playlist._id, song._id)
                            val addResponse = ApiClient.api.addToPlaylist(body)
                            if (addResponse.isSuccessful) {
                                val result = addResponse.body()
                                if (result?.success == true) {
                                    Toast.makeText(requireContext(), "Đã thêm vào ${playlist.title}", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                } else {
                                    Toast.makeText(requireContext(), result?.message ?: "Thêm thất bại", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Thêm thất bại", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnCreatePlaylist.setOnClickListener {
            showCreatePlaylistDialog(song) {
                // Reload playlists after creation
                lifecycleScope.launch {
                    try {
                        val response = ApiClient.api.getMyPlaylists()
                        (rvPlaylists.adapter as? PlaylistAdapter)?.apply {
                            val newAdapter = PlaylistAdapter(response.data)
                            rvPlaylists.adapter = newAdapter
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        dialog.show()
    }

    private fun showCreatePlaylistDialog(song: Song, onCreated: () -> Unit) {
        val inputView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_playlist, null)
        val etTitle = inputView.findViewById<EditText>(R.id.etTitle)
        val etDescription = inputView.findViewById<EditText>(R.id.etDescription)

        AlertDialog.Builder(requireContext())
            .setTitle("Tạo Playlist mới")
            .setView(inputView)
            .setPositiveButton("Tạo") { _, _ ->
                val title = etTitle.text.toString().trim()
                val desc = etDescription.text.toString().trim()

                if (title.isNotEmpty()) {
                    lifecycleScope.launch {
                        try {
                            val body = CreatePlaylistRequest(
                                title = title,
                                description = desc,
                                songs = listOf(song._id),
                                song.coverImage
                            )
                            val response = ApiClient.api.createPlaylist(body)
                            if (response.code == "success") {
                                Toast.makeText(requireContext(), "Tạo playlist thành công!", Toast.LENGTH_SHORT).show()
                                onCreated()
                            } else {
                                Toast.makeText(requireContext(), "Không thể tạo playlist", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Tên playlist không được trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    companion object {
        fun newInstance(): FavoriteSongsFragment {
            return FavoriteSongsFragment()
        }
    }
}




