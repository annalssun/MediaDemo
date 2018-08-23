package com.cowinclub.dingdong.mediademo.openGlES

import android.opengl.GLSurfaceView
import android.opengl.GLU
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES10.glLoadIdentity
import android.opengl.GLES10.glMatrixMode
import android.R.attr.angle
import android.opengl.GLES10.glPopMatrix
import android.opengl.GLES10.glRotatef
import android.opengl.GLES10.glScalef
import android.opengl.GLES10.glTranslatef
import android.opengl.GLES10.glPushMatrix
import android.opengl.GLES10.glLoadIdentity




class MyRender : GLSurfaceView.Renderer {

    private var angle = 0

    private var square = Square()


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
        // Clears the screen and depth buffer.
        gl?.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        // Replace the current matrix with the identity matrix
        gl?.glLoadIdentity()
        // Translates 10 units into the screen.
        gl?.glTranslatef(0f, 0f, -10f)

        // SQUARE A
        // Save the current matrix.
        gl?.glPushMatrix()
        // Rotate square A counter-clockwise.
        gl?.glRotatef(angle.toFloat(), 0f, 0f, 1f)
        // Draw square A.
        square.draw(gl)
        // Restore the last matrix.
        gl?.glPopMatrix()

        // SQUARE B
        // Save the current matrix
        gl?.glPushMatrix()
        // Rotate square B before moving it, making it rotate around A.
        gl?.glRotatef(-angle.toFloat(), 0f, 0f, 1f)
        // Move square B.
        gl?.glTranslatef(2f, 0f, 0f)
        // Scale it to 50% of square A
        gl?.glScalef(.5f, .5f, .5f)
        // Draw square B.
        square.draw(gl)

        // SQUARE C
        // Save the current matrix
        gl?.glPushMatrix()
        // Make the rotation around B
        gl?.glRotatef(-angle.toFloat(), 0f, 0f, 1f)
        gl?.glTranslatef(2f, 0f, 0f)
        // Scale it to 50% of square B
        gl?.glScalef(.5f, .5f, .5f)
        // Rotate around it's own center.
        gl?.glRotatef((angle * 10).toFloat(), 0f, 0f, 1f)
        // Draw square C.
        square.draw(gl)

        // Restore to the matrix as it was before C.
        gl?.glPopMatrix()
        // Restore to the matrix as it was before B.
        gl?.glPopMatrix()

        // Increse the angle.
        angle++
    }
}