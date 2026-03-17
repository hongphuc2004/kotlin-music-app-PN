package com.example.musicapp.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.playlists.PlaylistDetailFragment
import kotlinx.coroutines.launch

class AllPlaylistsFragment : Fragment() {

    private lateinit var rvPlaylists: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var layoutEmpty: View
    private lateinit var adapter: PlaylistGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvPlaylists = view.findViewById(R.id.rvAllPlaylists)
        btnBack = view.findViewById(R.id.btnBack)
        layoutEmpty = view.findViewById(R.id.layoutEmptyPlaylists)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupRecyclerView()
        loadPlaylists()
    }

    private fun setupRecyclerView() {
        rvPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = PlaylistGridAdapter(emptyList()) { playlist ->
            openPlaylistDetail(playlist._id)
        }
        rvPlaylists.adapter = adapter
    }

    private fun loadPlaylists() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.getMyPlaylists()
                val playlists = response.data

                if (playlists.isNotEmpty()) {
                    rvPlaylists.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    adapter.updateData(playlists)
                } else {
                    rvPlaylists.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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

    companion object {
        fun newInstance() = AllPlaylistsFragment()
    }
}
