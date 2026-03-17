package com.example.musicapp.ui.topic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.models.songs.ApiListResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.common.UniversalSongAdapter
import com.example.musicapp.ui.player.PlayerViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopicSongsFragment : Fragment() {

    private var topicId: String = ""
    private var topicName: String = ""
    private var topicImage: String? = null

    private lateinit var rvSongs: RecyclerView
    private lateinit var tvTopicName: TextView
    private lateinit var ivTopicCover: ImageView
    private lateinit var btnShowMoreSong: TextView
    private lateinit var btnBack: ImageButton

    private var fullSongList: List<Song> = emptyList()
    private lateinit var songAdapter: UniversalSongAdapter
    private var isExpanded = false
    
    private val playerVM: PlayerViewModel by activityViewModels()
    private lateinit var favoriteRepository: FavoriteSongsRepository
    private var favoriteSongIds: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicId = arguments?.getString("topicId") ?: ""
        topicName = arguments?.getString("topicName") ?: ""
        topicImage = arguments?.getString("topicImage")
        
        Log.d("TopicSongsFragment", "=== Fragment Created ===")
        Log.d("TopicSongsFragment", "Topic ID: $topicId")
        Log.d("TopicSongsFragment", "Topic Name: $topicName")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_topic_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTopicName = view.findViewById(R.id.tvTopicName)
        ivTopicCover = view.findViewById(R.id.ivTopicCover)
        rvSongs = view.findViewById(R.id.rvSongs)
        btnShowMoreSong = view.findViewById(R.id.btnShowMoreSong)
        btnBack = view.findViewById(R.id.btnBack)

        favoriteRepository = FavoriteSongsRepository()

        tvTopicName.text = topicName

        // Load ảnh topic
        Glide.with(requireContext())
            .load(topicImage)
            .placeholder(R.drawable.ic_default_album_art)
            .centerCrop()
            .into(ivTopicCover)

        // Setup RecyclerView với UniversalSongAdapter
        rvSongs.layoutManager = LinearLayoutManager(requireContext())
        songAdapter = UniversalSongAdapter(
            items = emptyList(),
            onClick = { song ->
                com.example.musicapp.ui.player.PlayerHolder.currentSong = song
                playerVM.play(song)
            },
            onAddToPlaylist = { song ->
                Toast.makeText(requireContext(), "Add to playlist", Toast.LENGTH_SHORT).show()
            },
            onToggleFavorite = { song ->
                toggleFavorite(song)
            },
            favoriteSongIds = favoriteSongIds
        )
        rvSongs.adapter = songAdapter

        btnShowMoreSong.setOnClickListener { toggleSongs() }
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Load favorites + songs
        loadFavoriteSongs()
        fetchSongsForTopic()
    }

    private fun loadFavoriteSongs() {
        favoriteRepository.getFavoriteSongs { _, _, ids ->
            favoriteSongIds = ids ?: emptySet()
            songAdapter.updateFavoriteIds(favoriteSongIds)
        }
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

    private fun fetchSongsForTopic() {
        Log.d("TopicSongsFragment", "=== Fetching songs for topic: $topicId ===")
        
        ApiClient.api.getSongs().enqueue(object : Callback<ApiListResponse<Song>> {
            override fun onResponse(
                call: Call<ApiListResponse<Song>>,
                response: Response<ApiListResponse<Song>>
            ) {
                if (!isAdded) return

                if (response.isSuccessful) {
                    val allSongs = response.body()?.data ?: emptyList()
                    Log.d("TopicSongsFragment", "Total songs from API: ${allSongs.size}")

                    // Filter theo topicId
                    fullSongList = allSongs.filter { song ->
                        val hasTopicId = song.topic.contains(topicId)
                        if (hasTopicId) {
                            Log.d("TopicSongsFragment", "✅ Matched: ${song.title}")
                        }
                        hasTopicId
                    }

                    Log.d("TopicSongsFragment", "Filtered songs: ${fullSongList.size}")

                    if (fullSongList.isEmpty()) {
                        Toast.makeText(requireContext(), "No songs in this topic", Toast.LENGTH_SHORT).show()
                    }

                    showLimitedSongs()
                } else {
                    Log.e("TopicSongsFragment", "API Error: ${response.code()}")
                    Toast.makeText(requireContext(), "Error loading songs", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiListResponse<Song>>, t: Throwable) {
                if (!isAdded) return
                Log.e("TopicSongsFragment", "API Failure: ${t.message}", t)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLimitedSongs() {
        val limited = if (fullSongList.size > 5) fullSongList.take(5) else fullSongList

        songAdapter.updateData(limited)
        btnShowMoreSong.visibility = if (fullSongList.size > 5) View.VISIBLE else View.GONE
        btnShowMoreSong.text = "Xem thêm"
        isExpanded = false
    }

    private fun toggleSongs() {
        if (isExpanded) {
            showLimitedSongs()
        } else {
            songAdapter.updateData(fullSongList)
            btnShowMoreSong.text = "Thu gọn"
            isExpanded = true
        }
    }

    companion object {
        fun newInstance(topicId: String, topicName: String, topicImage: String?): TopicSongsFragment {
            val f = TopicSongsFragment()
            f.arguments = Bundle().apply {
                putString("topicId", topicId)
                putString("topicName", topicName)
                putString("topicImage", topicImage)
            }
            return f
        }
    }
}
