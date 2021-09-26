package com.example.flitetexttospeech

import android.media.AudioFormat
import android.os.Environment
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.util.Log
import org.w3c.dom.Text
import java.io.File


/**
 * This is the entry for the service itself. The request goes to onSynthesizeText
 */

// This class is built in the native JNI, I'm just giving it a java interface
class FliteTextToSpeechService: TextToSpeechService(), TextToSpeech.OnInitListener{
    var language = "eng"
    var country = "USA"
    var variant = "male,rms"
    var initialized = false
    // This is given to us from an externa application (I think its the Android OS)
    lateinit var globalCallback: SynthesisCallback
    // This is our internal engines stuff.
    lateinit var globalSynthReadyCallback: SynthReadyCallback
    // well, I have to figure this the fuck out... I think I need to download the data elsewhere
    val FLITE_DATA_PATH = Environment.getExternalStorageDirectory()
        .toString() + "/flite-data/"

    override fun onCreate() {
        initalizeEngine()
        super.onCreate()
    }

    // Set up the state of the object
    fun initalizeEngine() {
        var Datapath = File(FLITE_DATA_PATH).getParent()
        globalSynthReadyCallback = SynthReadyCallback()
        if (initialized) {
            return
        }

        // This is being called from the companion object. I think it's the native engine being created
        if (!nativeCreate(Datapath)) {
            Log.e(this.javaClass.name,"Failed to initialize flite library")
            return
        }else {
            Log.i(this.javaClass.name, "Initialized Flite")
            initialized = true
        }
    }

    // This is the entry point for Android. This what the service is started with
    // SynthesisCallback should be provided by the requesting application
    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        val requestedLanguage = request?.language
        val requestedCountry = request?.country
        val requestedVariant = request?.variant
        // This is something that needed to be update for Android 11
        val text = request!!.charSequenceText.toString()
        val requestedSpeechRate = request.speechRate

        var result = onLoadLanguage(requestedLanguage, requestedCountry, requestedVariant)
        // If loading the language was successful, update the services information
        if(result == TextToSpeech.LANG_AVAILABLE) {
            language = requestedLanguage!!
            country = requestedCountry!!
            variant = requestedVariant!!
        }else{
            Log.e(this.javaClass.simpleName, "Could not set language for synthesis")
        }

        nativeSetSpeechRate(requestedSpeechRate)

        // I should probably null check around here
        val rate: Int = nativeGetSampleRate()
        Log.e(this.javaClass.simpleName, rate.toString())
        // This is used to let the calling app know we're streaming audio
        callback!!.start(nativeGetSampleRate(), AudioFormat.ENCODING_PCM_16BIT, 1)
        nativeSynthesize(text)
    }

    // This service is a system callback, letting Android know when the TTS Engine is good to go
    override fun onInit(status: Int) {
        Log.v(this.javaClass.name,"Is this for me? Or some other app component")
    }

    // I don't think this is required. It's a legacy thing
    override fun onGetLanguage(): Array<String> {
        return arrayOf(language,country,variant)
    }

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
          return nativeIsLanguageAvailable(lang!!, country!!, variant!!)
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        var boolResponse = nativeSetLanguage(lang!!,country!!,variant!!)
        // Unfortunately, the native toolkit doesn't give a good response. I'm defaulting to these.
        // I can probably fix the issue later
        when(boolResponse){
            true -> return TextToSpeech.LANG_AVAILABLE
            false -> return TextToSpeech.LANG_MISSING_DATA
        }
    }

    override fun onStop() {
        nativeStop()
    }

    // This is the method the native code uses to operate the SynthReadyCallback
    private fun nativeSynthCallback(audioData: ByteArray?) {
        // If we didn't get a callback, return....
        if (globalSynthReadyCallback == null) return

        // if there is no audio data, its done...?
        if (audioData == null) {
            globalSynthReadyCallback.onSynthDataComplete()
        } else {
            // else, we are good to take audio data?
            globalSynthReadyCallback.onSynthDataReady(audioData)
        }
    }

    // This object is used by the native code to manipulate stuff. I don't really see why it needs to be a class though
    inner class SynthReadyCallback(){
        // This was an override, but I just implemented it directly. I don't see why it would be an issue
        fun onSynthDataComplete(){
            globalCallback.done()
        }

        // This was an override, but I just implemented it directly. I don't see why it would be an issue
        fun onSynthDataReady(audioData: ByteArray?) {
            if (audioData == null || audioData.size == 0) {
                onSynthDataComplete()
                return
            }
            // These !! are a code smell. i don't like it
            val maxBytesToCopy: Int = globalCallback.getMaxBufferSize()
            var offset = 0
            while (offset < audioData.size) {
                val bytesToWrite = Math.min(maxBytesToCopy, audioData.size - offset)
                globalCallback.audioAvailable(audioData, offset, bytesToWrite)
                offset += bytesToWrite
            }
        }
    }

    // This is just a companion object in order to reference the native classes
    companion object{
        init {
            System.loadLibrary("ttsflite")
            // This syncs the native classes w/ the Java classes
            nativeClassInit()
        }

        // These are native classes, which are handled in the jni for the application
        val mNativeData = 0
        external fun nativeClassInit(): Boolean
        // This is called during the initialization
        external fun nativeCreate(path: String): Boolean
        external fun nativeDestroy(): Boolean
        external fun nativeIsLanguageAvailable(language: String, country: String, variant: String): Int
        external fun nativeSetLanguage(language: String, country: String, variant: String): Boolean
        external fun nativeSetSpeechRate(rate: Int): Boolean
        external fun nativeGetSampleRate(): Int
        external fun nativeSynthesize(text: String): Boolean
        external fun nativeStop(): Boolean
        external fun nativeGetBenchmark(): Float
    }
}