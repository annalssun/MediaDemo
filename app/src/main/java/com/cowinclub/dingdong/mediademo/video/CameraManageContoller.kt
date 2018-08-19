package com.cowinclub.dingdong.mediademo.video

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CameraManageContoller{
    private lateinit var mCameraManager: CameraManager
    constructor(context: Context){
        mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
}