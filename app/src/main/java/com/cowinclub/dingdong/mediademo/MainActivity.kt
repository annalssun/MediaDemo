package com.cowinclub.dingdong.mediademo

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.cowinclub.dingdong.mediademo.Came2Capture.CaptureCamera2Controller
import com.cowinclub.dingdong.mediademo.Media.WindEar
import com.cowinclub.dingdong.mediademo.openGlES.MyRender

class MainActivity : AppCompatActivity() {

    private lateinit var mWindEar: WindEar
    private lateinit var camera2Controller: CaptureCamera2Controller
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = GLSurfaceView(this)
        view.setRenderer(MyRender())
        setContentView(view)
//        var autoFitTextureView = findViewById<AutoFitTextureView>(R.id.textureView)
//         camera2Controller = CaptureCamera2Controller(this, autoFitTextureView)

    }

//    override fun onResume() {
//        super.onResume()
//        camera2Controller.startCamera()
//
//    }

    private fun recordingRadio() {
        //        WindEar.init()
//        mWindEar = WindEar.instance
//        findViewById<Button>(R.id.recotd_btn).setOnClickListener {
//            mWindEar.startRecord()
//            Log.i("sun","===========================开始录制")
//        }
//
//        findViewById<Button>(R.id.stop_recotd_btn).setOnClickListener {
//            mWindEar.stopRecord()
//            Log.i("sun","===========================停止录制")
//        }
//
//        findViewById<Button>(R.id.play_pcm_btn).setOnClickListener {
//            mWindEar.startPlayPcm()
//        }
//
//        findViewById<Button>(R.id.play_wave_btn).setOnClickListener {
//            mWindEar.startPlayWave()
//        }
    }

}
