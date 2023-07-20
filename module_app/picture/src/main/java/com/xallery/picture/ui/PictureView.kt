package com.xallery.picture.ui

import android.animation.FloatArrayEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.ViewConfiguration
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.withTranslation
import androidx.core.view.GestureDetectorCompat
import com.xihh.base.util.logx
import java.util.*

class PictureView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs),
    ActionListener by ActionListenerHelper() {

    private val touchUpSlop: Int
    private val touchLeftSlop: Int
    private val touchRightSlop: Int
    private val touchDownSlop: Int
    private var nestedOrientation = ORIENTATION_HORIZONTAL
    private var firstScrollDirection = DIRECTION_NONE

    private val dragDistanceRatioInterpolator = AccelerateInterpolator()
    private var lastDragDistanceRatio = 0f
    private var dragCancelDistance = 0

    private var dragX = 0f
    private var dragY = 0f

    private val flingCancelVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity shr 1

    init {
        val scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        if (nestedOrientation == ORIENTATION_HORIZONTAL) {
            touchUpSlop = -scaledTouchSlop
            touchLeftSlop = -scaledTouchSlop
            touchRightSlop = scaledTouchSlop
            touchDownSlop = scaledTouchSlop
        } else {
            touchUpSlop = -scaledTouchSlop
            touchLeftSlop = -scaledTouchSlop
            touchRightSlop = scaledTouchSlop
            touchDownSlop = scaledTouchSlop
        }
    }

    private val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }

            override fun onScroll(
                e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float,
            ): Boolean {
                logx { "PictureView: onScroll e2=$e2" }
                val totalDistanceX = e2.x - e1.x
                val totalDistanceY = e2.y - e1.y

                val curScrollDirection = firstScrollDirection
                val curNestedOrientation = nestedOrientation
                handleNestedScroll(
                    totalDistanceX,
                    totalDistanceY,
                    distanceX,
                    distanceY,
                    curScrollDirection,
                    curNestedOrientation
                )

                handleScrollGesture(totalDistanceX, totalDistanceY)

                return true
            }

            override fun onFling(
                e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                // ---------- 求速度的矢量和 ----------
                val totalVelocity = Math.sqrt(
                    Math.pow(Math.abs(velocityX).toDouble(), 2.0)
                            + Math.pow(Math.abs(velocityY).toDouble(), 2.0)
                ).toFloat()

                if (totalVelocity >= flingCancelVelocity) {
                    notifyListenersFlingCancel()
                }

                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                return super.onDoubleTapEvent(e)
            }
        }).apply {
            setIsLongpressEnabled(false)
        }

    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                return false
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                return false
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
            }
        })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handle = scaleGestureDetector.onTouchEvent(event)
        if (!scaleGestureDetector.isInProgress) {
            handle = gestureDetector.onTouchEvent(event)
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                reset()
            }
        }

        logx { "PictureView: onTouchEvent($handle) event=$event" }

        return handle
    }

    private fun handleNestedScroll(
        totalDistanceX: Float,
        totalDistanceY: Float,
        distanceX: Float,
        distanceY: Float,
        curScrollDirection: Int,
        curNestedOrientation: Int,
    ) {
        if (curNestedOrientation == ORIENTATION_HORIZONTAL) {
            when {
                totalDistanceX > touchRightSlop && !curScrollDirection.isVerticalDirection -> {
                    firstScrollDirection = DIRECTION_RIGHT
                }

                totalDistanceX < touchLeftSlop && !curScrollDirection.isVerticalDirection -> {
                    firstScrollDirection = DIRECTION_LEFT
                }

                totalDistanceY < touchUpSlop && curScrollDirection == DIRECTION_NONE -> {
                    firstScrollDirection = DIRECTION_UP
                }

                totalDistanceY > touchDownSlop && curScrollDirection == DIRECTION_NONE -> {
                    firstScrollDirection = DIRECTION_DOWN
                }
            }

            logx { "PictureView: handleNestedScroll scrollDirection=$firstScrollDirection totalDistanceX=$totalDistanceX totalDistanceY=$totalDistanceY" }
            if (firstScrollDirection.isHorizontalDirection) {
                // ---------- 滑动方向和父布局需要处理的方向一致，直接不处理 ----------
                reset()
            } else {
                parent.requestDisallowInterceptTouchEvent(true)
            }
        } else {
            when {
                distanceX > touchRightSlop && !curScrollDirection.isHorizontalDirection -> {
                    firstScrollDirection = DIRECTION_RIGHT
                }

                distanceX < touchLeftSlop && !curScrollDirection.isHorizontalDirection -> {
                    firstScrollDirection = DIRECTION_LEFT
                }

                distanceY < touchUpSlop && !curScrollDirection.isVerticalDirection -> {
                    firstScrollDirection = DIRECTION_UP
                }

                distanceY > touchDownSlop && !curScrollDirection.isVerticalDirection -> {
                    firstScrollDirection = DIRECTION_DOWN
                }
            }

            logx { "PictureView: handleNestedScroll scrollDirection=$firstScrollDirection totalDistanceX=$totalDistanceX totalDistanceY=$totalDistanceY" }
            if (firstScrollDirection.isVerticalDirection) {
                // ---------- 滑动方向和父布局需要处理的方向一致，直接不处理 ----------
                reset()
            } else {
                parent.requestDisallowInterceptTouchEvent(true)
            }
        }
    }

    private fun handleScrollGesture(totalDistanceX: Float, totalDistanceY: Float) {
        dragX = totalDistanceX
        dragY = totalDistanceY

        notifyListenersDrag(totalDistanceX, totalDistanceY)

        logx { "PictureView: handleScrollGesture dragX=$dragX dragY=$dragY" }

        invalidate()
    }

    private val resetValueAnimator =
        ValueAnimator.ofObject(FloatArrayEvaluator(), floatArrayOf()).setDuration(
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        )

    private val resetRunnable = object : Runnable {
        override fun run() {
            if (resetValueAnimator.isRunning) {
                val value = resetValueAnimator.animatedValue as FloatArray
                dragX = value[0]
                dragY = value[1]

                invalidate()

                postOnAnimation(this)
            }
        }
    }

    private fun reset() {
        firstScrollDirection = DIRECTION_NONE
        parent.requestDisallowInterceptTouchEvent(false)

        notifyListenersDragFinish(lastDragDistanceRatio)
        lastDragDistanceRatio = 0f

        resetValueAnimator.setObjectValues(floatArrayOf(dragX, dragY), floatArrayOf(0f, 0f))
        resetValueAnimator.start()

        postOnAnimation(resetRunnable)
    }

