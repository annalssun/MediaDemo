package com.cowinclub.dingdong.mediademo.openGLMedia

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class CameraSurfaceView(context: Context, attributeSet: AttributeSet?, style: Int) :
        SurfaceView(context, attributeSet, style), SurfaceHolder.Callback {
    private lateinit var mSurfaceHolder:SurfaceHolder
    constructor(context: Context) : this(context, null) {

    }

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {

    }

    private fun initView(){
        mSurfaceHolder = holder

    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}