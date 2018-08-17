package com.cowinclub.dingdong.mediademo.Media

import android.media.AudioFormat
import android.os.Environment
import android.os.Handler
import android.os.Looper
import java.io.File

class WindEar {
    companion object {
        private val tag: String = "WindEar"
        val TEMP_FOLDER_NAME: String = "WindEar"
        val RECORD_AUDIO_BUFFER_TIMES: Int = 1
        val PLAY_AUDIO_BUFFER_TIMES: Int = 1
        val AUDIO_FREQUENCY: Int = 44100

        val RECORD_CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_IN_STEREO
        val PLAY_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO
        val AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT


        lateinit var cachePCMFolder: String

        lateinit var waveFloderPath: String


        fun init() {
            cachePCMFolder = Environment.getExternalStorageDirectory().absolutePath + File.separator + TEMP_FOLDER_NAME
            val folder = File(cachePCMFolder)
            if (!folder.exists()) {
                folder.mkdirs()
            } else {
                for (f in folder.listFiles()) {
                    f.delete()
                }
            }

            waveFloderPath = Environment.getExternalStorageDirectory().absolutePath + File.separator + TEMP_FOLDER_NAME
            val waveFileDir = File(waveFloderPath)
            if (!waveFileDir.exists()) {
                waveFileDir.mkdirs()
            }
        }

    }

    @Volatile
    var state = WindState.IDLE
    lateinit var tmpPCMFile: File
    lateinit var tmpWavFile: File
    private val mainHandler: Handler = Handler(Looper.getMainLooper())


    enum class WindState {
        ERROR, IDLE, RECODING, STOP_RECOD, PLAYING, STOP_PLAY
    }



}