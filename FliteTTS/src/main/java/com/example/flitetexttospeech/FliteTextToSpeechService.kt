package com.example.flitetexttospeech


import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeechService

/**
 * This is the entry for the service itself. The request goes to onSynthesizeText
 */

class FliteTextToSpeechService: TextToSpeechService(){

    lateinit var textToSpeech: FliteTextToSpeech
    lateinit var fliteEngine: NativeFliteEngine

    override fun onCreate() {
        super.onCreate()
        textToSpeech = FliteTextToSpeech(this)
    }

    override fun onGetLanguage(): Array<String> {
        TODO("Not yet implemented")
    }

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        TODO("Not yet implemented")
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        TODO("Not yet implemented")
    }

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        TODO("Not yet implemented")
    }

    override fun onStop() {
        TODO("Not yet implemented")
    }
}