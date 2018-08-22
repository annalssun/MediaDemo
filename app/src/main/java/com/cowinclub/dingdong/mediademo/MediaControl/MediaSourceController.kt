package com.cowinclub.dingdong.mediademo.MediaControl

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AppendTrack
import java.io.RandomAccessFile
import java.nio.ByteBuffer

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
object MediaSourceController {

    @JvmStatic
    fun extractVideo(sourceVideoPath: String, outVideoPath: String) {
        var mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(sourceVideoPath)
        val numTracks = mediaExtractor.trackCount
        var videoTrackIndex = -1
        for (i in 0 until numTracks) {
            var format = mediaExtractor.getTrackFormat(i)
            var mime = format.getString(MediaFormat.KEY_MIME)
            if (mime.startsWith("video/")) {
                mediaExtractor.selectTrack(i)
                videoTrackIndex = i
                break
            }
        }

        val mediaMuxe = MediaMuxer(outVideoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        mediaMuxe.addTrack(mediaExtractor.getTrackFormat(videoTrackIndex))
        mediaMuxe.start()

        val inputBuffer = ByteBuffer.allocate(1024 * 1024 * 2)  //分配足够内存
        val info = MediaCodec.BufferInfo()

        var isFinsh = false

        while (true) {
            val sampleSize = mediaExtractor.readSampleData(inputBuffer, 0)
            if (sampleSize < 0) break

            val presentationTimeUs = mediaExtractor.sampleTime
            info.offset = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                info.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME
            } else {
                info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
            }
            info.size = sampleSize
            info.presentationTimeUs = presentationTimeUs
            mediaMuxe.writeSampleData(videoTrackIndex, inputBuffer, info)
            mediaExtractor.advance()
        }

        mediaMuxe.stop()
        mediaMuxe.release()
        mediaExtractor.release()
    }

    @JvmStatic
    fun extractAudioFromMP4(outAudioPath: String, sourceMP4Path: String) {
        var movie = MovieCreator.build(sourceMP4Path)
        var audioTracksList = arrayListOf<Track>()
        for (t in movie.tracks) {
            if (t.handler == "soun")
                audioTracksList.add(t)
        }

//        var audioTracks = Array<Track>(audioTracksList.size)
//        for (i in 0 until audioTracksList.size){
//            audioTracks[0] = audioTracksList[i]
//        }

        val result = Movie()
        if (audioTracksList.size > 0) {
            result.addTrack(AppendTrack(*audioTracksList.toArray(Array<Track>(audioTracksList.size))))
        }

        var container = DefaultMp4Builder().build(result)
        var fc = RandomAccessFile(outAudioPath, "rw").channel
        container.writeContainer(fc)
        fc.close()

    }

    @JvmStatic
    fun replaceAudioFromMP4File(outputFilePath: String, videoProvidePath: String,
                                audioProvidePath: String) {
        val mediaMuxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        //视频MediaExtractor
        val mVideoMediaExtractor = MediaExtractor()
        mVideoMediaExtractor.setDataSource(videoProvidePath)
        var videoTrackIndex = -1
        for (i in 0 until mVideoMediaExtractor.trackCount) {
            val format = mVideoMediaExtractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                mVideoMediaExtractor.selectTrack(i)
                videoTrackIndex = mediaMuxer.addTrack(format)
            }
        }

        //音频MediaExtractor
        val mAudioMediaExtractor = MediaExtractor()
        mAudioMediaExtractor.setDataSource(audioProvidePath)
        var audioTrackIndex = -1
        for (i in 0 until mAudioMediaExtractor.trackCount) {
            val format = mAudioMediaExtractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                mAudioMediaExtractor.selectTrack(i)
                videoTrackIndex = mediaMuxer.addTrack(format)
            }
        }

        mediaMuxer.start()
        var videoEndPreTimeUs: Long = 0
        if (-1 != videoTrackIndex) {//视频封装
            val videoMediaCodecInfo = MediaCodec.BufferInfo()
            videoMediaCodecInfo.presentationTimeUs = 0
            var buffer = ByteBuffer.allocate(1024 * 1024)
            while (true) {
                val sampleSize = mVideoMediaExtractor.readSampleData(buffer, 0)
                if (sampleSize < 0) {
                    break
                }
                videoMediaCodecInfo.offset = 0
                videoMediaCodecInfo.size = sampleSize
                videoMediaCodecInfo.presentationTimeUs = mVideoMediaExtractor.sampleTime
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    videoMediaCodecInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME
                } else {
                    videoMediaCodecInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
                }
                videoEndPreTimeUs = videoMediaCodecInfo.presentationTimeUs
                mediaMuxer.writeSampleData(videoTrackIndex, buffer, videoMediaCodecInfo)
                mVideoMediaExtractor.advance()
            }
        }

        if (-1 != audioTrackIndex) {
            var info = MediaCodec.BufferInfo()
            info.presentationTimeUs = 0
            val buffer = ByteBuffer.allocate(1024 * 1024)
            var finish = false
            while (!finish && mAudioMediaExtractor.sampleTime <= videoEndPreTimeUs) {
                val sampleSize = mAudioMediaExtractor.readSampleData(buffer, 0)
                if (sampleSize < 0) {
                    finish = true
                }
                info.offset = 0;
                info.size = sampleSize;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    info.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME
                } else {
                    info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
                }
                info.presentationTimeUs = mAudioMediaExtractor.getSampleTime()
                mediaMuxer.writeSampleData(audioTrackIndex, buffer, info)
                mAudioMediaExtractor.advance()
            }
        }

        mAudioMediaExtractor.release()
        mVideoMediaExtractor.release()
        mediaMuxer.stop()
        mediaMuxer.release()

    }

    inline fun <reified T> Array(size: Int): Array<T?> {
        return arrayOfNulls<T>(size)
    }


}

