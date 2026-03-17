package com.example.musicapp.ui.artist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.models.artists.ArtistDetailResponse
import com.example.musicapp.models.artists.ArtistResponse
import com.example.musicapp.models.playlists.AddToPlaylistRequest
import com.example.musicapp.models.playlists.CreatePlaylistRequest
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.common.UniversalSongAdapter
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.ui.home.SongAdapter
import com.example.musicapp.ui.player.PlayerHolder
import com.example.musicapp.ui.playlists.PlaylistAdapter
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArtistDetailFragment : Fragment() {

    private lateinit var rvSongs: RecyclerView
    private lateinit var songAdapter: UniversalSongAdapter
    private lateinit var favoriteRepository: FavoriteSongsRepository
    private var favoriteSongIds: Set<String> = emptySet()

    private lateinit var tvArtistName: TextView
    private lateinit var ivArtistCover: ImageView
    private lateinit var btnShowMoreSong: TextView

    // ---- Hot singers
    private lateinit var rvHotSingers: RecyclerView
    private lateinit var hotArtistAdapter: ArtistAdapter

    // ---- Information about artist
    private lateinit var tvArtistCountry: TextView
    private lateinit var tvArtistRealName: TextView

    private var artistId: String? = null
    private var fullSongList: List<com.example.musicapp.models.songs.Song> = emptyList()
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artistId = arguments?.getString("artistId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_artist_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 👇 Khởi tạo views TRƯỚC KHI gọi API
        tvArtistName = view.findViewById(R.id.tvArtistName)
        ivArtistCover = view.findViewById(R.id.ivArtistCover)
        btnShowMoreSong = view.findViewById(R.id.btnShowMoreSong)
        rvSongs = view.findViewById(R.id.rvSongsByArtist)
        rvHotSingers = view.findViewById(R.id.rvHotSingers)
        tvArtistRealName = view.findViewById(R.id.tvArtistRealName)
        tvArtistCountry = view.findViewById(R.id.tvArtistCountry)

        favoriteRepository = FavoriteSongsRepository()

        // Setup RecyclerViews
        rvSongs.layoutManager = LinearLayoutManager(requireContext())
        songAdapter = UniversalSongAdapter(
            items = emptyList(),
            onClick = { song ->
                (activity as? com.example.musicapp.MainActivity)?.showMiniPlayer(song)
            },
            onAddToPlaylist = { song ->
                showPlaylistDialog(song)
            },
            onToggleFavorite = { song ->
                toggleFavorite(song)
            }
        )
        rvSongs.adapter = songAdapter

        // Nút xem thêm / thu gọn
        btnShowMoreSong.setOnClickListener {
            toggleSongs()
        }

        // Hot singers RecyclerView
        rvHotSingers.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        hotArtistAdapter = ArtistAdapter(emptyList()) { artist ->
            if (artist._id == artistId) {
                Toast.makeText(requireContext(), "Đang xem ${artist.fullName}", Toast.LENGTH_SHORT).show()
                return@ArtistAdapter
            }
            val frag = ArtistDetailFragment().apply {
                arguments = Bundle().apply { putString("artistId", artist._id) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, frag)
                .addToBackStack("ARTIST_DETAIL")
                .commit()
        }
        rvHotSingers.adapter = hotArtistAdapter

        // 👇 GỌI API SAU KHI đã khởi tạo views
        if (artistId != null) {
            loadFavoriteSongs()
            loadArtistSongs(artistId!!)
            loadHotSingers(artistId!!)
        } else {
            Toast.makeText(requireContext(), "Không có artistId", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFavoriteSongs() {
        favoriteRepository.getFavoriteSongs { _, _, ids ->
            favoriteSongIds = ids ?: emptySet()
            songAdapter.updateFavoriteIds(favoriteSongIds)
        }
    }

    private fun loadArtistSongs(artistId: String) {
        android.util.Log.d("ArtistDetailFragment", "=== Loading artist: $artistId ===")

        ApiClient.api.getArtistDetail(artistId).enqueue(object : Callback<ArtistDetailResponse> {
            override fun onResponse(
                call: Call<ArtistDetailResponse>,
                response: Response<ArtistDetailResponse>
            ) {
                if (!isAdded) return

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    android.util.Log.d("ArtistDetailFragment", "Artist: ${body.artist.fullName}")
                    android.util.Log.d("ArtistDetailFragment", "Country: ${body.artist.country}")
                    android.util.Log.d("ArtistDetailFragment", "Cover: ${body.artist.coverImage}")
                    android.util.Log.d("ArtistDetailFragment", "Songs: ${body.songs.size}")

                    // Update UI
                    tvArtistName.text = body.artist.fullName
                    tvArtistRealName.text = "Tên thật: ${body.artist.fullName}"
                    tvArtistCountry.text = "Quốc gia: ${body.artist.country}"

                    // Load artist cover
                    Glide.with(requireContext())
                        .load(body.artist.coverImage)
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_default_album_art)
                        .centerCrop()
                        .into(ivArtistCover)

                    // Convert SongForArtist to Song
                    fullSongList = body.songs.map { s ->
                        com.example.musicapp.models.songs.Song(
                            _id = s._id,
                            title = s.title,
                            artist = s.artist,
                            album = s.album ?: "",
                            topic = s.topic ?: emptyList(),
                            fileUrl = s.fileUrl ?: "",
                            coverImage = s.coverImage ?: "",
                            likes = s.likes ?: emptyList(),
                            lyrics = s.lyrics,
                            description = s.description,
                            status = s.status ?: "active",
                            deleted = s.deleted ?: false,
                            createdAt = s.createdAt ?: "",
                            updatedAt = s.updatedAt ?: ""
                        )
                    }

                    songAdapter.updateData(fullSongList.take(5))
                    songAdapter.updateFavoriteIds(favoriteSongIds)
                    
                    // Show button nếu có > 5 bài
                    btnShowMoreSong.visibility = if (fullSongList.size > 5) View.VISIBLE else View.GONE
                } else {
                    android.util.Log.e("ArtistDetailFragment", "Response not successful")
                    Toast.makeText(requireContext(), "Lỗi tải artist detail", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArtistDetailResponse>, t: Throwable) {
                if (!isAdded) return
                android.util.Log.e("ArtistDetailFragment", "API error: ${t.message}", t)
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // chỉ hiển thị 5 bài
    private fun showLimitedSongs() {
        val limitedList = if (fullSongList.size > 5) fullSongList.take(5) else fullSongList
        // 👇 Đổi từ submit() thành updateData()
        songAdapter.updateData(limitedList)
        btnShowMoreSong.visibility = if (fullSongList.size > 5) View.VISIBLE else View.GONE
        btnShowMoreSong.text = "Xem thêm"
        isExpanded = false
    }

    // toggle xem thêm / thu gọn
    private fun toggleSongs() {
        if (isExpanded) {
            showLimitedSongs()
        } else {
            // 👇 Đổi từ submit() thành updateData()
            songAdapter.updateData(fullSongList)
            btnShowMoreSong.text = "Thu gọn"
            isExpanded = true
        }
    }

    // hot singers
    private fun loadHotSingers(currentArtistId: String) {
        ApiClient.api.getHotArtists().enqueue(object : Callback<ArtistResponse> {
            override fun onResponse(call: Call<ArtistResponse>, response: Response<ArtistResponse>) {
                if (!isAdded) return
                if (response.isSuccessful && response.body()?.data != null) {
                    val allArtists = response.body()!!.data
                    val filteredArtists = allArtists.filter { it._id != currentArtistId }
                    hotArtistAdapter.updateArtists(filteredArtists)
                }
            }

            override fun onFailure(call: Call<ArtistResponse>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Không tải được danh sách ca sĩ: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleFavorite(song: com.example.musicapp.models.songs.Song) {
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

    private fun displayArtistInfo(artist: com.example.musicapp.models.artists.Artist) {
        tvArtistName.text = artist.fullName
        tvArtistCountry.text = artist.country

        Glide.with(requireContext())
            .load(artist.coverImage)
            .placeholder(R.drawable.ic_user)
            .error(R.drawable.ic_default_album_art)
            .centerCrop()
            .into(ivArtistCover)
    }

    private fun displayArtistSongs(songs: List<com.example.musicapp.models.songs.Song>) {
        val adapter = SongAdapter(songs) { song ->
            (activity as? MainActivity)?.showMiniPlayer(song)
        }
        rvSongs.adapter = adapter
    }

    private fun showPlaylistDialog(song: com.example.musicapp.models.songs.Song) {
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

    private fun showCreatePlaylistDialog(song: com.example.musicapp.models.songs.Song, onCreated: () -> Unit) {
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
