package com.example.musicapp.ui.playlists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.models.playlists.AddToPlaylistRequest
import com.example.musicapp.models.playlists.CreatePlaylistRequest
import com.example.musicapp.models.playlists.PlaylistDetailResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.common.UniversalSongAdapter
import com.example.musicapp.ui.player.PlayerHolder
import com.example.musicapp.ui.player.PlayerViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PlaylistDetailFragment : Fragment() {
    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDesc: TextView
    private lateinit var rvSongs: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var btnPlayAll: Button
    private lateinit var btnShuffle: Button
    private lateinit var tvSongCount: TextView

    private val playerVM: PlayerViewModel by activityViewModels()
    private lateinit var favoriteRepository: FavoriteSongsRepository
    private var adapter: UniversalSongAdapter? = null
    private var songs: List<Song> = emptyList()
    private var favoriteSongIds: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)
        ivCover = view.findViewById(R.id.ivCover)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvDesc = view.findViewById(R.id.tvDesc)
        rvSongs = view.findViewById(R.id.rvSongs)
        btnBack = view.findViewById(R.id.btnBack)
        btnPlayAll = view.findViewById(R.id.btnPlayAll)
        btnShuffle = view.findViewById(R.id.btnShuffle)
        tvSongCount = view.findViewById(R.id.tvSongCount)

        favoriteRepository = FavoriteSongsRepository()
        rvSongs.layoutManager = LinearLayoutManager(requireContext())

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnPlayAll.setOnClickListener {
            if (songs.isNotEmpty()) {
                // 👇 Update PlayerHolder + play
                PlayerHolder.currentSong = songs.first()
                playerVM.play(songs.first())
                Toast.makeText(requireContext(), "Playing all", Toast.LENGTH_SHORT).show()
            }
        }

        btnShuffle.setOnClickListener {
            if (songs.isNotEmpty()) {
                val shuffled = songs.shuffled()
                // 👇 Update PlayerHolder + play
                PlayerHolder.currentSong = shuffled.first()
                playerVM.play(shuffled.first())
                Toast.makeText(requireContext(), "Shuffle play", Toast.LENGTH_SHORT).show()
            }
        }

        val playlistId = arguments?.getString("playlistId")
        if (playlistId != null) {
            loadFavoriteSongs()
            fetchPlaylistDetail(playlistId)
        }

        return view
    }

    private fun loadFavoriteSongs() {
        favoriteRepository.getFavoriteSongs { _, _, ids ->
            favoriteSongIds = ids ?: emptySet()
            adapter?.updateFavoriteIds(favoriteSongIds)
        }
    }

    private fun fetchPlaylistDetail(playlistId: String) {
        ApiClient.api.getPlaylistDetail(playlistId).enqueue(object : Callback<PlaylistDetailResponse> {
            override fun onResponse(
                call: Call<PlaylistDetailResponse>,
                response: Response<PlaylistDetailResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val playlist = data.playlist
                    songs = data.songsInPlaylist

                    tvTitle.text = playlist.title
                    tvDesc.text = playlist.description
                    tvSongCount.text = "${songs.size} songs"

                    Glide.with(requireContext())
                        .load(playlist.coverImage)
                        .into(ivCover)

                    adapter = UniversalSongAdapter(
                        items = songs,
                        onClick = { song ->
                            PlayerHolder.currentSong = song
                            playerVM.play(song)
                        },
                        onAddToPlaylist = { song ->
                            showPlaylistDialog(song)
                        },
                        onToggleFavorite = { song ->
                            toggleFavorite(song)
                        },
                        favoriteSongIds = favoriteSongIds
                    )

                    rvSongs.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "Lỗi load playlist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PlaylistDetailResponse>, t: Throwable) {
                Log.d("API_DEBUG", "API lỗi: ${t.message}")
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleFavorite(song: Song) {
        if (favoriteSongIds.contains(song._id)) {
            favoriteRepository.removeFavoriteSong(song._id) { success, _ ->
                if (success == true) {
                    loadFavoriteSongs()
                }
            }
        } else {
            favoriteRepository.addFavoriteSong(song._id) { success, _ ->
                if (success == true) {
                    loadFavoriteSongs()
                }
            }
        }
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
                            if (addResponse.isSuccessful && addResponse.body()?.success == true) {
                                Toast.makeText(requireContext(), "Đã thêm vào ${playlist.title}", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(requireContext(), addResponse.body()?.message ?: "Thêm thất bại", Toast.LENGTH_SHORT).show()
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
                            val body = CreatePlaylistRequest(title, desc, listOf(song._id), song.coverImage)
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
}