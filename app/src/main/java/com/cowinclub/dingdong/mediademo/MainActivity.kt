package com.cowinclub.dingdong.mediademo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.cowinclub.dingdong.mediademo.Came2Capture.CaptureCamera2Controller
import com.cowinclub.dingdong.mediademo.Media.WindEar

class MainActivity : AppCompatActivity() {

    private lateinit var mWindEar: WindEar
    private lateinit var camera2Controller: CaptureCamera2Controller
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        val view = GLSurfaceView(this)
//        view.setRenderer(MyRender(this))
//        setContentView(view)
//        var autoFitTextureView = findViewById<AutoFitTextureView>(R.id.textureView)
//         camera2Controller = CaptureCamera2Controller(this, autoFitTextureView)
        setContentView(R.layout.play_layout)
        recordingRadio()

    }

//    override fun onResume() {
//        super.onResume()
//        camera2Controller.startCamera()
//
//    }

    private fun recordingRadio() {
                WindEar.init()
        mWindEar = WindEar.instance
        findViewById<Button>(R.id.recotd_btn).setOnClickListener {
            mWindEar.startRecord()
            Log.i("sun","===========================开始录制")
        }

        findViewById<Button>(R.id.stop_recotd_btn).setOnClickListener {
            mWindEar.stopRecord()
            Log.i("sun","===========================停止录制")
        }

        findViewById<Button>(R.id.play_pcm_btn).setOnClickListener {
            mWindEar.startPlayPcm()
        }

        findViewById<Button>(R.id.play_wave_btn).setOnClickListener {
            mWindEar.startPlayWave()
        }
    }

}
