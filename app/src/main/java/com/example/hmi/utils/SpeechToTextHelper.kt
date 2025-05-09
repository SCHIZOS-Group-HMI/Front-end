package com.example.hmi.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechToTextHelper(
    context: Context,
    private val onResult: (String) -> Unit
) : RecognitionListener {

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(this@SpeechToTextHelper)
    }

    private val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    fun startListening() {
        recognizer.startListening(intent)
    }

    fun stopListening() {
        recognizer.stopListening()
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onError(error: Int) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onPartialResults(partial: Bundle?) {
        val matches = partial
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        onResult(matches?.firstOrNull().orEmpty())
    }

    override fun onResults(results: Bundle?) {
        val matches = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        onResult(matches?.firstOrNull().orEmpty())
    }
}
