package com.cowinclub.dingdong.mediademo.video

import android.content.Context
import com.cowinclub.dingdong.mediademo.Came2Capture.AutoFitTextureView

class VideoRecordController constructor(context: Context, textureView: AutoFitTextureView) {

    private lateinit var mCameraController: VideoCamera2Controller

    init {
        mCameraController = VideoCamera2Controller(context, textureView)
    }

    fun openCamera() {
        mCameraController.startCamera()
    }

    fun startRecording() {
        mCameraController.startRecording()
    }

    fun stopRecording() {
        mCameraController.stopRecording()
    }

}