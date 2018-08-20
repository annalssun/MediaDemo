package com.cowinclub.dingdong.mediademo.video

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.*
import android.hardware.camera2.CameraDevice.StateCallback
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.SurfaceHolder
import java.util.concurrent.Semaphore

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CameraManageContoller {
    private lateinit var mCameraManager: CameraManager
    private lateinit var mCameraId: String
    private var mFlashSupported = false
    private var mCameraDevice: CameraDevice? = null
    private var mSurfaceHolder: SurfaceHolder
    private lateinit var mPreviewRequestBuilder: CaptureRequest.Builder
    private var mCameraCaptureSession: CameraCaptureSession? = null
    private var mBackGroundThread: HandlerThread? = null
    private var mBackGroundHandler: Handler? = null
    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private val cameraOpenCloseLock = Semaphore(1)

    private var mPreviewRequest: CaptureRequest? = null

    constructor(context: Context, surfaceHolder: SurfaceHolder) {
        this.mSurfaceHolder = surfaceHolder
        mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    @SuppressLint("MissingPermission")
    fun openRearCamera() {
        try {
            //获取摄像头列表
            for (cameraId in mCameraManager.cameraIdList) {
                //获取设备信息
                var characteristics = mCameraManager.getCameraCharacteristics(cameraId)
                //获取后置摄像头信息
                var facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }
                //获取支持图片信息
                var map: StreamConfigurationMap? = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        ?: continue
                mFlashSupported = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                mCameraId = cameraId
                break
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        mCameraManager.openCamera(mCameraId, mStateCallback, null)
    }

    //相机状态回调
    private var mStateCallback = object : StateCallback() {
        override fun onOpened(camera: CameraDevice?) {
            cameraOpenCloseLock.release()
            mCameraDevice = camera

            //创建CameraPreViewSession
            startPreView()
        }

        override fun onDisconnected(camera: CameraDevice?) {
            cameraOpenCloseLock.release()
            camera?.close()
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            cameraOpenCloseLock.release()
            camera?.close()
            mCameraDevice = null
        }
    }

    /*启动预览模式*/
    private fun startPreView() {
        if (mCameraDevice == null) return

        try {
            closePreviewSession()
            createCameraPreviewSession(CameraDevice.TEMPLATE_PREVIEW)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private fun createCameraPreviewSession(templeType: Int) {
        try {
            //设置了一个具有输出Surface的CaptureRequest.Builder。
            mPreviewRequestBuilder = mCameraDevice?.createCaptureRequest(templeType)!!
            mPreviewRequestBuilder.addTarget(mSurfaceHolder.surface)
            //创建一个CameraCaptureSession来进行相机预览。
            mCameraDevice?.createCaptureSession(listOf(mSurfaceHolder.surface)
                    , object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession?) {
                    Log.i("CameraManage", "打开相机失败")
                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    if (mCameraDevice == null) return

                    mCameraCaptureSession = session

                    try {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)

                        mPreviewRequest = mPreviewRequestBuilder.build()
                        HandlerThread("CameraPreview").start()
                        mCameraCaptureSession?.setRepeatingRequest(mPreviewRequest, null, mBackGroundHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }
            }, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun startBackGroundThread() {
        mBackGroundThread = HandlerThread("CameraBackThread")
        mBackGroundThread?.start()
        mBackGroundHandler = Handler(mBackGroundThread?.looper)
    }

    fun stopBackGroundThread() {
        mBackGroundThread?.quitSafely()
        try {
            mBackGroundThread?.join()
            mBackGroundThread = null
            mBackGroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun closePreviewSession() {
        mCameraCaptureSession?.close()
        mCameraCaptureSession = null
    }

    fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            closePreviewSession()
            mCameraDevice?.close()
            mCameraDevice = null

        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }
}