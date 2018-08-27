package com.cowinclub.dingdong.mediademo.video

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import com.cowinclub.dingdong.mediademo.Came2Capture.AutoFitTextureView
import com.cowinclub.dingdong.mediademo.Came2Capture.Camera2Utils
import com.cowinclub.dingdong.mediademo.Came2Capture.CompareSizesByArea
import com.cowinclub.dingdong.mediademo.Came2Capture.ImageSaver
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore

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
     * Whether the current camera device supports Flash or not.
     */
    private var mSupportFlash = false

    /**
     * Orientation of the camera sensor
     */
    private var mSensorOrientation = 0

    /**
     * The [android.util.Size] of camera preview.
     */
    private lateinit var previewSize: Size
    private var state = STATE_PREVIEW
    private var mImageReader: ImageReader? = null
    private lateinit var file: File
    private var mCameraDevice: CameraDevice? = null

    private var MAX_PREVIEW_WIDTH = 1080
    private var MAX_PREVIEW_HEIGHT = 1920

    /**
     * This a callback object for the [ImageReader]. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        backgroundHandler?.post(ImageSaver(it.acquireNextImage(), file))
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
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice?) {
            mCameraOpenSemaphore.release()
            camera?.close()
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice?, error: Int) {
        }
    }

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            process(result!!)
        }

        override fun onCaptureProgressed(session: CameraCaptureSession?, request: CaptureRequest?, partialResult: CaptureResult?) {
            process(partialResult!!)
        }

        private fun process(result: CaptureResult) {
            when (state) {
                STATE_PREVIEW -> Unit
                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        state = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                    || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                // CONTROL_AE_STATE can be null on some devices
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    state = STATE_PICTURE_TAKEN
                    captureStillPicture()
                } else {
                    runPrecaptureSequence()
                }
            }
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
//            if (mCameraOpenSemaphore.tryAcquire(5000, TimeUnit.SECONDS))
//                throw RuntimeException("Time out waiting to lock camera opening.")
            mCameraManager.openCamera(mCameraID, mCameraDeviceStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

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
                val largest = Collections.max(Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
                        CompareSizesByArea())
                mImageReader = ImageReader.newInstance(largest.width, largest.height,
                        ImageFormat.JPEG, 2).apply {
                    setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
                }
                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                val displayRotation = (context as Activity).windowManager.defaultDisplay.rotation
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                val swappedDimension = areDimensionsSwapped(displayRotation)

                val displaySize = Point()
                (context as Activity).windowManager.defaultDisplay.getSize(displaySize)
                val rotatePreviewWidth = if (swappedDimension) height else width
                val rotatePreviewHeight = if (swappedDimension) width else height
                var maxPreviewWidth = if (swappedDimension) displaySize.y else displaySize.x
                var maxPreviewHeight = if (swappedDimension) displaySize.x else displaySize.y

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT

                previewSize = Camera2Utils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
                        rotatePreviewWidth, rotatePreviewHeight,
                        maxPreviewWidth, maxPreviewHeight,
                        largest)
                // We fit the aspect ratio of TextureView to the size of preview we picked.
                if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.width, previewSize.height)

                } else {
                    textureView.setAspectRatio(previewSize.height, previewSize.width)
                }

                mSupportFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                this.mCameraID = cameraID
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun areDimensionsSwapped(displayRotation: Int): Boolean {
        var swappedDimension = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (mSensorOrientation == 90 || mSensorOrientation == 270)
                    swappedDimension = true
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (mSensorOrientation == 0 || mSensorOrientation == 180)
                    swappedDimension = true
            }
            else -> {

            }
        }
        return swappedDimension
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
    private var mPreviewRequest: CaptureRequest? = null

    private fun createCameraPreviewSession() {
        try {
            val texture = textureView.surfaceTexture
            texture.setDefaultBufferSize(previewSize.width, previewSize.height)

            val surface = Surface(texture)
            mPreViewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreViewRequestBuilder?.addTarget(surface)

            mCameraDevice?.createCaptureSession(Arrays.asList(surface, mImageReader?.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigureFailed(session: CameraCaptureSession?) {
                        }

                        override fun onConfigured(session: CameraCaptureSession?) {
                            if (mCameraDevice == null) return
                            mCameraCaptureSession = session
                            try {
                                mPreViewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                                if (mSupportFlash) {
                                    mPreViewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE,
                                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                                }
                                mPreviewRequest = mPreViewRequestBuilder?.build()
                                mCameraCaptureSession?.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, backgroundHandler)
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }
                    }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
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

    private fun captureStillPicture() {
        try {
            if (mCameraDevice == null) return
            val rotation = (context as Activity).windowManager.defaultDisplay.rotation
            val captureBuilder =
                    mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                            .apply {
                                this!!.addTarget(mImageReader?.surface)
                                set(CaptureRequest.JPEG_ORIENTATION,
                                        (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360)
                                set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            }?.also {
                                if (mSupportFlash) {
                                    it.set(CaptureRequest.CONTROL_AE_MODE,
                                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                                }
                            }
            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
                    unlockFocus()
                }
            }

            mCameraCaptureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder?.build(), captureCallback, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in [.captureCallback] from [.lockFocus].
     */
    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreViewRequestBuilder?.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            // Tell #captureCallback to wait for the precapture sequence to be set.
            state = STATE_WAITING_PRECAPTURE
            mCameraCaptureSession?.capture(mPreViewRequestBuilder?.build(), mCaptureCallback,
                    backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }


    /**
     * Lock the focus as the first step for a still image capture.
     */
    fun lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreViewRequestBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START)
            // Tell #captureCallback to wait for the lock.
            state = STATE_WAITING_LOCK
            mCameraCaptureSession?.capture(mPreViewRequestBuilder?.build(), mCaptureCallback,
                    backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreViewRequestBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            if (mSupportFlash) {
                mPreViewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            }
            mCameraCaptureSession?.capture(mPreViewRequestBuilder?.build(), mCaptureCallback,
                    backgroundHandler)
            // After this, the camera will go back to the normal state of preview.
            state = STATE_PREVIEW
            mCameraCaptureSession?.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    fun closeCamera() {
        try {
            mCameraOpenSemaphore.acquire()
            mCameraDevice?.close()
            mCameraCaptureSession?.close()
            mImageReader?.close()
            mImageReader = null
            mCameraCaptureSession = null
            mCameraDevice = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }


    }


    companion object {

        /**
         * Conversion from screen rotation to JPEG orientation.
         */
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        /**
         * Tag for the [Log].
         */
        private val TAG = "Camera2BasicFragment"

        /**
         * Camera state: Showing camera preview.
         */
        private val STATE_PREVIEW = 0

        /**
         * Camera state: Waiting for the focus to be locked.
         */
        private val STATE_WAITING_LOCK = 1

        /**
         * Camera state: Waiting for the exposure to be precapture state.
         */
        private val STATE_WAITING_PRECAPTURE = 2

        /**
         * Camera state: Waiting for the exposure state to be something other than precapture.
         */
        private val STATE_WAITING_NON_PRECAPTURE = 3

        /**
         * Camera state: Picture was taken.
         */
        private val STATE_PICTURE_TAKEN = 4
    }
}


