package com.cowinclub.dingdong.mediademo.openGLMedia

import android.content.Context
import android.opengl.GLSurfaceView

class CameraGLSurfaceView(context: Context, cameraController: OpenGLCamera2Controller) : GLSurfaceView(context) {

    init {
        setEGLContextClientVersion(2)
        var render = CameraRender(context, cameraController)
        setRenderer(render)
    }
}