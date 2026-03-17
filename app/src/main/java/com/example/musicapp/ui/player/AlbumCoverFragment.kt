package com.example.musicapp.ui.player

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.musicapp.R

class AlbumCoverFragment : Fragment() {

    private var coverUrl: String? = null
    private var vinylRotation: ObjectAnimator? = null
    private lateinit var imgVinyl: ImageView
    private lateinit var imgAlbumArt: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            coverUrl = it.getString(ARG_COVER_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album_cover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        imgVinyl = view.findViewById(R.id.imgVinyl)
        imgAlbumArt = view.findViewById(R.id.imgAlbumArt)
        
        // Load album art
        Glide.with(requireContext())
            .load(coverUrl)
            .placeholder(R.drawable.ic_default_album_art)
            .error(R.drawable.ic_default_album_art)
            .centerCrop()
            .into(imgAlbumArt)
        
        // Start vinyl rotation if playing
        if (PlayerHolder.player.isPlaying) {
            startVinylRotation()
        }
    }

    fun startVinylRotation() {
        if (vinylRotation == null) {
            vinylRotation = ObjectAnimator.ofFloat(imgVinyl, "rotation", 0f, 360f).apply {
                duration = 10000
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
        }
        
        if (vinylRotation?.isPaused == true) {
            vinylRotation?.resume()
        } else if (vinylRotation?.isRunning != true) {
            vinylRotation?.start()
        }
    }

    fun pauseVinylRotation() {
        vinylRotation?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vinylRotation?.cancel()
        vinylRotation = null
    }

    companion object {
        private const val ARG_COVER_URL = "cover_url"
        
        fun newInstance(coverUrl: String) = AlbumCoverFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_COVER_URL, coverUrl)
            }
        }
    }
}
