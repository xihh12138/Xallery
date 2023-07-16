package com.xallery.picture.ui

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GestureDetectorCompat
import com.xihh.base.util.logx

class PictureView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                translationX = (e2.x - e1.x) * 0.8f
                translationY = (e2.y - e1.y) * 0.8f
                logx { "PictureView: onScroll translationX=$translationX translationY=$translationY" }

                return true
            }
        }).apply {
            setIsLongpressEnabled(false)
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        logx { "PictureView: onTouchEvent event=$event" }
        return gestureDetector.onTouchEvent(event)
    }
}