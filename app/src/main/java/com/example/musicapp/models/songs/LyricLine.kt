package com.example.musicapp.models.songs

import java.io.Serializable

data class LyricLine(
    val timestampMs: Long,  // Timestamp chính xác đến millisecond
    val text: String,       // Nội dung lời bài hát
    val endTimeMs: Long = timestampMs + 3000  // Thời gian kết thúc (mặc định +3s)
) : Serializable
