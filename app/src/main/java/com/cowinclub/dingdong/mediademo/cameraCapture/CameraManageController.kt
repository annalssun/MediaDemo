package com.cowinclub.dingdong.mediademo.cameraCapture

import android.hardware.Camera


class CameraManageController private constructor() {

    private var mCamera: Camera? = null

    companion object {
        val instance: CameraManageController by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CameraManageController()
        }
    }

    fun openCamera(): Camera? {
        if (mCamera != null) return mCamera
        try {
            mCamera = Camera.open()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mCamera
    }

    fun releaseCamera() {
        if (mCamera != null) {
            mCamera?.setPreviewCallback(null)
            mCamera?.reconnect()
            mCamera = null
        }
    }

    fun isCameraAvaliable(): Boolean {
        return mCamera != null
    }

}