package com.cowinclub.dingdong.mediademo.openGLMedia

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class CameraGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context,attrs) {

    private lateinit var render: CameraRender
    fun init(cameraController: OpenGLCamera2Controller) {
        try {
            setEGLContextClientVersion(2)
            render = CameraRender(context, cameraController, this)
            setRenderer(render)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}