package com.example.flitetexttospeech

import android.content.Context
import android.speech.tts.Voice
import android.util.Log
import java.io.File


open class FliteEngine(context: Context, callback: SynthReadyCallback){
    fun FliteEngine(context: Context, callback: SynthReadyCallback) {
        System.loadLibrary("ttsflite")
        nativeClassInit()

        mDatapath = File(context.filesDir,"Something?").toString()
        mContext = context
        mCallback = callback
        attemptInit()
    }

    private lateinit var mContext: Context
    private var mCallback: SynthReadyCallback? = null
    private var mDatapath: String? = null
    private var mInitialized = false



    protected fun finalize() {
        nativeDestroy()
    }

    fun isLanguageAvailable(language: String, country: String, variant: String): Int {
        return nativeIsLanguageAvailable(language, country, variant)
    }

    fun setLanguage(language: String, country: String, variant: String): Boolean {
        attemptInit()
        return nativeSetLanguage(language, country, variant)
    }

    fun getSampleRate(): Int {
        return nativeGetSampleRate()
    }

    fun setSpeechRate(rate: Int): Boolean {
        return nativeSetSpeechRate(rate)
    }

    fun synthesize(text: String) {
        nativeSynthesize(text)
    }

    fun stop() {
        nativeStop()
    }

    fun getNativeBenchmark(): Float {
        return nativeGetBenchmark()
    }

    private fun nativeSynthCallback(audioData: ByteArray?) {
        when{
            mCallback == null -> return
            // I can trust that it isn't null, but I think this may be a codesmell
            audioData == null -> mCallback!!.onSynthDataComplete()
            else -> mCallback!!.onSynthDataReady(audioData)
        }
    }

    private fun attemptInit() {
        if (mInitialized) {
            return
        }
        if (!nativeCreate(mDatapath)) {
            Log.e(this.javaClass.name, "Failed to initialize flite library")
            return
        }
        Log.i(this.javaClass.name, "Initialized Flite")
        mInitialized = true
    }

    private val mNativeData = 0
    private external fun nativeClassInit(): Boolean
    private external fun nativeCreate(path: String?): Boolean
    private external fun nativeDestroy(): Boolean
    private external fun nativeIsLanguageAvailable(
        language: String,
        country: String,
        variant: String
    ): Int

    private external fun nativeSetLanguage(
        language: String,
        country: String,
        variant: String
    ): Boolean

    private external fun nativeSetSpeechRate(rate: Int): Boolean
    private external fun nativeGetSampleRate(): Int
    private external fun nativeSynthesize(text: String): Boolean
    private external fun nativeStop(): Boolean

    private external fun nativeGetBenchmark(): Float


    interface SynthReadyCallback{
        fun onSynthDataReady(audioData: ByteArray?)
        fun onSynthDataComplete()
    }
}