package com.cowinclub.dingdong.mediademo.Media

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.os.Looper
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class WindEar {
    companion object {
        private val tag: String = "WindEar"
        private val TEMP_FOLDER_NAME: String = "WindEar"
        private val RECORD_AUDIO_BUFFER_TIMES: Int = 1
        private val PLAY_AUDIO_BUFFER_TIMES: Int = 1
        private val AUDIO_FREQUENCY: Int = 44100

        private val RECORD_CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_IN_STEREO
        private val PLAY_CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_OUT_STEREO
        private val AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT


        private lateinit var cachePCMFolder: String

        private lateinit var waveFloderPath: String


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

    private class AudioRecordThread() : Thread() {
        private lateinit var aRecord: AudioRecord
        private var bufferSize: Int = 10240
        private var createWav: Boolean = false
        private lateinit var tmpPcmFile: File
        private lateinit var tmpWaveFile: File

        constructor(createWav: Boolean, tmpPcmFile: File, tmpWaveFile: File) : this() {
            this.createWav = createWav
            this.tmpPcmFile = tmpPcmFile
            this.tmpWaveFile = tmpWaveFile
            bufferSize = AudioRecord.getMinBufferSize(AUDIO_FREQUENCY, RECORD_CHANNEL_CONFIG,
                    AUDIO_ENCODING) * RECORD_AUDIO_BUFFER_TIMES
            aRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                    AUDIO_FREQUENCY, RECORD_CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize)
        }

        override fun run() {
            var pcmFos = FileOutputStream(tmpPcmFile)
            var wavePos = FileOutputStream(tmpWaveFile)

            if (createWav) {
                MediaUtils.writeWaveFileHeader(wavePos, bufferSize.toLong(), AUDIO_FREQUENCY, aRecord.channelCount)
            }
            aRecord.startRecording()
            var byteBuffer: ByteArray = ByteArray(bufferSize)
            while (state.equals(WindState.RECODING) && !isInterrupted) {
                var end: Int = aRecord.read(byteBuffer, 0, byteBuffer.size)
                pcmFos.write(byteBuffer, 0, end)
                pcmFos.flush()
                if (createWav) {
                    wavePos.write(byteBuffer, 0, end)
                    wavePos.flush()
                }
            }
            aRecord.stop()
            pcmFos.close()
            wavePos.close()
            if (createWav) {
                var wareRaf = RandomAccessFile(tmpWaveFile,"rw")
                var header = MediaUtils.genetateWaveFileHeader(tmpPcmFile.length(), AUDIO_FREQUENCY,aRecord.channelCount)
                wareRaf.seek(0)
                wareRaf.write(header)
                wareRaf.close()
            }


        }
    }


}