package com.cowinclub.dingdong.mediademo

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.*
import com.cowinclub.dingdong.mediademo.openGLMedia.FilterEngine
import com.cowinclub.dingdong.mediademo.openGLMedia.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class MyFilterEngine {

    private lateinit var mContext: Context
    private lateinit var mBuffer: FloatBuffer
    var mOESTextureId = IntArray(2)
    private var vertexShader = -1
    private var fragmentShader = -1

    private var mShaderProgram = -1

    private var aPositionLocation = -1
    private var aTextureCoordLocation = -1
    private var uTextureMatrixLocation = -1
    private var uTextureSamplerLocation = -1

    constructor(context: Context) {
        filterEngine(context)
    }

    fun filterEngine(context: Context) {
        mContext = context
        mOESTextureId = Utils.createOESTextureObject()
        mBuffer = createBuffer(vertexData)
        vertexShader = loadShader(GL_VERTEX_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_vertex_shader))
        fragmentShader = loadShader(GL_FRAGMENT_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_fragment_shader))
        mShaderProgram = linkProgram(vertexShader, fragmentShader)
    }

    /*public static FilterEngine getInstance() {
        if (filterEngine == null) {
            synchronized (FilterEngine.class) {
                if (filterEngine == null)
                    filterEngine = new FilterEngine();
            }
        }
        return filterEngine;
    }*/

    private val vertexData = floatArrayOf(1f, 1f, 1f, 1f, -1f, 1f, 0f, 1f, -1f, -1f, 0f, 0f, 1f, 1f, 1f, 1f, -1f, -1f, 0f, 0f, 1f, -1f, 1f, 0f)


    private fun createBuffer(vertexData: FloatArray): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(vertexData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        buffer.put(vertexData, 0, vertexData.size).position(0)
        return buffer
    }

    private fun loadShader(type: Int, shaderSource: String): Int {
        val shader = glCreateShader(type)
        if (shader == 0) {
            throw RuntimeException("Create Shader Failed!" + glGetError())
        }
        glShaderSource(shader, shaderSource)
        glCompileShader(shader)
        return shader
    }

    private fun linkProgram(verShader: Int, fragShader: Int): Int {
        val program = glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Create Program Failed!" + glGetError())
        }
        glAttachShader(program, verShader)
        glAttachShader(program, fragShader)
        glLinkProgram(program)

        glUseProgram(program)
        return program
    }

    fun drawFrame(transformMatrix: FloatArray) {

        aPositionLocation = GLES20.glGetAttribLocation(mShaderProgram, FilterEngine.POSITION_ATTRIBUTE);
        aTextureCoordLocation = GLES20.glGetAttribLocation(mShaderProgram, FilterEngine.TEXTURE_COORD_ATTRIBUTE);
        uTextureMatrixLocation = GLES20.glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = GLES20.glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_SAMPLER_UNIFORM);

        GLES20.glActiveTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId[0]);
        GLES20.glUniform1i(uTextureSamplerLocation, 0);
        GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        if (true) {
            mBuffer.position(0);
            GLES20.glEnableVertexAttribArray(aPositionLocation);
            GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 16, mBuffer);

            mBuffer.position(2);
            GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
            GLES20.glVertexAttribPointer(aTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 16, mBuffer);
        }


        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

    }

    private inline fun <reified T> Array(size: Int): Array<T?> {
        return arrayOfNulls<T>(size)
    }


    companion object {
        val POSITION_ATTRIBUTE = "aPosition"
        val TEXTURE_COORD_ATTRIBUTE = "aTextureCoordinate"
        val TEXTURE_MATRIX_UNIFORM = "uTextureMatrix"
        val TEXTURE_SAMPLER_UNIFORM = "uTextureSampler"
    }
}