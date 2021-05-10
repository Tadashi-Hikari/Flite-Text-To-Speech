package com.example.flitetexttospeech

import android.content.Context
import android.media.AudioFormat
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.util.Log



class FliteTextToSpeech(cont: Context): TextToSpeech.OnInitListener{
    lateinit var context: Context
    lateinit var textToSpeech: TextToSpeech

    init{
        context = cont
        textToSpeech = TextToSpeech(context,this,"com.example.flitetexttospeech")
    }

    override fun onInit(status: Int){
        //Well, I suppose it can actually start listening now?
    }

    var engine: NativeFliteEngine? = null
    // I really don't see where this is ever initialized....
    private var mCallback: android.speech.tts.SynthesisCallback? = null
    private val DEFAULT_LANGUAGE = "eng"
    private val DEFAULT_COUNTRY = "USA"
    private val DEFAULT_VARIANT = "male,rms"

    private var mCountry = DEFAULT_COUNTRY
    private var mLanguage = DEFAULT_LANGUAGE
    private var mVariant = DEFAULT_VARIANT
    private val mAvailableVoices: Any? = null

    // This is the local implementation of the FlightEngine interface
    inner class SynthReadyCallback(callback: android.speech.tts.SynthesisCallback): NativeFliteEngine.SynthReadyCallback{
        private val localCallback = callback

        override fun onSynthDataComplete(){
            localCallback.done()
        }

        override fun onSynthDataReady(audioData: ByteArray?) {
            if (audioData == null || audioData.size == 0) {
                onSynthDataComplete()
                return
            }
            // These !! are a code smell. i don't like it
            val maxBytesToCopy: Int = localCallback.getMaxBufferSize()
            var offset = 0
            while (offset < audioData.size) {
                val bytesToWrite = Math.min(maxBytesToCopy, audioData.size - offset)
                localCallback.audioAvailable(audioData, offset, bytesToWrite)
                offset += bytesToWrite
            }
        }
    }

    fun onGetLanguage(): Array<String> {
        return arrayOf(
            mLanguage, mCountry, mVariant
        )
    }

    fun onIsLanguageAvailable(language: String?, country: String?, variant: String?): Int {
        return engine!!.isLanguageAvailable(language!!, country!!, variant!!);
    }

    fun onLoadLanguage(language: String?, country: String?, variant: String?): Int {
        return engine!!.isLanguageAvailable(language!!, country!!, variant!!)
    }

    fun onSynthesizeText(request: SynthesisRequest?, callback: android.speech.tts.SynthesisCallback?) {
        val language = request!!.language
        val country = request.country
        val variant = request.variant
        val text = request.text
        val speechrate = request.speechRate

        var result = true

        if (!(mLanguage === language &&
                    mCountry === country &&
                    mVariant === variant)
        ) {
            result = engine!!.setLanguage(language, country, variant)
            mLanguage = language
            mCountry = country
            mVariant = variant
        }

        if (!result) {
            Log.e(this.javaClass.simpleName,"Could not set language for synthesis")
            return
        }

        engine!!.setSpeechRate(speechrate)

        mCallback = callback
        val rate: Int = engine!!.getSampleRate()
        Log.e(this.javaClass.simpleName, rate.toString())
        mCallback!!.start(engine!!.getSampleRate(), AudioFormat.ENCODING_PCM_16BIT, 1)
        engine!!.synthesize(text)
    }
}