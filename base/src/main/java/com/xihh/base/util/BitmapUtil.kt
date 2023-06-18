package com.xihh.base.util

import android.graphics.*

/**
 * 位图的处理方法
 * 注意：关于位图的操作都算是耗时操作，所以不能在主线程执行
 **/
object BitmapUtil {
    /**
     * 灰度化处理
     */
    fun Bitmap.copyGray(): Bitmap {
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        val result = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        canvas.drawBitmap(this, 0f, 0f, paint)
        return result
    }

    /**
     * bitmap旋转
     */
    fun rotate(bitmap: Bitmap, degrees: Int): Bitmap? {
        var result = bitmap
        if (degrees != 0) {
            val m = Matrix()
            m.setRotate(degrees.toFloat(), result.width.toFloat() / 2, result.height.toFloat() / 2)
            try {
                val b2 = Bitmap.createBitmap(result, 0, 0, result.width, result.height, m, true)
                if (result != b2) {
                    result.recycle() //Android开发网再次提示Bitmap操作完应该显示的释放
                    result = b2
                }
            } catch (ex: OutOfMemoryError) {
                // Android123建议大家如何出现了内存不足异常，最好return 原始的bitmap对象。.
                return null
            }
        }
        return result
    }

    /**
     *  先灰度化再二值化是图片识别非常重要的一步，能够方便的提取图片特征。

        原理是，将图片的灰度设定一个阈值，高于这个阈值的点变为黑色，低于这个阈值的点变为白色，这样能很方便的判断图片特征。

        通用的灰度公式是：gray=r*0.3+g*0.59+b*0.11

        获取颜色的argb方式：

        alpha=color >>> 24

        red=(color >> 16) & 0xFF

        green=(color >> 8) & 0xFF

        blue=color & 0xFF

        生成颜色的方式：color=(alpha << 24) | (red << 16) | (green << 8) | blue

        获取图片的color信息：

        Bitmap对象.getPixels(@ColorInt int[] pixels, int offset, int stride,int x, int y, int width, int height)

        pixels  接收位图颜色的数组
        offset  第一个写入像素的索引[]
        stride  行之间要跳过的项目数（以像素[]为单位）（必须大于等于位图的宽度）。可能是负面的。
        x       从位图中读取的第一个像素的x坐标
        y       从位图中读取的第一个像素的y坐标
        width    从每行读取的像素数
        height   要读取的行数

        写入图片的color信息：

        Bitmap对象.setPixels(@ColorInt int[] pixels, int offset, int stride,nt x, int y, int width, int height) 方法：用数组中的颜色替换位图中的像素。

        pixels   要写入位图的颜色
        offset   从像素[]读取的第一种颜色的索引
        stride   要在行之间跳过的颜色数（以像素为单位）。通常，该值与位图的宽度相同，但可以更大（或负值）。
        x        位图中要写入的第一个像素的x坐标。
        y        位图中要写入的第一个像素的y坐标。
        width    每行从像素[]复制的颜色数
        height   要写入位图的行数
     **/
    fun Bitmap.copySingleThreshold(threshold: Int): Bitmap {
        val width = width
        val height = height
        var color: Int
        var r: Int
        var g: Int
        var b: Int
        var a: Int
        val oldPx = IntArray(width * height)
        val newPx = IntArray(width * height)
        getPixels(oldPx, 0, width, 0, 0, width, height)
        for (j in 0 until width * height) {
            color = oldPx[j]
            r = Color.red(color)
            g = Color.green(color)
            b = Color.blue(color)
            a = Color.alpha(color)
            var gray = (r.toFloat() * 0.3 + g.toFloat() * 0.59 + b.toFloat() * 0.11).toInt()
            gray = if (gray < threshold) {
                0
            } else {
                255
            }
            newPx[j] = Color.argb(a, gray, gray, gray)
        }
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bmp.setPixels(newPx, 0, width, 0, 0, width, height)
        return bmp
    }
}