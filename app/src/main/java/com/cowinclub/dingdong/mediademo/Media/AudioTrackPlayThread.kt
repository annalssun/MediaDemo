package com.cowinclub.dingdong.mediademo.Media

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import com.cowinclub.dingdong.mediademo.Media.WindEar.Companion.AUDIO_ENCODING
import com.cowinclub.dingdong.mediademo.Media.WindEar.Companion.AUDIO_FREQUENCY
import com.cowinclub.dingdong.mediademo.Media.WindEar.Companion.PLAY_CHANNEL_CONFIG
import java.io.File

class AudioTrackPlayThread() : Thread() {
    private lateinit var track: AudioTrack
    private var bufferSize = 10240
    private lateinit var audioFile: File

    constructor(file: File) : this() {
        priority = Thread.MAX_PRIORITY
        bufferSize = AudioTrack.getMinBufferSize(WindEar.AUDIO_FREQUENCY, WindEar.PLAY_CHANNEL_CONFIG,
                WindEar.AUDIO_ENCODING) * WindEar.PLAY_AUDIO_BUFFER_TIMES

//        track = AudioTrack(AudioManager.STREAM_MUSIC,WindEar.PLAY_CHANNEL_CONFIG,
//                bufferSize,AudioTrack.MODE_STREAM)
//        track = AudioTrack.Builder()
//                .setAudioFormat()
//                .setBufferSizeInBytes(bufferSize)
//                .build()
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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            track = AudioTrack(,
                    AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(WindEar.AUDIO_FREQUENCY)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build(),
                    bufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                   )
        }else{
            track = AudioTrack(AudioManager.STREAM_MUSIC,
                    AUDIO_FREQUENCY,
                    PLAY_CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize,
                    AudioTrack.MODE_STREAM)
        }

    }


}