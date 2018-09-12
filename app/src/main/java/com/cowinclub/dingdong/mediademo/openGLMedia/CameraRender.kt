package com.cowinclub.dingdong.mediademo.openGLMedia

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.cowinclub.dingdong.mediademo.MyFilterEngine
import com.cowinclub.dingdong.mediademo.openGLMedia.egl.EGLTextureRender
import com.cowinclub.dingdong.mediademo.openGLMedia.opengl.Plane
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class CameraRender : GLSurfaceView.Renderer {

    private lateinit var context: Context
    private lateinit var mCameraController: OpenGLCamera2Controller
    private var angle = 0
    private var isPreviewState = false
    private var mSurfaceTexture: SurfaceTexture? = null
    private val transformMatrix = FloatArray(16)
    private var mFilterEngine: MyFilterEngine? = null
    //    private var mDataBuffer: FloatBuffer? = null
//    private var mShaderProgram = -1
    private var aPositionLocation = -1
    private var aTextureCoordLocation = -1
    private var uTextureMatrixLocation = -1
    private var uTextureSamplerLocation = -1
    private val mFBOIds = IntArray(1)

    private lateinit var mCameraGLSurfaceView: CameraGLSurfaceView

    private var plane = Plane(2f, 2f, 1, 1)

    private lateinit var mEGLRender: EGLTextureRender

    constructor(context: Context, cameraController: OpenGLCamera2Controller, cameraGLSurfaceView: CameraGLSurfaceView, render: EGLTextureRender)
            : this(context, cameraController, cameraGLSurfaceView, render, false)

    constructor(context: Context, cameraController: OpenGLCamera2Controller, cameraGLSurfaceView: CameraGLSurfaceView, render: EGLTextureRender, isPreviewState: Boolean) {
        this.context = context
        this.mCameraController = cameraController
        this.isPreviewState = isPreviewState
        this.mCameraGLSurfaceView = cameraGLSurfaceView
        this.mEGLRender = render

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gl?.glViewport(0, 0, width, height)// OpenGL docs.
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mFilterEngine = MyFilterEngine(context)
        mOESTextureId = mFilterEngine?.mOESTextureId!![0]
        glGenFramebuffers(1, mFBOIds, 0)
        glBindFramebuffer(GL_FRAMEBUFFER, mFBOIds[0])
    }

    override fun onDrawFrame(gl: GL10?) {
        if (mSurfaceTexture != null) {
            mSurfaceTexture?.updateTexImage()
            mSurfaceTexture?.getTransformMatrix(transformMatrix);
        }

        if (!isPreviewState) {
            isPreviewState = initSurfaceTexture()
        }
        mFilterEngine?.drawFrame(transformMatrix)

//        aPositionLocation = glGetAttribLocation(mShaderProgram, FilterEngine.POSITION_ATTRIBUTE);
//        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, FilterEngine.TEXTURE_COORD_ATTRIBUTE);
//        uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MATRIX_UNIFORM);
//        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_SAMPLER_UNIFORM);
//
//        glActiveTexture(GL_TEXTURE_EXTERNAL_OES);
//        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
//        glUniform1i(uTextureSamplerLocation, 0);
//        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);
//
//        if (mDataBuffer != null) {
//            mDataBuffer?.position(0);
//            glEnableVertexAttribArray(aPositionLocation);
//            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, mDataBuffer);
//
//            mDataBuffer?.position(2);
//            glEnableVertexAttribArray(aTextureCoordLocation);
//            glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, mDataBuffer);
//        }
//
//
//        glDrawArrays(GL_TRIANGLES, 0, 6);
//        glBindFramebuffer(GL_FRAMEBUFFER, 0);

    }

    private var mOESTextureId = 0

    private fun initSurfaceTexture(): Boolean {
        mSurfaceTexture = SurfaceTexture(mOESTextureId)
        mSurfaceTexture?.setOnFrameAvailableListener(object : SurfaceTexture.OnFrameAvailableListener {
            override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
                mCameraGLSurfaceView.requestRender()
            }
        })

        mCameraController.setSurfaceTexture(mSurfaceTexture!!)
        mEGLRender.start()
        return true
    }

}