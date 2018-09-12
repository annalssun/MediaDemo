package com.cowinclub.dingdong.mediademo.openGLMedia.egl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.cowinclub.dingdong.mediademo.MyFilterEngine
import com.cowinclub.dingdong.mediademo.SMainActivity
import com.cowinclub.dingdong.mediademo.gles.EglCore
import com.cowinclub.dingdong.mediademo.gles.WindowSurface

class EGLTextureRender private constructor(threadName: String) : HandlerThread(threadName) {


    private lateinit var mContext: Context


//    private lateinit var mTextureView: Surface
    private lateinit var mSurface: Surface
    private var width = 0
    private var height = 0

//    private var mEGL: EGL10? = null
//    private var mEGLContext: EGLContext? = null
//    private var mEGLDisplay: EGLDisplay? = null
//    private var mEGLConfig: EGLConfig? = null
//    private var mEGLSurface: EGLSurface? = null

    //    private lateinit var mFilterEngine: EglFilterEngine
    lateinit var mSurfaceTexture: SurfaceTexture

    private var mTextures = IntArray(1)

    private var mFrameAvailable = false

    private var mIsRunning = false


    private val transformMatrix = FloatArray(16)
    private var mFilterEngine: MyFilterEngine? = null
    //    private var mDataBuffer: FloatBuffer? = null
//    private var mShaderProgram = -1
    private var aPositionLocation = -1
    private var aTextureCoordLocation = -1
    private var uTextureMatrixLocation = -1
    private var uTextureSamplerLocation = -1
    private val mFBOIds = IntArray(1)

    private lateinit var handler: Handler


    private lateinit var mWindowSurface: WindowSurface
    private lateinit var mInputWindowSurface: WindowSurface
    private lateinit var mEGLCore: EglCore

    constructor(context: Context, handler: Handler,
                surface: Surface, width: Int, height: Int) : this("EGLRenderThread") {
        this.mContext = context
        this.mSurface = surface
//        this.mTextureView = textureView
        this.width = width
        this.height = height
        this.handler = handler
        mIsRunning = true
        mEGLCore = EglCore()
    }

    private fun setUpEGL() {


        mWindowSurface = WindowSurface(mEGLCore, mSurface, false)
        mWindowSurface.makeCurrent()


//        mInputWindowSurface.makeCurrentReadFrom(mWindowSurface)

    }

//    private var mOESTextureId = IntArray(2)

    private fun initTexture() {
//        mOESTextureId = Utils.createOESTextureObject()
        mFilterEngine = MyFilterEngine(mContext)
//        mDataBuffer = mFilterEngine?.buffer
//        mShaderProgram = mFilterEngine?.shaderProgram!!
        GLES20.glGenFramebuffers(1, mFBOIds, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOIds[0])
        try {
            mSurfaceTexture = SurfaceTexture(mFilterEngine?.mOESTextureId!![0])
            mSurfaceTexture.setOnFrameAvailableListener {
                synchronized(this) {
                    mFrameAvailable = true
                    Log.v("ttt", "==================================tt")
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun distroyEGL() {
        mWindowSurface.release()
        mInputWindowSurface.release()
    }

    override fun run() {
        setUpEGL()
//        initFilter()
        initTexture()
        handler.post {
            (mContext as SMainActivity).startPreView()
        }
        while (mIsRunning) {
            if (draw()) {
                //EGL交换缓存区，实现双缓存交换并刷新显示缓存（由底层的FramebufferNativeWindow输出--FramebufferNativeWindo是ANativeWindow的继承类，其内部实现了queuebuffer dequeuebuffer等操作）
                //双缓冲刷新 front buffer 和 back buffer
                //eglSwapBuffers会去触发queuebuffer，dequeuebuffer，
                //queuebuffer将画好的buffer(back->front)交给surfaceflinger处理，
                //dequeuebuffer新创建一个buffer用来画图
                mWindowSurface.swapBuffers()
            }
        }

        distroyEGL()
    }

    fun onPause() {
        mIsRunning = false

    }


    private fun draw(): Boolean {
        synchronized(this) {
            if (mFrameAvailable) {
                mSurfaceTexture.updateTexImage() // 更新SurfaceTexture纹理图像信息，然后绑定的GLES11Ext.GL_TEXTURE_EXTERNAL_OES纹理才能渲染
                mSurfaceTexture.getTransformMatrix(transformMatrix) // 获取SurfaceTexture纹理变换矩
                mFrameAvailable = false
            } else {
                return false
            }
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)  //设置清除颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //GL_COLOR_BUFFER_BIT 设置窗口颜色
        //GL_DEPTH_BUFFER_BIT 设置深度缓存--把所有像素的深度值设置为最大值(一般为远裁剪面)
        GLES20.glViewport(0, 0, width, height)
        mFilterEngine?.drawFrame(transformMatrix)
        return true
    }


//   private fun drawFrame() {
//
//        aPositionLocation = GLES20.glGetAttribLocation(mShaderProgram, FilterEngine.POSITION_ATTRIBUTE);
//        aTextureCoordLocation = GLES20.glGetAttribLocation(mShaderProgram, FilterEngine.TEXTURE_COORD_ATTRIBUTE);
//        uTextureMatrixLocation = GLES20.glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MATRIX_UNIFORM);
//        uTextureSamplerLocation = GLES20.glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_SAMPLER_UNIFORM);
//
//        GLES20.glActiveTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId[0]);
//        GLES20.glUniform1i(uTextureSamplerLocation, 0);
//        GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);
//
//        if (mDataBuffer != null) {
//            mDataBuffer?.position(0);
//            GLES20.glEnableVertexAttribArray(aPositionLocation);
//            GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 16, mDataBuffer);
//
//            mDataBuffer?.position(2);
//            GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
//            GLES20.glVertexAttribPointer(aTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 16, mDataBuffer);
//        }
//
//
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//
//    }

    private inline fun <reified T> Array(size: Int): Array<T?> {
        return arrayOfNulls<T>(size)
    }
}