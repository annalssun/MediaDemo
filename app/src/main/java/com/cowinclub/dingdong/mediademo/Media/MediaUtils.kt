package com.cowinclub.dingdong.mediademo.Media

import java.io.FileOutputStream

object MediaUtils {
    /**
     * @param out            wav音频文件流
     * @param totalAudioLen  不包括header的音频数据总长度
     * @param longSampleRate 采样率,也就是录制时使用的频率
     * @param channels       audioRecord的频道数量
     *
     */
    @JvmStatic
    fun writeWaveFileHeader(out: FileOutputStream, totalAudioLen: Long,
                            longSampleRate: Int, channels: Int) {
        val header = genetateWaveFileHeader(totalAudioLen, longSampleRate, channels)
        out.write(header, 0, header.size)
    }

    /**
     * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，
     * wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
     * FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的
     *
     * @param pcmAudioByteCount 不包括header的音频数据总长度
     * @param longSampleRate    采样率,也就是录制时使用的频率
     * @param channels          audioRecord的频道数量
     */
    @JvmStatic
    fun genetateWaveFileHeader(pcmAudioByteCount: Long,
                               longSampleRate: Int, channels: Int): ByteArray {


        var totalDataLen = pcmAudioByteCount + 32  //包含前8字节的Wav文件总长度
        var byteRate = longSampleRate * 2 * channels
        val header = ByteArray(44)

        //RIFF
        header[0] = 'R'.toByte()
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()

        //数据长度
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shl 8) and 0xff).toByte()
        header[6] = ((totalDataLen shl 16) and 0xff).toByte()
        header[7] = ((totalDataLen shl 24) and 0xff).toByte()

        //wave
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()

        //fmt
        header[12] = 'f'.toByte()
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()

        //数据大小
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0

        //编码方式，10H为PCM编码
        header[20] = 1
        header[21] = 0

        //通道数
        header[22] = channels.toByte()
        header[23] = 0

        //采样率，每个通道的播放速度
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()

        //音频传输速率，采样率*通道数*采样深度/8
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()

        //确定系统一次处理多少字节的数量，确定缓冲区，通道数*采样位数
        header[32] = (2 * channels).toByte()
        header[33] = 0

        //样本的数据位数
        header[34] = 16
        header[35] = 0

        //data chunk
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()

        header[40] = (pcmAudioByteCount and 0xff).toByte()
        header[41] = ((pcmAudioByteCount shr 8) and 0xff).toByte()
        header[42] = ((pcmAudioByteCount shr 16) and 0xff).toByte()
        header[43] = ((pcmAudioByteCount shr 24) and 0xff).toByte()

        return header
    }

}