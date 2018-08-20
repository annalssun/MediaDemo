package com.cowinclub.dingdong.mediademo.video

import android.annotation.TargetApi
import android.os.Build
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import java.util.*

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
object Camera2Constance{
    val ORIENTATIONS = SparseIntArray()
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

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private val MAX_PREVIEW_WIDTH = 1920

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private val MAX_PREVIEW_HEIGHT = 1080

    /**
     * Given `choices` of `Size`s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as
     * the respective max size, and whose aspect ratio matches with the specified value. If such
     * size doesn't exist, choose the largest one that is at most as large as the respective max
     * size, and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended
     *                          output class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal `Size`, or an arbitrary one if none were big enough
     */
    @JvmStatic private fun chooseOptimalSize(
            choices: Array<Size>,
            textureViewWidth: Int,
            textureViewHeight: Int,
            maxWidth: Int,
            maxHeight: Int,
            aspectRatio: Size
    ): Size {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough = ArrayList<Size>()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough = ArrayList<Size>()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight &&
                    option.height == option.width * h / w) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size > 0) {
            return Collections.min(bigEnough, CompareSizesByArea())
        } else if (notBigEnough.size > 0) {
            return Collections.max(notBigEnough, CompareSizesByArea())
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size")
            return choices[0]
        }
    }



}