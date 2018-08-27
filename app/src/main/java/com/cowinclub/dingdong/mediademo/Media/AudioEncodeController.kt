package com.cowinclub.dingdong.mediademo.Media

import android.annotation.TargetApi
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import java.io.FileOutputStream
import java.io.OutputStream

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class AudioEncodeController() {

    private var mEncoder: MediaCodec? = null
    private var mFileStream: OutputStream? = null
    private var minBufferSize = 10240

    init {
        minBufferSize = AudioRecord.getMinBufferSize(WindEar.AUDIO_FREQUENCY, WindEar.RECORD_CHANNEL_CONFIG,
                WindEar.AUDIO_ENCODING) * WindEar.RECORD_AUDIO_BUFFER_TIMES
        init()
    }

    private fun init() {
        try {
            mEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1)
            format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC)
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 96000)
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, minBufferSize * 2)
            mEncoder?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mFileStream = FileOutputStream(MediaUtils.getSDPath() + "/acc_encode.mp4")
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun startRead(){
        mEncoder?.start()
    }

    fun read(len: Int, byteArray: ByteArray): Boolean {

        val inputBufferIndex = mEncoder?.dequeueInputBuffer(10000)
        if (inputBufferIndex!! >= 0) {
            val inputBuffer = mEncoder?.getInputBuffer(inputBufferIndex)
            inputBuffer?.clear()
            inputBuffer?.put(byteArray)
            inputBuffer?.limit(len)
            mEncoder?.queueInputBuffer(inputBufferIndex, 0, len, System.nanoTime(), 0)
            return true
        }
        return false
    }

    fun write() {
        val bufferInfo = MediaCodec.BufferInfo()
        var outputIndex = mEncoder?.dequeueOutputBuffer(bufferInfo, 0)
        while (outputIndex!! > 0) {
            val outBitsSize = bufferInfo.size
            val outPacketSize = outBitsSize + 7 //ADTS头部7个字节
            val outputBuffer = mEncoder?.getOutputBuffer(outputIndex)
            outputBuffer?.position(bufferInfo.offset)
            outputBuffer?.limit(bufferInfo.offset + outBitsSize)

            val outData = ByteArray(outPacketSize)
            addADTStoPacket(outData, outPacketSize)

            outputBuffer?.get(outData, 7, outBitsSize)
            outputBuffer?.position(bufferInfo.offset)
            mFileStream?.write(outData)
            mEncoder?.releaseOutputBuffer(outputIndex, false)
            outputIndex = mEncoder?.dequeueOutputBuffer(bufferInfo, 0)
        }

    }

    private fun addADTStoPacket(packet: ByteArray, len: Int) {
        val profile = 2 //AAC LC
        val freqIdx = 4 //44100
        val chanCfg = 2 //CPE

        packet[0] = (0xFF).toByte()
        packet[1] = (0xF9).toByte()
        packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (len shr 11)).toByte()
        packet[4] = (len and 0x7FF shr 3).toByte()
        packet[5] = ((len and 7 shl 5) + 0x1F).toByte()
        packet[6] = (0XFC).toByte()
    }

    //释放资源
    fun release() {
        mEncoder?.stop()
        mEncoder?.release()
        mEncoder = null

        mFileStream?.flush()
        mFileStream?.close()
        mFileStream = null
    }


}