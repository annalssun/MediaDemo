package com.cowinclub.dingdong.mediademo.Media

import android.annotation.TargetApi
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import java.nio.ByteBuffer

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class AudioDecodeController constructor() {

    private var mExtractor: MediaExtractor? = null
    private var mDecoder: MediaCodec? = null
    private var mPlayer: AudioTrack? = null

    var sampleRate = 0
    var changelConfig = 0
    var selectTrack = 0
    var mine = MediaFormat.MIMETYPE_AUDIO_AAC
    var mFormate: MediaFormat? = null

    init {
        initSource()
    }

    private fun initSource() {
        try {
            mExtractor = MediaExtractor()
            mExtractor?.setDataSource(MediaUtils.getSDPath() + "/acc_encode.mp4")


            val trackCounts = mExtractor?.trackCount
            for (i in 0 until trackCounts!!) {
                mFormate = mExtractor?.getTrackFormat(i)
                mine = mFormate?.getString(MediaFormat.KEY_MIME)!!
                if (mine.startsWith("audio/") ) {
                    selectTrack = i
                    sampleRate = mFormate?.getInteger(MediaFormat.KEY_SAMPLE_RATE)!!
                    changelConfig = mFormate?.getInteger(MediaFormat.KEY_CHANNEL_COUNT)!!
                    break
                }
            }

            mExtractor?.selectTrack(selectTrack)
            mDecoder = MediaCodec.createDecoderByType(mine)
            mDecoder?.configure(mFormate, null, null, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initAudioTrack(audioTrack: AudioTrack) {
        this.mPlayer = audioTrack
    }



    fun startPlay() {
        mPlayer?.play()
        mDecoder?.start()
        var isFinish = false
        var bufferInfo = MediaCodec.BufferInfo()
        while (!isFinish) {
            var inputIndex = mDecoder?.dequeueInputBuffer(10000)
            if (inputIndex!! < 0) {
                isFinish = true
            }
            var inputBuffer = mDecoder?.getInputBuffer(inputIndex)
            inputBuffer?.clear()
            var sampleSize = mExtractor?.readSampleData(inputBuffer, 0)
            if (sampleSize!! > 0) {
                mDecoder?.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0)
                mExtractor?.advance()
            } else {
                isFinish = true
            }

            var outputIndex = mDecoder?.dequeueOutputBuffer(bufferInfo, 10000)
            var outputBuffer: ByteBuffer? = null
            var chunkPCM: ByteArray? = null
            while (outputIndex!! >= 0) {
                outputBuffer = mDecoder?.getOutputBuffer(outputIndex)
                chunkPCM = ByteArray(bufferInfo.size)
                outputBuffer?.get(chunkPCM)
                outputBuffer?.clear()
                mPlayer?.write(chunkPCM, 0, bufferInfo.size)
                mDecoder?.releaseOutputBuffer(outputIndex, false)
                outputIndex = mDecoder?.dequeueOutputBuffer(bufferInfo, 10000)
            }
        }

        release()
    }

    private fun release() {
        mDecoder?.stop()
        mDecoder?.release()
        mDecoder = null

        mExtractor?.release()
        mExtractor = null

        mPlayer?.stop()
        mPlayer?.release()
        mPlayer = null
    }
}