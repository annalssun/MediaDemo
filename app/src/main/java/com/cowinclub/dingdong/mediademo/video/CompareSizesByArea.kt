package com.cowinclub.dingdong.mediademo.video

import android.annotation.TargetApi
import android.os.Build
import android.util.Size
import java.lang.Long.signum

/**
 * Compares two `Size`s based on their areas.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal class CompareSizesByArea : Comparator<Size> {

    // We cast here to ensure the multiplications won't overflow
    override fun compare(lhs: Size, rhs: Size) =
            signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

}