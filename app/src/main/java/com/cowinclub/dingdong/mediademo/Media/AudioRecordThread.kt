package com.cowinclub.dingdong.mediademo.Media

import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class AudioRecordThread() : Thread() {
    private lateinit var aRecord: AudioRecord
    private var bufferSize: Int = 10240
    private var createWav: Boolean = false
    private lateinit var tmpPcmFile: File
    private lateinit var tmpWaveFile: File
    private lateinit var state: WindEar.WindState
    private lateinit var windEar: WindEar

    private var pcmFos: FileOutputStream? = null
    private var waveFos: FileOutputStream? = null
    private var waveRaf: RandomAccessFile? = null
    private lateinit var mHandler: Handler

    constructor(createWav: Boolean, tmpPcmFile: File
                , tmpWaveFile: File, windEar: WindEar,handler: Handler) : this() {
        this.createWav = createWav
        this.tmpPcmFile = tmpPcmFile
        this.tmpWaveFile = tmpWaveFile
        this.windEar = windEar
        this.mHandler = handler
        bufferSize = AudioRecord.getMinBufferSize(WindEar.AUDIO_FREQUENCY, WindEar.RECORD_CHANNEL_CONFIG,
                WindEar.AUDIO_ENCODING) * WindEar.RECORD_AUDIO_BUFFER_TIMES
        aRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                WindEar.AUDIO_FREQUENCY, WindEar.RECORD_CHANNEL_CONFIG, WindEar.AUDIO_ENCODING, bufferSize)
        init()
    }

    fun init() {//
        pcmFos = FileOutputStream(tmpPcmFile)
        waveFos = FileOutputStream(tmpWaveFile)
        state = WindEar.WindState.RECODING
        notifySateChange(state)
    }

    fun notifySateChange(state:WindEar.WindState){
        mHandler.post {
            windEar.notifyStateChange(state)
        }
    }

    override fun run() {
        try {
            if (createWav) {
                MediaUtils.writeWaveFileHeader(waveFos!!, bufferSize.toLong(), WindEar.AUDIO_FREQUENCY, aRecord.channelCount)
            }
            aRecord.startRecording()
            var byteBuffer: ByteArray = ByteArray(bufferSize)
            while (state.equals(WindEar.WindState.RECODING) && !isInterrupted) {
                var end: Int = aRecord.read(byteBuffer, 0, byteBuffer.size)
                pcmFos!!.write(byteBuffer, 0, end)
                pcmFos!!.flush()
                if (createWav) {
                    waveFos!!.write(byteBuffer, 0, end)
                    waveFos!!.flush()
                }
            }
            aRecord.stop()
            pcmFos!!.close()
            waveFos!!.close()
            if (createWav) {
                waveRaf = RandomAccessFile(tmpWaveFile, "rw")
                val header = MediaUtils.genetateWaveFileHeader(tmpPcmFile.length(), WindEar.AUDIO_FREQUENCY, aRecord.channelCount)
                waveRaf!!.seek(0)
                waveRaf!!.write(header)
                waveRaf!!.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            notifySateChange(WindEar.WindState.ERROR)
        }
        state = WindEar.WindState.STOP_RECOD
        notifySateChange(state)
        state = WindEar.WindState.IDLE
        notifySateChange(state)
    }

    fun stopRecording() {
        if (aRecord.recordingState.equals(AudioRecord.RECORDSTATE_RECORDING)) {
//            aRecord.stop()
//            pcmFos!!.close()
//            waveFos!!.close()
//            waveRaf!!.close()
//            state = WindEar.WindState.STOP_RECOD
//            notifySateChange(state)
//            state = WindEar.WindState.IDLE
//            notifySateChange(state)
            state = WindEar.WindState.STOP_RECOD
        }
    }
}