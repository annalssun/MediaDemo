package com.cowinclub.dingdong.mediademo.openGLMedia

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
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
import com.cowinclub.dingdong.mediademo.Came2Capture.CompareSizesByArea
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class OpenGLCamera2Controller(private var context: Context) {
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


    private val mCameraDeviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice?) {
            mCameraOpenSemaphore.release()
            mCameraDevice = camera

//            startPreview()

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


    fun openCamera() {
        startBackGroundThread()
        val permission = ContextCompat.checkSelfPermission((context as Activity), Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return
        }

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
    private var mWidth = 0
    private var mHeight = 0
    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    fun setUpCameraOutputs(width: Int, height: Int) {

        this.mWidth = width
        this.mHeight = height
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

                this.mCameraID = cameraID
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }


    private var mPreViewRequestBuilder: CaptureRequest.Builder? = null
    private var mCameraCaptureSession: CameraCaptureSession? = null

    private lateinit var mSurfaceTexture: SurfaceTexture
    fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
        this.mSurfaceTexture = surfaceTexture
    }

    fun startPreview(surface: Surface,surfaceTexture: SurfaceTexture) {
        try {

            surfaceTexture.setDefaultBufferSize(mWidth, mHeight)
            val surface0 = Surface(surfaceTexture)

            mSurfaceTexture.setDefaultBufferSize(mWidth, mHeight)
            var surface1 = Surface(mSurfaceTexture)
            mPreViewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreViewRequestBuilder?.addTarget(surface0)
            mPreViewRequestBuilder?.addTarget(surface1)


            mCameraDevice?.createCaptureSession(Arrays.asList(surface0,surface1),
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

            mSurfaceTexture.apply {
                setDefaultBufferSize(previewSize.width, previewSize.height)
            }

            val previewSurface = Surface(mSurfaceTexture)
//            mediaRecorder?.setPreviewDisplay(previewSurface)

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
//        startPreview()
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


