package com.cowinclub.dingdong.mediademo.openGlES

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL10

class Square {


    private var vertices: FloatArray = floatArrayOf(
            -1.0f,  1.0f, 0.0f,  // 0, Top Left
            -1.0f, -1.0f, 0.0f,  // 1, Bottom Left
            1.0f, -1.0f, 0.0f,  // 2, Bottom Right
            1.0f,  1.0f, 0.0f   // 3, Top Right
    )
    //定点顺序
    private var indices = shortArrayOf(0, 1, 2, 0, 2, 3)

    //定点缓冲
    private lateinit var vertexBuffer: FloatBuffer

    //循序缓冲
    private lateinit var indexBuffer: ShortBuffer

    init {
        init()
    }

    private fun init() {
        // 一个float是4字节，所以我们申请缓冲的大小为数组长度乘以4
        val vbb = ByteBuffer.allocateDirect(vertices.size * 4)
        vbb.order(ByteOrder.nativeOrder())
        vertexBuffer = vbb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)
        // 一个short是两个字节，申请缓冲大小为数组长度乘以2
        val ibb = ByteBuffer.allocateDirect(indices.size * 2)
        ibb.order(ByteOrder.nativeOrder())
        indexBuffer = ibb.asShortBuffer()
        indexBuffer.put(indices)
        indexBuffer.position(0)
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
            gl?.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            // Specifies the location and data format of an array of vertex
            // coordinates to use when rendering.
            gl?.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

            gl?.glDrawElements(GL10.GL_TRIANGLES, indices.size,
                    GL10.GL_UNSIGNED_SHORT, indexBuffer);

            // Disable the vertices buffer.
            gl?.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            // Disable face culling.
            gl?.glDisable(GL10.GL_CULL_FACE);
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}