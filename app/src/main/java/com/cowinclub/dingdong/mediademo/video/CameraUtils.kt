package com.cowinclub.dingdong.mediademo.video

import android.hardware.Camera


object CameraUtils {

    @JvmStatic
    fun getCameraInstance(): Camera? {
        var camera: Camera? = null
        try {
            camera = Camera.open()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return camera
    }
}