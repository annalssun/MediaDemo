package com.cowinclub.dingdong.mediademo.openGLMedia

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLSurfaceView
import android.opengl.GLU
import com.cowinclub.dingdong.mediademo.openGLMedia.opengl.Plane
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraRender : GLSurfaceView.Renderer {

    private lateinit var context: Context
    private lateinit var mCameraController: OpenGLCamera2Controller
    private var angle = 0
    private var isPreviewState = false
    private var mSurfaceTexture: SurfaceTexture? = null

    private lateinit var mCameraGLSurfaceView: CameraGLSurfaceView

    private var plane = Plane(2f,2f,1,1)


    constructor(context: Context, cameraController: OpenGLCamera2Controller, cameraGLSurfaceView: CameraGLSurfaceView)
            : this(context, cameraController, cameraGLSurfaceView, false)

    constructor(context: Context, cameraController: OpenGLCamera2Controller, cameraGLSurfaceView: CameraGLSurfaceView, isPreviewState: Boolean) {
        this.context = context
        this.mCameraController = cameraController
        this.isPreviewState = isPreviewState
        this.mCameraGLSurfaceView = cameraGLSurfaceView
        mOESTextureId = Utils.createOESTextureObject(context)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Sets the current view port to the new size.
        gl?.glViewport(0, 0, width, height)// OpenGL docs.
        // Select the projection matrix
        gl?.glMatrixMode(GL10.GL_PROJECTION)// OpenGL docs.
        // Reset the projection matrix
        gl?.glLoadIdentity()// OpenGL docs.
        // Calculate the aspect ratio of the window
        GLU.gluPerspective(gl, 45.0f,
                width.toFloat() / height.toFloat(),
                0.1f, 100.0f)
        // Select the modelview matrix
        gl?.glMatrixMode(GL10.GL_MODELVIEW)// OpenGL docs.
        // Reset the modelview matrix
        gl?.glLoadIdentity()// OpenGL docs.
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set the background color to black ( rgba ).
        gl?.glClearColor(1f, 0.0f, 0.0f, 0.5f);  // OpenGL docs.
        // Enable Smooth Shading, default not really needed.
        gl?.glShadeModel(GL10.GL_SMOOTH);// OpenGL docs.
        // Depth buffer setup.
        gl?.glClearDepthf(1.0f);// OpenGL docs.
        // Enables depth testing.
        gl?.glEnable(GL10.GL_DEPTH_TEST);// OpenGL docs.
        // The type of depth testing to do.
        gl?.glDepthFunc(GL10.GL_LEQUAL);// OpenGL docs.
        // Really nice perspective calculations.
        gl?.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, // OpenGL docs.
                GL10.GL_NICEST);
    }

    override fun onDrawFrame(gl: GL10?) {
        if (mSurfaceTexture != null) {
            mSurfaceTexture?.updateTexImage()
        }

        if (!isPreviewState) {
            isPreviewState = initSurfaceTexture()
        }

        gl?.glClearColor(1.0f, 0f, 0f, 0f)
        gl?.glActiveTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
        gl?.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId)

        plane.draw(gl)
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
        mCameraController.startPreview()
        return true
    }

}