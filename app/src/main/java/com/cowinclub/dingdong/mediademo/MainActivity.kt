package com.cowinclub.dingdong.mediademo

import android.annotation.TargetApi
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.TextureView
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import com.cowinclub.dingdong.mediademo.Came2Capture.CaptureCamera2Controller
import com.cowinclub.dingdong.mediademo.Media.WindEar
import com.cowinclub.dingdong.mediademo.openGLMedia.OpenGLCamera2Controller
import com.cowinclub.dingdong.mediademo.openGLMedia.egl.EGLTextureRender
import com.cowinclub.dingdong.mediademo.openGLMedia.egl.EncodeRecordController
import com.cowinclub.dingdong.mediademo.video.VideoRecordController

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {


    private lateinit var mWindEar: WindEar
    private lateinit var camera2Controller: CaptureCamera2Controller
    private lateinit var videoRecordController: VideoRecordController
    private lateinit var cameraController: OpenGLCamera2Controller
    private var handler: Handler = Handler(Looper.getMainLooper())

    private lateinit var textureView: TextureView
    private lateinit var render: EGLTextureRender
    private lateinit var controller: EncodeRecordController


    private lateinit var dm: DisplayMetrics
    private lateinit var showViewRl: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        controller = EncodeRecordController(dm.widthPixels, dm.heightPixels)
        setContentView(R.layout.activity_main)

        textureView = findViewById<TextureView>(R.id.texture)
        textureView.surfaceTextureListener = this

        cameraController = OpenGLCamera2Controller(this)
        cameraController.setUpCameraOutputs(dm.widthPixels, dm.heightPixels)

        findViewById<Button>(R.id.start_btn).setOnClickListener{
            controller.startCode()
        }
        findViewById<Button>(R.id.end_btn).setOnClickListener{
            controller.stopCode()
        }

    }

    fun startPreView() {
//        cameraController.startPreview(render.mSurfaceTexture)
    }


    private fun testP() {
//        var dm = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(dm)
//
//        var surface = findViewById<CameraGLSurfaceView>(R.id.texture)
//        surface.init(cameraController)


    }

    override fun onResume() {
        super.onResume()
        cameraController.openCamera()
    }

    private fun recordingRadio() {
        WindEar.init()
        mWindEar = WindEar.instance
        findViewById<Button>(R.id.recotd_btn).setOnClickListener {
            mWindEar.startRecord()
            Log.i("sun", "===========================开始录制")
        }

        findViewById<Button>(R.id.stop_recotd_btn).setOnClickListener {
            mWindEar.stopRecord()
            Log.i("sun", "===========================停止录制")
        }

        findViewById<Button>(R.id.play_pcm_btn).setOnClickListener {
            mWindEar.startPlayPcm()
        }

        findViewById<Button>(R.id.play_wave_btn).setOnClickListener {
            mWindEar.startPlayWave()
        }
    }


    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }


    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
//        render = EGLTextureRender(MainActivity@ this, handler, textureView, dm.widthPixels, dm.heightPixels)
//        render.start()
//
//        controller.setInputSurface(Surface(surface))
    }

}
