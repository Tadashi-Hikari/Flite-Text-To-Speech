package com.example.flitetexttospeech

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader


open class NativeFliteEngine(context: Context, callback: SynthReadyCallback){
    private lateinit var mContext: Context
    private var mCallback: SynthReadyCallback? = null
    private var mDatapath: String? = null
    private var mInitialized = false

    init{
        System.loadLibrary("ttsflite")
        nativeClassInit()

        // This is loading from the Voice class
        var tempPath = convertAssetToFile("cmu_us_aew.flitevox").absolutePath
        mDatapath = tempPath
        mContext = context
        mCallback = callback
        attemptInit()
    }

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

    fun convertAssetToFile(filename: String): File {
        var suffix = ".temp"
        // This file needs to be tab separated columns
        var asset = mContext.applicationContext.assets.open(filename)
        var fileReader = InputStreamReader(asset)

        var tempFile = File.createTempFile(filename, suffix)
        var tempFileWriter = FileOutputStream(tempFile)
        // This is ugly AF
        var data = fileReader.read()
        while (data != -1) {
            tempFileWriter.write(data)
            data = fileReader.read()
        }
        // Do a little clean up
        asset.close()
        tempFileWriter.close()

        return tempFile
    }
}