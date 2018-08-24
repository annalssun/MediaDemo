package com.cowinclub.dingdong.mediademo.openGlES

import android.graphics.Bitmap
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL10

open class Mesh {

    //顶点数组
    private var verticesBuffer: FloatBuffer? = null

    //顶点顺序
    private var indicesBuffer: ShortBuffer? = null

    //顶点颜色
    private var colorsBuffer: FloatBuffer? = null

    private var textureBuffer: FloatBuffer? = null

    //顺序缓存的数量
    private var numOfIndices = -1

    //纹理ID
    private var textureId = -1

    //图片
    private var bitmap: Bitmap? = null

    //应该加载
    private var mShouldLoadTexture = false

    //纯色
    private var argb = floatArrayOf(1f, 1f, 1f, 1f)

    var x = 0f
    var y = 0f
    var z = 0f

    var rx = 0f
    var ry = 0f
    var rz = 0f

    fun loadBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        mShouldLoadTexture = true
    }

    fun draw(gl: GL10?) {
        try {
            // Counter-clockwise winding.
            gl?.glFrontFace(GL10.GL_CCW);
            // Enable face culling.
            gl?.glEnable(GL10.GL_CULL_FACE);
            // What faces to remove with the face culling.
            gl?.glCullFace(GL10.GL_BACK);

            // Enabled the vertices buffer for writing and to be used during
            // rendering.
            gl?.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            // Specifies the location and data format of an array of vertex
            // coordinates to use when rendering.
            gl?.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer)
            // Enable the color array buffer to be used during rendering.
            gl?.glColor4f(argb[0], argb[1], argb[2], argb[3])
            if (colorsBuffer != null) {
                gl?.glEnableClientState(GL10.GL_COLOR_ARRAY)
                gl?.glColorPointer(4, GL10.GL_FLOAT, 0, colorsBuffer)
            }

            if (mShouldLoadTexture) {
                loadGLTexture(gl)
                mShouldLoadTexture = false
            }
            if (mTextTureId != -1 && textureBuffer != null) {
                gl?.glEnable(GL10.GL_TEXTURE_2D)
                // Enable the texture state
                gl?.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)

                // Point to our buffers
                gl?.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
                gl?.glBindTexture(GL10.GL_TEXTURE_2D, mTextTureId);
            }

            gl?.glTranslatef(x, y, z)
            gl?.glRotatef(rx, 1f, 0f, 0f)
            gl?.glRotatef(ry, 0f, 1f, 0f)
            gl?.glRotatef(rz, 0f, 0f, 1f)

            gl?.glDrawElements(GL10.GL_TRIANGLES, numOfIndices,
                    GL10.GL_UNSIGNED_SHORT, indicesBuffer)

            if (textureId != -1 && textureBuffer != null) {
                gl?.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
            }
            // Disable the vertices buffer.
            gl?.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            // Disable face culling.
            gl?.glDisable(GL10.GL_CULL_FACE);
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVertices(vertices: FloatArray) {
        val vbb = ByteBuffer.allocateDirect(vertices.size * 4)
        vbb.order(ByteOrder.nativeOrder())
        verticesBuffer = vbb.asFloatBuffer()
        verticesBuffer?.put(vertices)
        verticesBuffer?.position(0)
    }

    fun setIndices(indices: ShortArray) {
        val ibb = ByteBuffer.allocateDirect(indices.size * 2)
        ibb.order(ByteOrder.nativeOrder())
        indicesBuffer = ibb.asShortBuffer()
        indicesBuffer?.put(indices)
        indicesBuffer?.position(0)
        numOfIndices = indices.size
    }

    fun setColors(colors: FloatArray) {
        val cbb = ByteBuffer.allocateDirect(colors.size * 4)
        cbb.order(ByteOrder.nativeOrder())
        colorsBuffer = cbb.asFloatBuffer()
        colorsBuffer?.put(colors)
        colorsBuffer?.position(0)
    }

    fun setTexTure(textures: FloatArray) {
        val tbb = ByteBuffer.allocateDirect(textures.size * 4)
        tbb.order(ByteOrder.nativeOrder())
        textureBuffer = tbb.asFloatBuffer()
        textureBuffer?.put(textures)
        textureBuffer?.position(0)
    }

    fun setRGB(colors: FloatArray) {
        argb[0] = colors[0]
        argb[1] = colors[1]
        argb[2] = colors[2]
        argb[3] = colors[3]
    }

    private var mTextTureId: Int = -1

    /**
     * Loads the texture.
     *
     * @param gl
     */
    private fun loadGLTexture(gl: GL10?) { // New function
        // Generate one texture pointer...
        val textures = IntArray(1)
        gl?.glGenTextures(1, textures, 0)
        mTextTureId = textures[0]

        // ...and bind it to our array
        gl?.glBindTexture(GL10.GL_TEXTURE_2D, mTextTureId)

        // Create Nearest Filtered Texture
        gl?.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR.toFloat())
        gl?.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR.toFloat())

        // Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
        gl?.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE.toFloat())
        gl?.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_REPEAT.toFloat())

        // Use the Android GLUtils to specify a two-dimensional texture image
        // from our bitmap
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
    }

}