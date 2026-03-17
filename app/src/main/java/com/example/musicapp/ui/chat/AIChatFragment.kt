package com.example.musicapp.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.models.chat.AIChatMessage
import com.example.musicapp.models.chat.ChatRequest
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.player.PlayerViewModel
import kotlinx.coroutines.launch

class AIChatFragment : Fragment() {

    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnVoiceInput: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var playlistPreview: View
    private lateinit var tvPlaylistTitle: TextView
    private lateinit var tvPlaylistDesc: TextView
    private lateinit var btnPlayNow: Button
    private lateinit var btnSavePlaylist: Button

    private val chatAdapter = ChatAdapter()
    private val messages = mutableListOf<AIChatMessage>()
    private val playerVM: PlayerViewModel by activityViewModels()
    
    private var currentSuggestion: com.example.musicapp.models.chat.PlaylistSuggestion? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ai_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvChatMessages = view.findViewById(R.id.rvChatMessages)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)
        btnVoiceInput = view.findViewById(R.id.btnVoiceInput)
        btnBack = view.findViewById(R.id.btnBack)
        playlistPreview = view.findViewById(R.id.playlistPreview)
        tvPlaylistTitle = view.findViewById(R.id.tvPlaylistTitle)
        tvPlaylistDesc = view.findViewById(R.id.tvPlaylistDesc)
        btnPlayNow = view.findViewById(R.id.btnPlayNow)
        btnSavePlaylist = view.findViewById(R.id.btnSavePlaylist)

        setupRecyclerView()
        setupClickListeners()
        loadChatHistory()
    }

    private fun setupRecyclerView() {
        rvChatMessages.layoutManager = LinearLayoutManager(requireContext())
        rvChatMessages.adapter = chatAdapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSend.setOnClickListener {
            sendMessage()
        }

        btnVoiceInput.setOnClickListener {
            // TODO: Implement voice input
            Toast.makeText(requireContext(), "Voice input coming soon", Toast.LENGTH_SHORT).show()
        }

        btnPlayNow.setOnClickListener {
            currentSuggestion?.let { playlist ->
                playerVM.setPlaylistAndPlay(playlist.songs, playlist.songs.first())
                Toast.makeText(requireContext(), "Playing ${playlist.title}", Toast.LENGTH_SHORT).show()
                playlistPreview.visibility = View.GONE
            }
        }

        btnSavePlaylist.setOnClickListener {
            savePlaylist()
        }
    }

    private fun sendMessage() {
        val message = etMessage.text.toString().trim()
        if (message.isEmpty()) return

        val userMessage = AIChatMessage(
            content = message,
            sender = "user"
        )
        messages.add(userMessage)
        chatAdapter.updateMessages(messages)
        rvChatMessages.scrollToPosition(messages.size - 1)
        etMessage.text.clear()

        lifecycleScope.launch {
            try {
                val request = ChatRequest(
                    message = message,
                    userId = "current_user_id"
                )
                val response = ApiClient.api.sendChatMessage(request)

                if (response.success) {
                    val botMessage = AIChatMessage(
                        content = response.message,
                        sender = "bot"
                    )
                    messages.add(botMessage)
                    chatAdapter.updateMessages(messages)
                    rvChatMessages.scrollToPosition(messages.size - 1)

                    response.playlist?.let { playlist ->
                        showPlaylistPreview(playlist, response.cached)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPlaylistPreview(
        playlist: com.example.musicapp.models.chat.PlaylistSuggestion,
        cached: Boolean
    ) {
        currentSuggestion = playlist
        playlistPreview.visibility = View.VISIBLE
        
        tvPlaylistTitle.text = playlist.title
        tvPlaylistDesc.text = "${playlist.songs.size} songs • ${playlist.mood}" +
                if (cached) " • Cached" else ""

        // Auto scroll to show preview
        rvChatMessages.postDelayed({
            rvChatMessages.smoothScrollToPosition(messages.size - 1)
        }, 100)
    }

    private fun savePlaylist() {
        currentSuggestion?.let { playlist ->
            lifecycleScope.launch {
                try {
                    val response = ApiClient.api.saveSuggestedPlaylist(playlist)
                    if (response.success) {
                        Toast.makeText(requireContext(), "Playlist saved!", Toast.LENGTH_SHORT).show()
                        playlistPreview.visibility = View.GONE
                    } else {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadChatHistory() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.getChatHistory()
                if (response.success && response.data != null) {
                    messages.clear()
                    messages.addAll(response.data)
                    chatAdapter.updateMessages(messages)
                }
            } catch (e: Exception) {
                // Ignore, start fresh
            }
        }
    }
}
