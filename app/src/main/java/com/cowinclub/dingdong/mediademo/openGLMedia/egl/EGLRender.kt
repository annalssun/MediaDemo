package com.cowinclub.dingdong.mediademo.openGLMedia.egl

import android.annotation.TargetApi
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.Build
import android.os.HandlerThread
import android.view.Surface
import com.cowinclub.dingdong.mediademo.R
import com.cowinclub.dingdong.mediademo.openGLMedia.Utils
import javax.microedition.khronos.egl.*


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class EGLRender private constructor(threadName: String) : HandlerThread(threadName) {
    private lateinit var mContext: Context
    private lateinit var mSurface: Surface
    private var width = 0
    private var height = 0

    private var mEGL: EGL10? = null
    private var mEGLContext: EGLContext? = null
    private var mEGLDisplay: EGLDisplay? = null
    private var mEGLConfig: EGLConfig? = null
    private var mEGLSurface: EGLSurface? = null

    private lateinit var mFilterEngine: EglFilterEngine
    lateinit var mSurfaceTexture: SurfaceTexture

    private var mTextures = IntArray(1)

    private var mFrameAvailable = false

    private var mIsRunning = false

    constructor(context: Context, surface: Surface, width: Int, height: Int) : this("EGLRenderThread") {
        this.mContext = context
        this.mSurface = surface
        this.width = width
        this.height = height
        mIsRunning = true
        setUpEGL()
        initTexture()
    }

    fun setUpEGL() {
        mEGL = EGLContext.getEGL() as EGL10?

        mEGLDisplay = mEGL?.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

        var version = IntArray(2)
        mEGL?.eglInitialize(mEGLDisplay, version) // 初始化显示设备、获取EGL版本号

        mEGLConfig = selectEGLConfig()

        mEGLContext = createEGLContext(mEGL, mEGLDisplay, mEGLConfig)

        mEGLSurface = mEGL?.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, mSurface, null)

        try {
            if (mEGLSurface == null || mEGLSurface == EGL10.EGL_NO_SURFACE) {
                throw RuntimeException("EGL error EGLSurface null")
            }

            if (!mEGL?.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)!!) {
                throw RuntimeException("GL Make current Error")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun createEGLContext(egl: EGL10?, eglDisplay: EGLDisplay?, eglConfig: EGLConfig?): EGLContext? {
        val attrs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        )

        return egl?.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrs)
    }

    private fun selectEGLConfig(): EGLConfig? {
        val configsCount = IntArray(1)
        val configs = Array<EGLConfig>(1)
        val attributes = initAttributes()
        val confSize = 1
        if (!mEGL?.eglChooseConfig(mEGLDisplay, attributes, configs, confSize, configsCount)!!) {
            throw IllegalArgumentException("Failed to select config" + GLUtils.getEGLErrorString(mEGL?.eglGetError()!!))
        }

        if (configsCount[0] > 0)
            return configs[0]

        return null
    }


    private fun initAttributes(): IntArray {
        return intArrayOf(
                EGL10.EGL_RENDERABLE_TYPE,
                EGL14.EGL_OPENGL_ES2_BIT,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0, // EGL_DEPTH_SIZE 深度、模板尺寸
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE

        )
    }

    fun initTexture() {
        mTextures = Utils.createOESTextureObject()
        Utils.load2Dexture(mContext, R.drawable.text,mTextures[0])
        mFilterEngine = EglFilterEngine(mTextures[0], mContext)
        mSurfaceTexture = mFilterEngine.setupTextture(mTextures)
        mSurfaceTexture.setOnFrameAvailableListener {
            mFrameAvailable = true
        }
    }

    fun distroyEGL() {
        mEGL?.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, mEGLContext)
        mEGL?.eglDestroyContext(mEGLDisplay, mEGLContext)
        mEGL?.eglDestroySurface(mEGLDisplay, mEGLSurface)
        mEGL?.eglTerminate(mEGLDisplay)
    }

//    override fun start() {
//        super.start()
//
//    }

    override fun run() {
        super.run()
        while (mIsRunning) {
            if (draw()) {
                //EGL交换缓存区，实现双缓存交换并刷新显示缓存（由底层的FramebufferNativeWindow输出--FramebufferNativeWindo是ANativeWindow的继承类，其内部实现了queuebuffer dequeuebuffer等操作）
                //双缓冲刷新 front buffer 和 back buffer
                //eglSwapBuffers会去触发queuebuffer，dequeuebuffer，
                //queuebuffer将画好的buffer(back->front)交给surfaceflinger处理，
                //dequeuebuffer新创建一个buffer用来画图
                mEGL?.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            }
        }
    }

    fun onPause() {
        mIsRunning = false
    }


    protected fun draw(): Boolean {
        synchronized(this) {
            if (mFrameAvailable) {
                mSurfaceTexture.updateTexImage() // 更新SurfaceTexture纹理图像信息，然后绑定的GLES11Ext.GL_TEXTURE_EXTERNAL_OES纹理才能渲染
                mSurfaceTexture.getTransformMatrix(mFilterEngine.videoTextureTransform) // 获取SurfaceTexture纹理变换矩
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
        mFilterEngine.drawTexture()
        return true
    }

    private inline fun <reified T> Array(size: Int): Array<T?> {
        return arrayOfNulls<T>(size)
    }
}