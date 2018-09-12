package com.cowinclub.dingdong.mediademo.openGLMedia

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.cowinclub.dingdong.mediademo.openGLMedia.egl.EGLTextureRender

class CameraGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context,attrs) {

    private lateinit var render: CameraRender
    fun init(cameraController: OpenGLCamera2Controller,eglRender:EGLTextureRender) {
        try {
            setEGLContextClientVersion(2)
            render = CameraRender(context, cameraController, this,eglRender)
            setRenderer(render)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}