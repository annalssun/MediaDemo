package com.cowinclub.dingdong.mediademo.Media

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import com.cowinclub.dingdong.mediademo.Media.WindEar.Companion.AUDIO_ENCODING
import com.cowinclub.dingdong.mediademo.Media.WindEar.Companion.AUDIO_FREQUENCY
import com.cowinclub.dingdong.mediademo.Media.WindEar.Companion.PLAY_CHANNEL_CONFIG
import java.io.File
import java.io.FileInputStream

class AudioTrackPlayThread() : Thread() {
    private lateinit var track: AudioTrack
    private var bufferSize = 10240
    private lateinit var audioFile: File
    private lateinit var state: WindEar.WindState
    private lateinit var windEar: WindEar
    private lateinit var mHandler: Handler

    private lateinit var mDecoder: AudioDecodeController

    private var accType = false

    private var fis: FileInputStream? = null

    constructor(file: File, windEar: WindEar, handler: Handler, accType: Boolean) : this() {
        priority = Thread.MAX_PRIORITY

        this.audioFile = file
        this.windEar = windEar
        this.mHandler = handler
        if (accType) {
            this.accType = true
            buildACCTrack()
        } else {
            buildDefaultTrack()
        }
        init()
    }

    constructor(file: File, windEar: WindEar, handler: Handler) : this(file, windEar, handler, false)

    private fun buildDefaultTrack() {
        bufferSize = AudioTrack.getMinBufferSize(WindEar.AUDIO_FREQUENCY, WindEar.PLAY_CHANNEL_CONFIG,
                WindEar.AUDIO_ENCODING) * WindEar.PLAY_AUDIO_BUFFER_TIMES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            track = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(WindEar.AUDIO_FREQUENCY)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build())
                    .setBufferSizeInBytes(bufferSize)
                    .build()
        } else {
            track = AudioTrack(AudioManager.STREAM_MUSIC,
                    AUDIO_FREQUENCY,
                    PLAY_CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize,
                    AudioTrack.MODE_STREAM)
        }
    }

    private fun buildACCTrack() {//初始化ACC解码器
        mDecoder = AudioDecodeController()
        bufferSize = AudioTrack.getMinBufferSize(mDecoder.sampleRate, mDecoder.changelConfig,
                WindEar.AUDIO_ENCODING) * WindEar.PLAY_AUDIO_BUFFER_TIMES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            track = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(mDecoder.sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build())
                    .setBufferSizeInBytes(bufferSize)
                    .build()
        } else {
            track = AudioTrack(AudioManager.STREAM_MUSIC,
                    mDecoder.sampleRate,
                    mDecoder.changelConfig, AUDIO_ENCODING, bufferSize,
                    AudioTrack.MODE_STREAM)
        }

        mDecoder.initAudioTrack(track)
    }

    private fun init() {
        fis = FileInputStream(this.audioFile)
        state = WindEar.WindState.PLAYING
        notifySateChange(state)
    }

    private fun changeState(changeState: WindEar.WindState) {
        state = changeState
    }

    override fun run() {
        super.run()
        if (accType){
            mDecoder.startPlay()
        }else{
            palyDefault()
        }
            state = WindEar.WindState.STOP_PLAY
        notifySateChange(state)
        state = WindEar.WindState.IDLE
        notifySateChange(state)

    }

    fun notifySateChange(state: WindEar.WindState) {
        mHandler.post {
            windEar.notifyStateChange(state)
        }
    }

    fun stopPlaying() {
        if (track.state.equals(AudioTrack.PLAYSTATE_PLAYING)) {
            track.stop()
            track.release()
            fis!!.close()
            state = WindEar.WindState.STOP_PLAY
            notifySateChange(state)
            state = WindEar.WindState.IDLE
            notifySateChange(state)
        }
    }

    private fun palyDefault() {
        try {
            val byteBuf = ByteArray(bufferSize)
            track.play()
            while (state.equals(WindEar.WindState.PLAYING) && fis!!.read(byteBuf) > 0) {
                track.write(byteBuf, 0, byteBuf.size)
            }
            track.stop()
            track.release()
        } catch (e: Exception) {
            notifySateChange(WindEar.WindState.ERROR)
        }

        fis!!.close()
    }


}