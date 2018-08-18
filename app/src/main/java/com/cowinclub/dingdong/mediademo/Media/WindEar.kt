package com.cowinclub.dingdong.mediademo.Media

import android.media.AudioFormat
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class WindEar private constructor() {
    companion object {
        var WINDEAR_MSG = 10001

        private val tag: String = "WindEar"
        val TEMP_FOLDER_NAME: String = "WindEar"
        val RECORD_AUDIO_BUFFER_TIMES: Int = 1
        val PLAY_AUDIO_BUFFER_TIMES: Int = 1
        val AUDIO_FREQUENCY: Int = 44100

        val RECORD_CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_IN_STEREO
        val PLAY_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO
        val AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT


        private lateinit var cachePCMFolder: String

        private lateinit var waveFloderPath: String

        val instance: WindEar by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            WindEar()
        }

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
    private var state = WindState.IDLE
    private lateinit var tmpPCMFile: File
    private lateinit var tmpWavFile: File
    private var handler = Handler(Looper.getMainLooper())
    private var aRecordThread: AudioRecordThread? = null
    private var aTrackPlayThread: AudioTrackPlayThread? = null
    private var createWare: Boolean = true


    @Synchronized
    fun startRecord() {
        if (!state.equals(WindState.IDLE)) {
            return
        }

        try {
            tmpPCMFile = File.createTempFile("recording", ".pcm", File(cachePCMFolder))
            if (createWare) {
                var sdf = SimpleDateFormat("yyMMdd_HHmmss", Locale(Locale.getDefault().language))
                tmpWavFile = File(waveFloderPath + File.pathSeparator + "r" + sdf.format(Date(System.currentTimeMillis())))
            }
            if (tmpPCMFile.exists()){
                Log.i("WindEar","===========================tmpPCMFile创建成功")
            }
            if (tmpWavFile.exists()){
                Log.i("WindEar","===========================tmpWavFile创建成功")
            }

            if (aRecordThread != null) {
                aRecordThread!!.interrupt()
                aRecordThread = null
            }

            aRecordThread = AudioRecordThread(createWare, tmpPCMFile, tmpWavFile, this, handler)
            aRecordThread!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun stopRecord() {

        if (!state.equals(WindState.RECODING)) {
            return
        }
        aRecordThread!!.stopRecording()
    }

    @Synchronized
    fun startPlayPcm() {
        if (!state.equals(WindState.IDLE)) {
            return
        }
        if (0 == tmpPCMFile.length().toInt()){
            Log.i("WindEar","===========================tmpPCMFile长度为0")
            return
        }
        aTrackPlayThread = AudioTrackPlayThread(tmpPCMFile, this, handler)
        aTrackPlayThread!!.start()
    }

    @Synchronized
    fun startPlayWave() {
        if (!state.equals(WindState.IDLE)) {
            return
        }
        aTrackPlayThread = AudioTrackPlayThread(tmpWavFile, this, handler)
        aTrackPlayThread!!.start()
    }

    @Synchronized
    fun stopPlay() {
        if (state.equals(WindState.PLAYING)) {
            return
        }
        aTrackPlayThread!!.stopPlaying()
    }

    fun notifyStateChange(state: WindState) {
        this.state = state
    }

    enum class WindState {
        ERROR, IDLE, RECODING, STOP_RECOD, PLAYING, STOP_PLAY
    }


    @Synchronized
    fun changeState(state: WindState) {
        this.state = state
    }


}