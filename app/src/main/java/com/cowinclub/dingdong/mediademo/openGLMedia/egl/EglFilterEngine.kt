package com.cowinclub.dingdong.mediademo.openGLMedia.egl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.*
import com.cowinclub.dingdong.mediademo.R
import com.cowinclub.dingdong.mediademo.openGLMedia.FilterEngine
import com.cowinclub.dingdong.mediademo.openGLMedia.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class EglFilterEngine {


    private lateinit var mContext: Context
    //    private var mBuffer: FloatBuffer? = null
    private var mOESTextureId = -1
    private var vertexShader = -1
    private var fragmentShader = -1

    private var mShaderProgram = -1

    private var aPositionLocation = -1
    private var aTextureCoordLocation = -1
    private var uTextureMatrixLocation = -1
    private var uTextureSamplerLocation = -1
//    private var textureHandle = -1

    private var mVerticesData = floatArrayOf(
            -1.0f, 1.0f,  // top left
            -1.0f, -1.0f,  // bottom left
            1.0f, -1.0f,  // bottom right
            1.0f, 1.0f   // top right
    )

    private var mDrawOrderData = shortArrayOf(
            0, 1, 2, 0, 2, 3
    )

    private var mTextureData = floatArrayOf(
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f
    )

    lateinit var videoTextureTransform: FloatArray

    private var mVerticeBuffer: FloatBuffer? = null
    private var mDrawOrderBuffer: ShortBuffer? = null
    private var mTextureBuffer: FloatBuffer? = null

    constructor(OESTextureId: Int, context: Context) {
        mContext = context
        mOESTextureId = OESTextureId
        createBuffer()
        videoTextureTransform = FloatArray(16)
        vertexShader = loadShader(GL_VERTEX_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_vertex_shader))
        fragmentShader = loadShader(GL_FRAGMENT_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_fragment_shader))
        mShaderProgram = linkProgram(vertexShader, fragmentShader)
    }

//    private val vertexData = floatArrayOf(
//            1f, 1f, 1f, 1f,
//            -1f, 1f, 0f, 1f,
//            -1f, -1f, 0f, 0f,
//            1f, 1f, 1f, 1f,
//            -1f, -1f, 0f, 0f,
//            1f, -1f, 1f, 0f)


    fun createBuffer() {

        mVerticeBuffer = ByteBuffer.allocateDirect(mVerticesData.size * 4)
                .run {
                    order(ByteOrder.nativeOrder())
                    asFloatBuffer()
                }.apply {
                    put(mVerticesData, 0, mVerticesData.size)
                    position(0)
                }

        mDrawOrderBuffer = ByteBuffer.allocateDirect(mDrawOrderData.size * 2)
                .run {
                    order(ByteOrder.nativeOrder())
                    asShortBuffer()
                }.apply {
                    put(mDrawOrderData, 0, mDrawOrderData.size)
                    position(0)
                }

        mTextureBuffer = ByteBuffer.allocateDirect(mTextureData.size * 4)
                .run {
                    order(ByteOrder.nativeOrder())
                    asFloatBuffer()
                }.apply {
                    put(mVerticesData)
                    position(0)
                }

    }

    fun loadShader(type: Int, shaderSource: String): Int {
        val shader = glCreateShader(type)
        if (shader == 0) {
            throw RuntimeException("Create Shader Failed!" + glGetError())
        }
        glShaderSource(shader, shaderSource)
        glCompileShader(shader)
        return shader
    }

    fun linkProgram(verShader: Int, fragShader: Int): Int {
        val program = glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Create Program Failed!")
        }
        glAttachShader(program, verShader)
        glAttachShader(program, fragShader)
        glLinkProgram(program)

        glUseProgram(program)
        return program
    }

    private lateinit var textures: IntArray

    fun setupTextture(textures: IntArray): SurfaceTexture {
        this.textures = textures
        return SurfaceTexture(textures[0])
    }

    fun drawTexture() {
        aPositionLocation = glGetAttribLocation(mShaderProgram, FilterEngine.POSITION_ATTRIBUTE)
        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, FilterEngine.TEXTURE_COORD_ATTRIBUTE)
        uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MATRIX_UNIFORM)
        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_SAMPLER_UNIFORM)
//        textureHandle = GLES20.glGetUniformLocation(mShaderProgram, "texture0"); // 获得贴图对应的纹理采样器句柄（索引）

//        glActiveTexture(GLES20.GL_TEXTURE0)
//        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId)
//        glUniform1i(uTextureSamplerLocation, 0)
//        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0)

        if (mVerticeBuffer != null) {
            glEnableVertexAttribArray(aPositionLocation)
            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 0, mVerticeBuffer)
        }

        glActiveTexture(GLES20.GL_TEXTURE0)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textures[0])
        glUniform1i(uTextureSamplerLocation,0)


//        glActiveTexture(GLES20.GL_TEXTURE1)
//        glBindTexture(GLES20.GL_TEXTURE_2D,textures[0])
//        glUniform1f(textureHandle,1.toFloat())

        glEnableVertexAttribArray(aTextureCoordLocation)
        glVertexAttribPointer(aTextureCoordLocation,4,GLES20.GL_FLOAT,false,0,mTextureBuffer)

        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, videoTextureTransform, 0);
        glDrawElements(GLES20.GL_TRIANGLE_STRIP,mDrawOrderData.size,GLES20.GL_UNSIGNED_SHORT,mDrawOrderBuffer)

//        GLES20.glDisableVertexAttribArray(textureHandle)
//        GLES20.glDisableVertexAttribArray(aTextureCoordLocation)
    }

    fun getShaderProgram(): Int {
        return mShaderProgram
    }

//    fun getBuffer(): FloatBuffer? {
//        return mBuffer
//    }

    fun getOESTextureId(): Int {
        return mOESTextureId
    }

    fun setOESTextureId(OESTextureId: Int) {
        mOESTextureId = OESTextureId
    }

    companion object {
        val POSITION_ATTRIBUTE = "aPosition"
        val TEXTURE_COORD_ATTRIBUTE = "aTextureCoordinate"
        val TEXTURE_MATRIX_UNIFORM = "uTextureMatrix"
        val TEXTURE_SAMPLER_UNIFORM = "uTextureSampler"
    }
}