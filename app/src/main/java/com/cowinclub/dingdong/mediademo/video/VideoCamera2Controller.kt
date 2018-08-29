package com.cowinclub.dingdong.mediademo.video

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.content.ContextCompat
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import com.cowinclub.dingdong.mediademo.Came2Capture.AutoFitTextureView
import com.cowinclub.dingdong.mediademo.Came2Capture.CompareSizesByArea
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class VideoCamera2Controller(private var context: Context,
                             private var textureView: AutoFitTextureView) {
    private var mCameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private lateinit var mCameraID: String

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private var backgroundThread: HandlerThread? = null

    /**
     * A [Handler] for running tasks in the background.
     */
    private var backgroundHandler: Handler? = null

    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private var mCameraOpenSemaphore = Semaphore(1)

    /**
     * Orientation of the camera sensor
     */
    private var mSensorOrientation = 0

    /**
     * The [android.util.Size] of camera preview.
     */
    private lateinit var previewSize: Size

    private var mCameraDevice: CameraDevice? = null

    private var mediaRecorder: MediaRecorder? = null

    private var filePath: String? = null

    private val SENSOR_ORIENTATION_DEFAULT_DEGREES = 90
    private val SENSOR_ORIENTATION_INVERSE_DEGREES = 270
    private val DEFAULT_ORIENTATIONS = SparseIntArray().apply {
        append(Surface.ROTATION_0, 90)
        append(Surface.ROTATION_90, 0)
        append(Surface.ROTATION_180, 270)
        append(Surface.ROTATION_270, 180)
    }
    private val INVERSE_ORIENTATIONS = SparseIntArray().apply {
        append(Surface.ROTATION_0, 270)
        append(Surface.ROTATION_90, 180)
        append(Surface.ROTATION_180, 90)
        append(Surface.ROTATION_270, 0)
    }


    private var textureViewlistener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            closeCamera()
            closeBackGroundThread()
            return true
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera(width, height)
        }
    }

    private val mCameraDeviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice?) {
            mCameraOpenSemaphore.release()
            mCameraDevice = camera

            startPreview()
            configureTransform(textureView.width, textureView.height)
        }

        override fun onDisconnected(camera: CameraDevice?) {
            mCameraOpenSemaphore.release()
            camera?.close()
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            mCameraOpenSemaphore.release()
            camera?.close()
            mCameraDevice = null
        }
    }


    private fun openCamera(width: Int, height: Int) {
        val permission = ContextCompat.checkSelfPermission((context as Activity), Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return
        }
        setUpCameraOutputs(width, height)
        configureTransform(width, height)

        try {
            mediaRecorder = MediaRecorder()
            mCameraManager.openCamera(mCameraID, mCameraDeviceStateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private lateinit var videoSize: Size

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        try {
            for (cameraID in mCameraManager.cameraIdList) {
                var characteristics = mCameraManager.getCameraCharacteristics(cameraID)
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        ?: continue
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder::class.java))
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
                        width, height, videoSize
                )
                // We fit the aspect ratio of TextureView to the size of preview we picked.
                if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.width, previewSize.height)

                } else {
                    textureView.setAspectRatio(previewSize.height, previewSize.width)
                }
                this.mCameraID = cameraID
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }


    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `textureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `textureView` is fixed.
     *
     * @param viewWidth  The width of `textureView`
     * @param viewHeight The height of `textureView`
     */

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val rotation = (context as Activity).windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.width.toFloat(), previewSize.height.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            val scale = Math.max(viewHeight.toFloat() / previewSize.height,
                    viewWidth.toFloat() / previewSize.width)
            with(matrix) {
                setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView.setTransform(matrix)
    }

    private var mPreViewRequestBuilder: CaptureRequest.Builder? = null
    private var mCameraCaptureSession: CameraCaptureSession? = null


    private fun startPreview() {
        try {
            val texture = textureView.surfaceTexture
            texture.setDefaultBufferSize(previewSize.width, previewSize.height)

            val surface = Surface(texture)
            mPreViewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreViewRequestBuilder?.addTarget(surface)

            mCameraDevice?.createCaptureSession(Arrays.asList(surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigureFailed(session: CameraCaptureSession?) {
                        }

                        override fun onConfigured(session: CameraCaptureSession?) {
                            if (mCameraDevice == null) return
                            mCameraCaptureSession = session
                            updatePreView()
                        }
                    }, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private fun updatePreView() {
        if (mCameraDevice == null) return

        try {
            setUpCaptureRequestBuilder(mPreViewRequestBuilder)
            HandlerThread("CameraPreview").start()
            mCameraCaptureSession?.setRepeatingRequest(mPreViewRequestBuilder?.build(),
                    null, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun setUpCaptureRequestBuilder(builder: CaptureRequest.Builder?) {
        builder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
    }


    private fun startBackGroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    fun closeBackGroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun startCamera() {
        startBackGroundThread()
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = textureViewlistener
        }
    }

    fun closeCamera() {
        try {
            mCameraOpenSemaphore.acquire()
            mCameraDevice?.close()
            mCameraCaptureSession?.close()
            mCameraCaptureSession = null
            mCameraDevice = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun closePreviewSession() {
        mCameraCaptureSession?.close()
        mCameraCaptureSession = null
    }

    private fun setupMediaRecorder() {

        if (filePath == null || filePath!!.isEmpty()) {
            filePath = getFilePath(context)
        }

        val rotation = (context as Activity).windowManager.defaultDisplay.rotation

        when (mSensorOrientation) {
            SENSOR_ORIENTATION_DEFAULT_DEGREES -> {
                mediaRecorder?.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation))
            }
            SENSOR_ORIENTATION_INVERSE_DEGREES -> {
                mediaRecorder?.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation))
            }
        }



        mediaRecorder?.apply {

            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(filePath)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(30)
            setVideoSize(videoSize.width, videoSize.height)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
        }
    }

    fun startRecording() {
        try {
            closePreviewSession()
            setupMediaRecorder()

            val texture = textureView.surfaceTexture.apply {
                setDefaultBufferSize(previewSize.width, previewSize.height)
            }

            val previewSurface = Surface(texture)
            val recordSurface = mediaRecorder!!.surface




            val sufaces = ArrayList<Surface>().apply {
                add(previewSurface)
                add(recordSurface)
            }

            mPreViewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
                    ?.apply {
                        addTarget(previewSurface)
                        addTarget(recordSurface)
                    }

            mCameraDevice?.createCaptureSession(sufaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession?) {

                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    mCameraCaptureSession = session
                    updatePreView()
                }
            }, backgroundHandler)

            mediaRecorder?.start()

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun getFilePath(context: Context): String {
        val filename = "${System.currentTimeMillis()}.mp4"
        val dir = context.getExternalFilesDir(null)

        return if (dir == null) {
            filename
        } else {
            "${dir.absolutePath}/$filename"
        }


    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            reset()
        }
        filePath = null
        startPreview()
    }


    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private fun chooseVideoSize(choices: Array<Size>) = choices.firstOrNull {
        it.width == it.height * 4 / 3 && it.width <= 1080
    } ?: choices[choices.size - 1]

    /**
     * Given [choices] of [Size]s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal [Size], or an arbitrary one if none were big enough
     */
    private fun chooseOptimalSize(
            choices: Array<Size>,
            width: Int,
            height: Int,
            aspectRatio: Size
    ): Size {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val w = aspectRatio.width
        val h = aspectRatio.height
        val bigEnough = choices.filter {
            it.height == it.width * h / w && it.width >= width && it.height >= height
        }

        // Pick the smallest of those, assuming we found any
        return if (bigEnough.isNotEmpty()) {
            Collections.min(bigEnough, CompareSizesByArea())
        } else {
            choices[0]
        }
    }

}


