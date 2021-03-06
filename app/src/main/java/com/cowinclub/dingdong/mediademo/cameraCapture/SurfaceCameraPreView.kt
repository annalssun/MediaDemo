package com.cowinclub.dingdong.mediademo.cameraCapture

import android.content.Context
import android.content.res.Configuration
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView

class SurfaceCameraPreView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    companion object {
        private var mOptVideoHeight = 1920
        private var mOptVideoWidth = 1080
    }

    private var mSurfaceHolder: SurfaceHolder = holder.apply {
        addCallback(this@SurfaceCameraPreView)
    }
    private var mCamera: Camera? = null
    private var mFrameCount = 0

    private val mCameraPreviewCallback = object : Camera.PreviewCallback {
        override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
            mFrameCount++
        }

    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        if (mSurfaceHolder.surface == null) {
            return
        }

        try {
            mCamera?.stopPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var parameters = mCamera?.parameters

        if (this.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            mCamera?.setDisplayOrientation(90)
        } else {
            mCamera?.setDisplayOrientation(0)
        }

        mCamera.apply {
            try {
                this?.setPreviewDisplay(mSurfaceHolder)
                this?.setPreviewCallback(mCameraPreviewCallback)
                this?.startPreview()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

//        getCameraOptimalVideoSize()
    }

    private fun getCameraOptimalVideoSize() {
        try {
            val parameters: Camera.Parameters? = mCamera?.parameters
            val supportPreViewSizes = parameters?.supportedPreviewSizes
            val supportVideoSizes = parameters?.supportedVideoSizes
            val optimalSize = CameraHelper.getOptimalVideoSize(supportPreViewSizes, supportVideoSizes as List<Camera.Size>,
                    width, height)
            mOptVideoHeight = optimalSize?.height ?: 1920
            mOptVideoWidth = optimalSize?.width ?: 1080

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        try {
            mSurfaceHolder.removeCallback(this)
            mCamera?.setPreviewCallback(null)
            mCamera?.stopPreview()
            mCamera?.release()
            mCamera = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mCamera = CameraManageController.instance.openCamera()
        mCamera.apply {
            try {
                this?.setPreviewDisplay(mSurfaceHolder)
                this?.startPreview()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
//        try {
//            getCameraOptimalVideoSize()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }


    fun setCamera(camera: Camera) {
        this.mCamera = camera
    }

}