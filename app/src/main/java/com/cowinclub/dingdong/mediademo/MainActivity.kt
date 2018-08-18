package com.cowinclub.dingdong.mediademo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import com.cowinclub.dingdong.mediademo.Media.WindEar

class MainActivity : AppCompatActivity() {

    private lateinit var mWindEar: WindEar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.play_layout)
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
