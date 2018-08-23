package com.cowinclub.dingdong.mediademo.openGlES

import android.opengl.GLSurfaceView
import android.opengl.GLU
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyRender : GLSurfaceView.Renderer {

    private var angle = 0

    private var square = Square()


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gl?.glViewport(0, 0, width, height)
        gl?.glMatrixMode(GL10.GL_PROJECTION)
        gl?.glLoadIdentity()
        GLU.gluPerspective(gl, 45.0f, width.toFloat() / height.toFloat(),
                0.1f, 100.0f)
        gl?.glMatrixMode(GL10.GL_MODELVIEW)
        gl?.glLoadIdentity()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //设置背景色
        gl?.glClearColor(0.0f, 0.0f, 0.0f, 0.5f)

        gl?.glShadeModel(GL10.GL_SMOOTH)
        //深度
        gl?.glClearDepthf(1.0f)
        gl?.glEnable(GL10.GL_DEPTH_TEST)
        gl?.glDepthFunc(GL10.GL_LEQUAL)
        gl?.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST)
    }

    override fun onDrawFrame(gl: GL10?) {
        //清空屏幕和深度缓存
        gl?.glClear(GL10.GL_COLOR_BUFFER_BIT or
                GL10.GL_DEPTH_BUFFER_BIT)
        gl?.glLoadIdentity()
        gl?.glTranslatex(0, 0, -10)

        //四边形
        //保留当前矩阵
        gl?.glPushMatrix()
        //逆时针旋转
        gl?.glRotatef(angle.toFloat(), 0.toFloat(), 0f, 1f)
        square.draw(gl)
        gl?.glPopMatrix()

        //四边形
        //保留当前矩阵
        gl?.glPushMatrix()
        gl?.glRotatef(-angle.toFloat(), 0f, 0f, 1f)
        gl?.glTranslatef(2f, 0f, 0f)
        gl?.glScalef(0.5f, 0.5f, 0.5f)
        square.draw(gl)

        // 四边形 C
        //保留当前矩阵
        gl?.glPushMatrix();
        // 使其绕着B旋转
        gl?.glRotatef(-angle.toFloat(), 0f, 0f, 1f);
        gl?.glTranslatef(2f, 0f, 0f);
        // 缩小到B的50%
        gl?.glScalef(.5f, .5f, .5f);
        // 自转
        gl?.glRotatef((angle * 10).toFloat(), 0f, 0f, 1f);
        // 绘制C
        square.draw(gl)

        gl?.glPopMatrix()
        gl?.glPopMatrix()

        angle++

    }
}