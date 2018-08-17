package com.cowinclub.dingdong.mediademo.Media

import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class AudioRecordThread():Thread(){
    private lateinit var aRecord: AudioRecord
    private var bufferSize: Int = 10240
    private var createWav: Boolean = false
    private lateinit var tmpPcmFile: File
    private lateinit var tmpWaveFile: File

    constructor(createWav: Boolean, tmpPcmFile: File, tmpWaveFile: File) : this() {
        this.createWav = createWav
        this.tmpPcmFile = tmpPcmFile
        this.tmpWaveFile = tmpWaveFile
        bufferSize = AudioRecord.getMinBufferSize(WindEar.AUDIO_FREQUENCY, WindEar.RECORD_CHANNEL_CONFIG,
                WindEar.AUDIO_ENCODING) * WindEar.RECORD_AUDIO_BUFFER_TIMES
        aRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                WindEar.AUDIO_FREQUENCY, WindEar.RECORD_CHANNEL_CONFIG, WindEar.AUDIO_ENCODING, bufferSize)
    }

    override fun run() {
        var pcmFos = FileOutputStream(tmpPcmFile)
        var wavePos = FileOutputStream(tmpWaveFile)

        if (createWav) {
            MediaUtils.writeWaveFileHeader(wavePos, bufferSize.toLong(), WindEar.AUDIO_FREQUENCY, aRecord.channelCount)
        }
        aRecord.startRecording()
        var byteBuffer: ByteArray = ByteArray(bufferSize)
        while (state.equals(WindEar.WindState.RECODING) && !isInterrupted) {
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
            var header = MediaUtils.genetateWaveFileHeader(tmpPcmFile.length(), WindEar.AUDIO_FREQUENCY,aRecord.channelCount)
            wareRaf.seek(0)
            wareRaf.write(header)
            wareRaf.close()
        }


    }
}