package com.example.musicapp.ui.player

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.musicapp.R
import com.example.musicapp.models.songs.LyricLine

class LyricsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    private val lyricsContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(48, 250, 48, 250)
    }

    private var lyrics: List<LyricLine> = emptyList()
    private var currentLineIndex = -1
    private val lyricViews = mutableListOf<TextView>()
    
    private val activeColor: Int
    private val inactiveColor: Int

    init {
        addView(lyricsContainer)
        isVerticalScrollBarEnabled = false
        isSmoothScrollingEnabled = true
        overScrollMode = OVER_SCROLL_NEVER // 👈 Tắt overscroll effect
        
        activeColor = try {
            ContextCompat.getColor(context, R.color.colorPrimary)
        } catch (e: Exception) {
            Color.parseColor("#1E1E1E")
        }
        inactiveColor = Color.parseColor("#AAAAAA")
    }

    fun setLyrics(newLyrics: List<LyricLine>) {
        lyrics = newLyrics
        lyricsContainer.removeAllViews()
        lyricViews.clear()
        currentLineIndex = -1
        
        if (lyrics.isEmpty()) {
            showEmptyState()
            return
        }
        
        lyrics.forEach { line ->
            val textView = TextView(context).apply {
                text = line.text
                textSize = 22f
                setTextColor(inactiveColor)
                gravity = Gravity.CENTER
                alpha = 0.4f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 32
                    bottomMargin = 32
                }
            }
            lyricsContainer.addView(textView)
            lyricViews.add(textView)
        }
    }

    private fun showEmptyState() {
        val emptyText = TextView(context).apply {
            text = "♪"
            textSize = 48f
            setTextColor(inactiveColor)
            gravity = Gravity.CENTER
            alpha = 0.2f
        }
        lyricsContainer.addView(emptyText)
    }

    fun updatePosition(positionMs: Long) {
        if (lyrics.isEmpty()) return
        
        var newIndex = -1
        var left = 0
        var right = lyrics.size - 1
        
        while (left <= right) {
            val mid = (left + right) / 2
            val line = lyrics[mid]
            
            when {
                positionMs < line.timestampMs -> right = mid - 1
                positionMs >= line.endTimeMs -> left = mid + 1
                else -> {
                    newIndex = mid
                    break
                }
            }
        }
        
        if (newIndex == -1 && right >= 0 && right < lyrics.size) {
            newIndex = right
        }
        
        if (newIndex != currentLineIndex && newIndex >= 0 && newIndex < lyricViews.size) {
            updateHighlight(newIndex)
        }
    }

    private fun updateHighlight(newIndex: Int) {
        // 👇 Không dùng animation nữa, chỉ đổi màu trực tiếp
        if (currentLineIndex >= 0 && currentLineIndex < lyricViews.size) {
            lyricViews[currentLineIndex].apply {
                setTextColor(inactiveColor)
                textSize = 22f
                alpha = 0.4f
            }
        }
        
        currentLineIndex = newIndex
        lyricViews[currentLineIndex].apply {
            setTextColor(activeColor)
            textSize = 28f
            alpha = 1.0f
        }
        
        // Smooth scroll
        post {
            val y = lyricViews[currentLineIndex].top - 
                   (this@LyricsView.height / 2) + 
                   (lyricViews[currentLineIndex].height / 2)
            smoothScrollTo(0, y)
        }
    }

    fun reset() {
        currentLineIndex = -1
        lyricViews.forEach { 
            it.setTextColor(inactiveColor)
            it.textSize = 22f
            it.alpha = 0.4f
        }
        smoothScrollTo(0, 0)
    }
}
