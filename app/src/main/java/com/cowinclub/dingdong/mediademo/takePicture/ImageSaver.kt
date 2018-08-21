package com.cowinclub.dingdong.mediademo.takePicture

import android.annotation.TargetApi
import android.media.Image
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@TargetApi(Build.VERSION_CODES.KITKAT)
internal class ImageSaver(private val image:Image,
                          private val file:File):Runnable{

    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var output:FileOutputStream? = null
        try {
            output = FileOutputStream(file).apply {
                write(bytes)
            }
        }catch (e:IOException){
            e.printStackTrace()
        }finally {
            image.close()
            output?.let {
                try {
                    it.close()
                }catch (e:IOException){
                    e.printStackTrace()
                }
            }
        }
    }

}