package com.cowinclub.dingdong.mediademo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import com.cowinclub.dingdong.mediademo.Came2Capture.CaptureCamera2Controller
import com.cowinclub.dingdong.mediademo.Media.WindEar
import com.cowinclub.dingdong.mediademo.openGLMedia.CameraGLSurfaceView
import com.cowinclub.dingdong.mediademo.openGLMedia.OpenGLCamera2Controller
import com.cowinclub.dingdong.mediademo.video.VideoRecordController

class MainActivity : AppCompatActivity() {

    private lateinit var mWindEar: WindEar
    private lateinit var camera2Controller: CaptureCamera2Controller
    private lateinit var videoRecordController: VideoRecordController
    private lateinit var cameraController: OpenGLCamera2Controller
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        val view = GLSurfaceView(this)
//        view.setRenderer(MyRender(this))

//        setContentView(R.layout.activity_main)
//        var autoFitTextureView = findViewById<AutoFitTextureView>(R.id.textureView)
//        videoRecordController = VideoRecordController(this,autoFitTextureView)
//         camera2Controller = CaptureCamera2Controller(this, autoFitTextureView)
//        setContentView(R.layout.play_layout)
//        recordingRadio()

//        findViewById<Button>(R.id.start_btn).setOnClickListener{
//            videoRecordController.startRecording()
//        }
//
//        findViewById<Button>(R.id.end_btn).setOnClickListener{
//            videoRecordController.stopRecording()
//        }

        var dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        var cameraController = OpenGLCamera2Controller(this)
        cameraController.setUpCameraOutputs(dm.widthPixels, dm.heightPixels)
        var surface = CameraGLSurfaceView(this, cameraController)
        setContentView(surface)

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

}
