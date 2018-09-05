package com.cowinclub.dingdong.mediademo.openGLMedia.egl

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.view.Surface
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class EncodeRecordController : MediaCodec.Callback {


    private var mWidth = 0
    private var mHeight = 0
    private var mCodec: MediaCodec? = null
    private var mOutputStream: BufferedOutputStream? = null
    private var mMuxer: MediaMuxer? = null

    lateinit var mEncoderSurface: Surface
    private var path = Environment.getExternalStorageDirectory().absolutePath + "/mcodecv26.264"
    private var mOutputPath = Environment.getExternalStorageDirectory().absolutePath + "/mcodecmux26.mp4"

    constructor(width: Int, height: Int) {
        this.mHeight = height
        this.mWidth = width
        setUpController()
    }

    fun setUpController() {

        createFile()

        try {
            mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 500000);//500kbps
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        //mediaFormat.setInteger("bitrate-mode", 2);
        //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mCodec?.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mEncoderSurface = mCodec?.createInputSurface()!!
        mCodec?.setCallback(this)

        try {
            mMuxer = MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun createFile() {
        for (i in 0 until 2) {
            var file: File? = null
            if (i == 0) {
                file = File(path)
            } else if (i == 1) {
                file = File(path)
            }

            if (!file?.exists()!!) {
                try {
                    file.createNewFile()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                if (!file.delete()) {
                    try {
                        file.createNewFile()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (i == 0) {
                try {
                    mOutputStream = BufferedOutputStream(FileOutputStream(file))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onOutputBufferAvailable(codec: MediaCodec?, index: Int, info: MediaCodec.BufferInfo?) {
        var outputByteBuffer = mCodec?.getOutputBuffer(index)
        var outData = ByteArray(info?.size!!)
        outputByteBuffer?.get(outData)

        try {
            mOutputStream?.write(outData, 0, outData.size)
            if (isMuxStarted && info.size > 0 && info.presentationTimeUs > 0) {
                mMuxer?.writeSampleData(mVideoTrack, outputByteBuffer, info)
            }
            mCodec?.releaseOutputBuffer(index, false)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInputBufferAvailable(codec: MediaCodec?, index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var mVideoTrack = 0

    private var isMuxStarted = false

    override fun onOutputFormatChanged(codec: MediaCodec?, format: MediaFormat?) {
        mVideoTrack = mMuxer?.addTrack(codec?.getOutputFormat())!!
        mMuxer?.start()
        isMuxStarted = true
    }

    override fun onError(codec: MediaCodec?, e: MediaCodec.CodecException?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun startCode() {
        if (mCodec == null) {
            setUpController()
        }
        mCodec?.start()
    }

    fun stopCode() {
        mCodec?.stop()
        mCodec?.release()

        mMuxer?.stop()
        mMuxer?.release()
    }

    fun stopDistroy() {
        mCodec = null
        mMuxer = null
    }
}


