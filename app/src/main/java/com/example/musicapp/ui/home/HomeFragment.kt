package com.example.musicapp.ui.home

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.models.artists.ArtistResponse
import com.example.musicapp.models.playlists.AddToPlaylistRequest
import com.example.musicapp.models.playlists.CreatePlaylistRequest
import com.example.musicapp.models.songs.Song
import com.example.musicapp.models.songs.SongListResponse
import com.example.musicapp.models.users.UserResponse
import com.example.musicapp.network.ApiClient
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.ui.auth.LoginActivity
import com.example.musicapp.ui.artist.ArtistAdapter
import com.example.musicapp.ui.artist.ArtistDetailFragment
import com.example.musicapp.ui.playlists.PlaylistAdapter
import com.example.musicapp.ui.suggestion.SuggestionAdapter
import com.example.musicapp.utils.PreferenceHelper
import com.example.myapp.SettingActivity
import com.example.musicapp.ui.common.UniversalSongAdapter
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class HomeFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: UniversalSongAdapter
    private lateinit var viewModel: SongViewModel
    private val playerVM: com.example.musicapp.ui.player.PlayerViewModel by activityViewModels()
    private lateinit var favoriteRepository: FavoriteSongsRepository

    // RecyclerView gợi ý bài hát
    private lateinit var rvSuggestions: RecyclerView
    private lateinit var suggestionAdapter: SuggestionAdapter

    // Header UI
    private var headerLayout: View? = null
    private var btnLogin: Button? = null
    private var imgAvatar: ImageView? = null
    private var tvWelcome: TextView? = null
    private var tvUserName: TextView? = null
    private var iconBell: ImageView? = null
    private var iconSetting: ImageView? = null
    private var userInfoLayout: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteRepository = FavoriteSongsRepository()

        // Playlist section
        rv = view.findViewById(R.id.rvPlaylists)
        rv.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = UniversalSongAdapter(
            items = emptyList(),
            onClick = { song -> 
                // Pass entire song list as queue
                playerVM.play(song, adapter.getAllSongs())
            },
            onAddToPlaylist = { song -> showPlaylistDialog(song) },
            onToggleFavorite = { song -> toggleFavorite(song) }
        )
        rv.adapter = adapter

        viewModel = ViewModelProvider(this)[SongViewModel::class.java]
        viewModel.songs.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }
        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
        viewModel.loadSongs()

        // Load favorite songs to update heart icons
        loadFavoriteSongs()

        //  Suggestions section
        rvSuggestions = view.findViewById(R.id.rvSuggestions)
        val layoutManager = GridLayoutManager(
            requireContext(),
            3,
            RecyclerView.HORIZONTAL,
            false
        )
        rvSuggestions.layoutManager = layoutManager

        suggestionAdapter = SuggestionAdapter(emptyList()) { song ->
            // Pass suggestion list as queue
            playerVM.play(song, suggestionAdapter.getAllSongs())
        }
        suggestionAdapter.setOnAddToPlaylistClickListener { song ->
            showPlaylistDialog(song)
        }
        rvSuggestions.adapter = suggestionAdapter

        // Thêm SnapHelper để cuộn từng "trang"
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvSuggestions)

        //  Gọi API lấy danh sách gợi ý
        ApiClient.api.getSuggestedSongs().enqueue(object : Callback<SongListResponse> {
            override fun onResponse(
                call: Call<SongListResponse>,
                response: Response<SongListResponse>
            ) {
                android.util.Log.d("HomeFragment", "=== Suggestions API Response ===")
                android.util.Log.d("HomeFragment", "Response code: ${response.code()}")
                android.util.Log.d("HomeFragment", "Response body: ${response.body()}")
                
                if (response.isSuccessful && response.body()?.data != null) {
                    val songs = response.body()!!.data
                    android.util.Log.d("HomeFragment", "Loaded ${songs.size} suggested songs")
                    
                    // Log artist của 2 bài đầu
                    songs.take(2).forEach { song ->  // 👈 Sửa từ "=>" thành "->"
                        android.util.Log.d("HomeFragment", "Song: ${song.title}")
                        android.util.Log.d("HomeFragment", "  Artists: ${song.artist.size} items")
                        song.artist.forEach { artist ->
                            android.util.Log.d("HomeFragment", "    - ${artist.fullName}")
                        }
                    }
                    
                    suggestionAdapter.submit(songs)
                } else {
                    android.util.Log.e("HomeFragment", "No suggestion data or failed")
                    Toast.makeText(requireContext(), "Không có dữ liệu gợi ý", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SongListResponse>, t: Throwable) {
                android.util.Log.e("HomeFragment", "API lỗi: ${t.message}", t)
                t.printStackTrace()
                if (isAdded) {
                    Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Header mapping
        headerLayout = view.findViewById(R.id.headerLayout)
        btnLogin = view.findViewById(R.id.btnLogin)
        imgAvatar = view.findViewById(R.id.imgAvatar)
        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvUserName = view.findViewById(R.id.tvUserName)
        iconBell = view.findViewById(R.id.iconBell)
        userInfoLayout = view.findViewById(R.id.userInfoLayout)
        iconSetting = view.findViewById(R.id.iconSetting)

        updateHeaderUI()

        iconSetting?.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
        }

        // Hot singers section
        val rvHotSingers = view.findViewById<RecyclerView>(R.id.rvHotSingers)
        rvHotSingers.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val artistAdapter = ArtistAdapter(emptyList()) { artist ->
            val fragment = ArtistDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("ARTIST_ID", artist._id)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        rvHotSingers.adapter = artistAdapter

        //  Gọi API lấy danh sách artists
        ApiClient.api.getHotArtists().enqueue(object : Callback<ArtistResponse> {
            override fun onResponse(
                call: Call<ArtistResponse>,
                response: Response<ArtistResponse>
            ) {
                if (response.isSuccessful && response.body()?.data != null) {
                    val artists = response.body()!!.data
                    rvHotSingers.adapter = ArtistAdapter(artists) { artist ->
                        openArtistDetail(artist._id)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Không tải được danh sách ca sĩ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ArtistResponse>, t: Throwable) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })

        // Nút "Xem tất cả"
        val tvViewAll = view.findViewById<TextView>(R.id.tvViewAllHotSingers)
        tvViewAll.setOnClickListener {
            Toast.makeText(requireContext(), "View All clicked", Toast.LENGTH_SHORT).show()
        }

        // Register receiver for next/prev from notification
        val filter = android.content.IntentFilter().apply {
            addAction("com.example.musicapp.ACTION_NEXT_SONG")
            addAction("com.example.musicapp.ACTION_PREV_SONG")
        }
        requireActivity().registerReceiver(musicControlReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    private val musicControlReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.musicapp.ACTION_NEXT_SONG" -> playerVM.playNext()
                "com.example.musicapp.ACTION_PREV_SONG" -> playerVM.playPrevious()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            requireActivity().unregisterReceiver(musicControlReceiver)
        } catch (_: Exception) {}
    }

    private fun updateHeaderUI() {
        val isLoggedIn = ApiClient.cookieManager?.getCookie() != null

        if (isLoggedIn) {
            val cachedName = PreferenceHelper.getUsername(requireContext())
            val cachedAvatar = PreferenceHelper.getAvatar(requireContext())

            headerLayout?.visibility = View.VISIBLE
            btnLogin?.visibility = View.GONE
            imgAvatar?.visibility = View.VISIBLE
            userInfoLayout?.visibility = View.VISIBLE

            tvWelcome?.text = "Welcome back !"
            tvUserName?.text = cachedName ?: "User"

            if (!cachedAvatar.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(cachedAvatar)
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(imgAvatar!!)
            } else {
                imgAvatar?.setImageResource(R.drawable.ic_user)
            }

            loadUserProfile()

        } else {
            headerLayout?.visibility = View.VISIBLE
            btnLogin?.visibility = View.VISIBLE
            imgAvatar?.visibility = View.GONE
            userInfoLayout?.visibility = View.GONE

            btnLogin?.setOnClickListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun loadUserProfile() {
        ApiClient.api.getUserProfile().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (!isAdded || view == null) return

                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data

                    tvWelcome?.text = "Welcome back !"
                    tvUserName?.text = user?.username ?: "Unknown"

                    if (!user?.avatar.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(user?.avatar)
                            .placeholder(R.drawable.ic_user)
                            .circleCrop()
                            .into(imgAvatar!!)
                    } else {
                        imgAvatar?.setImageResource(R.drawable.ic_user)
                    }

                    PreferenceHelper.setUserInfo(
                        requireContext(),
                        user?.username,
                        user?.avatar
                    )
                } else {
                    Toast.makeText(requireContext(), "Không lấy được thông tin user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                if (!isAdded || view == null) return
                Toast.makeText(requireContext(), "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

        // Load danh sách playlist
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.getMyPlaylists()
                val playlists = response.data

                val adapter = PlaylistAdapter(playlists)
                rvPlaylists.layoutManager = LinearLayoutManager(requireContext())
                rvPlaylists.adapter = adapter

                adapter.setOnItemClickListener { playlist ->
                    lifecycleScope.launch {
                        try {
                            val body = AddToPlaylistRequest(
                                playlist._id,
                                song._id
                            )
                            val addResponse = ApiClient.api.addToPlaylist(body)
                            if (addResponse.isSuccessful) {
                                val result = addResponse.body() ;
                                if (result != null) {
                                    if (result.success) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Đã thêm vào ${playlist.title}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        dialog.dismiss()
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            result.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            } else {
                                Toast.makeText(requireContext(),
                                    "Thêm thất bại", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(),
                                "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Xử lý nút tạo playlist ---
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
        val inputView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_playlist, null)
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
                                Toast.makeText(requireContext(),
                                    "Tạo playlist thành công!", Toast.LENGTH_SHORT).show()
                                onCreated()
                            } else {
                                Toast.makeText(requireContext(),
                                    "Không thể tạo playlist", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(),
                                "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show() ;
//
                            e.printStackTrace()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(),
                        "Tên playlist không được trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun toggleFavorite(song: Song) {
        // Optimistic toggle: update UI immediately
        adapter.updateFavoriteIds((setOf<String>() + listOf(song._id) + emptySet()).toSet())
        favoriteRepository.addFavoriteSong(song._id) { success, message ->
            if (success) {
                loadFavoriteSongs()
            } else {
                Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                loadFavoriteSongs()
            }
        }
    }

    private fun loadFavoriteSongs() {
        favoriteRepository.getFavoriteSongs { songs, error, favoriteIds ->
            if (error == null) {
                favoriteIds?.let { adapter.updateFavoriteIds(it) }
            }
        }
    }

    private fun openArtistDetail(artistId: String) {
        val fragment = ArtistDetailFragment().apply {
            arguments = Bundle().apply {
                putString("artistId", artistId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("ARTIST_DETAIL")
            .commit()
    }

}