//    override fun canScrollHorizontally(direction: Int): Boolean {
//        return true
//    }
//
//    override fun canScrollVertically(direction: Int): Boolean {
//        return true
//    }
//
//    private fun canScroll(orientation: Int, delta: Float): Boolean {
//        val direction = -delta.sign.toInt()
//        return when (orientation) {
//            ViewPager2.ORIENTATION_HORIZONTAL -> canScrollHorizontally(direction)
//            ViewPager2.ORIENTATION_VERTICAL -> canScrollVertically(direction)
//            else -> throw IllegalArgumentException()
//        }
//    }

    private fun notifyListenersDrag(totalDistanceX: Float, totalDistanceY: Float) {
        val dragCancelDistance = dragCancelDistance
        val totalDistance = Math.sqrt(
            Math.pow(Math.abs(totalDistanceX).toDouble(), 2.0)
                    + Math.pow(Math.abs(totalDistanceY).toDouble(), 2.0)
        ).toFloat()
        val distanceRatio = dragDistanceRatioInterpolator.getInterpolation(
            (totalDistance / dragCancelDistance).coerceIn(0f, 1f)
        )

        lastDragDistanceRatio = distanceRatio

        dragListenerList.forEach {
            it.onDrag(totalDistanceX, totalDistanceY, totalDistance, distanceRatio)
        }
    }

    private fun notifyListenersDragFinish(lastDragDistanceRatio: Float) {
        dragListenerList.forEach {
            it.onFinish(lastDragDistanceRatio)
        }
    }

    private fun notifyListenersFlingCancel() {
        dragListenerList.forEach {
            it.onFlingCancel()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.withTranslation(dragX, dragY) {
            super.onDraw(canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        dragCancelDistance = Math.min(w, h)
    }

    interface DragListener {
        fun onDrag(
            totalDistanceX: Float,
            totalDistanceY: Float,
            totalDistance: Float,
            distanceRatio: Float,
        )

        fun onFlingCancel()

        fun onFinish(finalDistanceRatio: Float) {}
    }

    companion object {
        const val DIRECTION_NONE = 0
        const val DIRECTION_LEFT = 1
        const val DIRECTION_UP = 2
        const val DIRECTION_RIGHT = 3
        const val DIRECTION_DOWN = 4

        const val ORIENTATION_HORIZONTAL = 0
        const val ORIENTATION_VERTICAL = 1

        private val Int.isHorizontalDirection get() = this == DIRECTION_RIGHT || this == DIRECTION_LEFT
        private val Int.isVerticalDirection get() = this == DIRECTION_UP || this == DIRECTION_DOWN
    }
}

interface ActionListener {

    val dragListenerList: List<PictureView.DragListener>
    fun addDragListener(listener: PictureView.DragListener)
    fun removeDragListener(listener: PictureView.DragListener)
    fun removeAllDragListener()
}

class ActionListenerHelper : ActionListener {

    private val mDragListenerList = LinkedList<PictureView.DragListener>()

    override val dragListenerList get() = mDragListenerList

    override fun addDragListener(listener: PictureView.DragListener) {
        mDragListenerList.add(listener)
    }

    override fun removeDragListener(listener: PictureView.DragListener) {
        mDragListenerList.remove(listener)
    }

    override fun removeAllDragListener() {
        mDragListenerList.clear()
    }
}