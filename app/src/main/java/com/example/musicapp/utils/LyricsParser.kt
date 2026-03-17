package com.example.musicapp.utils

import android.util.Log
import com.example.musicapp.models.songs.LyricLine

object LyricsParser {
    
    private const val TAG = "LyricsParser"
    
    /**
     * Parse LRC format chuẩn
     * Formats hỗ trợ:
     * - [mm:ss.xx]text
     * - [mm:ss.xxx]text
     * - [mm:ss]text
     */
    fun parseLRC(lrcText: String?): List<LyricLine> {
        if (lrcText.isNullOrBlank()) {
            Log.w(TAG, "LRC text is null or empty")
            return emptyList()
        }
        
        val lyrics = mutableListOf<LyricLine>()
        
        // Regex hỗ trợ nhiều format timestamp
        val timeRegex = """\[(\d+):(\d+)(?:\.(\d+))?\](.*)""".toRegex()
        
        lrcText.split("\n").forEach { line ->
            val trimmedLine = line.trim()
            
            // Skip metadata lines
            if (trimmedLine.startsWith("[ti:") || 
                trimmedLine.startsWith("[ar:") || 
                trimmedLine.startsWith("[al:") ||
                trimmedLine.startsWith("[by:")) {
                return@forEach
            }
            
            val match = timeRegex.find(trimmedLine)
            if (match != null) {
                try {
                    val minutes = match.groupValues[1].toLong()
                    val seconds = match.groupValues[2].toLong()
                    val millisStr = match.groupValues[3].padEnd(3, '0').take(3)
                    val millis = if (millisStr.isNotEmpty()) millisStr.toLong() else 0L
                    val text = match.groupValues[4].trim()
                    
                    if (text.isNotEmpty()) {
                        val timestampMs = (minutes * 60 * 1000) + (seconds * 1000) + millis
                        lyrics.add(LyricLine(timestampMs, text))
                        
                        Log.d(TAG, "Parsed: [$timestampMs ms] $text")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing line: $trimmedLine", e)
                }
            }
        }
        
        // Sort theo timestamp và tính endTime
        val sortedLyrics = lyrics.sortedBy { it.timestampMs }
        val finalLyrics = sortedLyrics.mapIndexed { index, line ->
            val nextTimestamp = sortedLyrics.getOrNull(index + 1)?.timestampMs ?: (line.timestampMs + 3000)
            line.copy(endTimeMs = nextTimestamp)
        }
        
        Log.d(TAG, "Total parsed: ${finalLyrics.size} lines")
        return finalLyrics
    }
    
    /**
     * Parse plain text (không có timestamps)
     * Mỗi dòng cách nhau 3 giây
     */
    fun parsePlainText(plainText: String?): List<LyricLine> {
        if (plainText.isNullOrBlank()) return emptyList()
        
        val lyrics = mutableListOf<LyricLine>()
        var currentTimeMs = 0L
        
        plainText.split("\n").forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty()) {
                lyrics.add(LyricLine(
                    timestampMs = currentTimeMs,
                    text = trimmed,
                    endTimeMs = currentTimeMs + 3000
                ))
                currentTimeMs += 3000
            }
        }
        
        return lyrics
    }
}
