package com.xihh.base.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.DisplayMetrics

/**
 * Created by Administrator on 2016/12/8.
 */
object CameraUtil {
    fun getScreenWH(context: Context): DisplayMetrics {
        var dMetrics = DisplayMetrics()
        dMetrics = context.resources.displayMetrics
        return dMetrics
    }

    /**
     * 计算焦点及测光区域
     *
     * @param focusWidth
     * @param focusHeight
     * @param areaMultiple
     * @param x
     * @param y
     * @param previewleft
     * @param previewRight
     * @param previewTop
     * @param previewBottom
     * @return Rect(left, top, right, bottom) : left、top、right、bottom是以显示区域中心为原点的坐标
     */
    fun calculateTapArea(
        focusWidth: Int, focusHeight: Int,
        areaMultiple: Float, x: Float, y: Float, previewleft: Int,
        previewRight: Int, previewTop: Int, previewBottom: Int
    ): Rect {
        val areaWidth = (focusWidth * areaMultiple).toInt()
        val areaHeight = (focusHeight * areaMultiple).toInt()
        val centerX = (previewleft + previewRight) / 2
        val centerY = (previewTop + previewBottom) / 2
        val unitx = (previewRight.toDouble() - previewleft.toDouble()) / 2000
        val unity = (previewBottom.toDouble() - previewTop.toDouble()) / 2000
        val left = clamp(
            ((x - areaWidth / 2 - centerX) / unitx).toInt(),
            -1000, 1000
        )
        val top = clamp(
            ((y - areaHeight / 2 - centerY) / unity).toInt(),
            -1000, 1000
        )
        val right = clamp((left + areaWidth / unitx).toInt(), -1000, 1000)
        val bottom = clamp((top + areaHeight / unity).toInt(), -1000, 1000)
        return Rect(left, top, right, bottom)
    }

    fun clamp(x: Int, min: Int, max: Int): Int {
        if (x > max) return max
        return if (x < min) min else x
    }

    /**
     * 检测摄像头设备是否可用
     * Check if this device has a camera
     *
     * @param context
     * @return
     */
    fun checkCameraHardware(context: Context?): Boolean {
        return context != null && context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    /**
     * bitmap旋转
     *
     * @param b
     * @param degrees
     * @return
     */
    fun rotate(b: Bitmap?, degrees: Int): Bitmap? {
        var b = b
        if (degrees != 0 && b != null) {
            val m = Matrix()
            m.setRotate(degrees.toFloat(), b.width.toFloat() / 2, b.height.toFloat() / 2)
            try {
                val b2 = Bitmap.createBitmap(
                    b, 0, 0, b.width, b.height, m, true
                )
                if (b != b2) {
                    b.recycle() //Android开发网再次提示Bitmap操作完应该显示的释放
                    b = b2
                }
            } catch (ex: OutOfMemoryError) {
                // Android123建议大家如何出现了内存不足异常，最好return 原始的bitmap对象。.
            }
        }
        return b
    }

    fun getHeightInPx(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    fun getWidthInPx(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }
}