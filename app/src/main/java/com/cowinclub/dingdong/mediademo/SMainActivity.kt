package com.cowinclub.dingdong.mediademo

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Button
import com.cowinclub.dingdong.mediademo.openGLMedia.CameraGLSurfaceView
import com.cowinclub.dingdong.mediademo.openGLMedia.OpenGLCamera2Controller
import com.cowinclub.dingdong.mediademo.openGLMedia.egl.EGLTextureRender
import com.cowinclub.dingdong.mediademo.openGLMedia.egl.EncodeRecordController

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class SMainActivity : AppCompatActivity(){

    private lateinit var surfaceView: CameraGLSurfaceView

    private lateinit var render: EGLTextureRender
    private lateinit var controller: EncodeRecordController

    private lateinit var cameraController: OpenGLCamera2Controller
    private var handler: Handler = Handler(Looper.getMainLooper())

    private lateinit var dm: DisplayMetrics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surface)
        surfaceView = findViewById(R.id.surfaceView)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        controller = EncodeRecordController(dm.widthPixels, dm.heightPixels)

        cameraController = OpenGLCamera2Controller(this)
        cameraController.setUpCameraOutputs(dm.widthPixels, dm.heightPixels)
        render = EGLTextureRender(MainActivity@ this, handler, controller.mEncoderSurface, dm.widthPixels, dm.heightPixels)

        surfaceView.init(cameraController,render)

        findViewById<Button>(R.id.end_btn).setOnClickListener {
            controller.stopCode()
        }

    }

    fun startPreView() {
        cameraController.startPreview(surfaceView.holder.surface, render.mSurfaceTexture)
    }

    override fun onResume() {
        super.onResume()
        cameraController.openCamera()
    }

//    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
//    }
//
//    override fun surfaceDestroyed(holder: SurfaceHolder?) {
//    }
//
//    override fun surfaceCreated(holder: SurfaceHolder?) {
//        render.start()
//    }
}