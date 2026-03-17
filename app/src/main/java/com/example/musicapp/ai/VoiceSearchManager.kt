package com.example.musicapp.ai

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment

class VoiceSearchManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var onResult: ((String) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null
    private var onListening: ((Boolean) -> Unit)? = null

    companion object {
        private const val TAG = "VoiceSearchManager"
        const val REQUEST_CODE_SPEECH = 9999
    }

    /**
     * Method 1: Sử dụng SpeechRecognizer (cho máy thật)
     */
    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onListening: (Boolean) -> Unit = {}
    ) {
        this.onResult = onResult
        this.onError = onError
        this.onListening = onListening

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Voice recognition not available on this device")
            return
        }

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                    onListening(true)
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Speech started")
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    Log.d(TAG, "Speech ended")
                    onListening(false)
                }

                override fun onError(error: Int) {
                    onListening(false)
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                        else -> "Unknown error: $error"
                    }
                    Log.e(TAG, "Error: $errorMessage")
                    onError(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0]
                        Log.d(TAG, "Recognized: $spokenText")
                        onResult(spokenText)
                    } else {
                        onError("No speech recognized")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        Log.d(TAG, "Partial: ${matches[0]}")
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting speech recognition: ${e.message}", e)
            onError("Failed to start voice recognition: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            onListening?.invoke(false)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition: ${e.message}", e)
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognizer: ${e.message}", e)
        }
    }

    /**
     * Method 2: Sử dụng Intent (cho cả Emulator và máy thật)
     * Hiển thị Google Voice Search UI
     */
    fun startVoiceSearchWithIntent(activity: Activity) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói tên bài hát, ca sĩ...")
        }

        try {
            activity.startActivityForResult(intent, REQUEST_CODE_SPEECH)
        } catch (e: Exception) {
            Log.e(TAG, "Voice search not supported: ${e.message}", e)
            onError?.invoke("Voice search not available on this device")
        }
    }

    fun startVoiceSearchWithIntent(fragment: Fragment, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "🎤 Nói tên bài hát, ca sĩ hoặc thể loại...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        try {
            launcher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Voice search error: ${e.message}", e)
            onError?.invoke("Voice search not available: ${e.message}")
        }
    }

    /**
     * Xử lý kết quả từ Intent-based voice search
     */
    fun handleVoiceSearchResult(data: Intent?): String? {
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        return results?.firstOrNull()
    }
}
