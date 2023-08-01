package com.xallery.picture.ui

import android.animation.IntArrayEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import com.xihh.base.util.logx


class VerticalTwoViewPager(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    private val interceptSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var isInterceptTouchEvent = false

    private val pageChangeDistanceRatio = 0.5f
    private var pageChangeDistance = 0
    private var pageChangeVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity shl 2

    private val curPage get() = if (scrollY < height) 0 else 1

    private val flingScroller = OverScroller(context, DecelerateInterpolator())

    private val gestureDetector =
        GestureDetectorCompat(context, object : SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return handleDownGesture(e)
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float,
            ): Boolean {
                logx { "VerticalTwoViewPager: onScroll e2=$e2" }
                val totalDistanceX = e2.x - e1.x
                val totalDistanceY = e2.y - e1.y

                val intercept = handleNestedScroll(totalDistanceX, totalDistanceY)

                if (intercept) {
                    handleScrollGesture(distanceX, distanceY)
                }

                return intercept
            }

            override fun onFling(
                e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float,
            ): Boolean {
                handleFling(velocityX, velocityY)

                return true
            }
        }).apply {
            setIsLongpressEnabled(false)
        }

    private val resetValueAnimator =
        ValueAnimator.ofObject(IntArrayEvaluator(), intArrayOf()).setDuration(
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        )

    private val resetRunnable = object : Runnable {
        override fun run() {
            val value = resetValueAnimator.animatedValue as? IntArray ?: return
            scrollY = value[0]

            if (resetValueAnimator.isRunning) {
                postOnAnimation(this)
            }
        }
    }

    private val flingRunnable = object : Runnable {

        override fun run() {
            handleFlingAnimation(flingScroller.currX, flingScroller.currY)

            if (flingScroller.computeScrollOffset()) {
                postOnAnimation(this)
            } else {
                reset()
            }
        }
    }

    private fun handleNestedScroll(totalDistanceX: Float, totalDistanceY: Float): Boolean {
        if (isInterceptTouchEvent) {
            return true
        }

        val scrollY = scrollY
        val height = height

        if (totalDistanceY > interceptSlop) {
            // ---------- 向下拖动，页面向下滚动(从1切换到0) ----------
            logx { "VerticalTwoViewPager: handleNestedScroll   向下拖动，页面向下滚动(从1切换到0)" }
            if (scrollY > 0) {
                // ---------- 可以拖动 ----------
                isInterceptTouchEvent = true
                parent.requestDisallowInterceptTouchEvent(true)
            }/* else if (scrollY == 0) {
                // ---------- 拖到头了 ----------
                isInterceptTouchEvent = false
                parent.requestDisallowInterceptTouchEvent(false)
            }*/
        } else if (totalDistanceY < -interceptSlop) {
            // ---------- 向上拖动，页面向上滚动(从0切换到1) ----------
            logx { "VerticalTwoViewPager: handleNestedScroll   向上拖动，页面向上滚动(从0切换到1)" }
            if (scrollY < height) {
                // ---------- 可以拖动 ----------
                isInterceptTouchEvent = true
                parent.requestDisallowInterceptTouchEvent(true)
            }/* else if (scrollY == height) {
                // ---------- 拖到头了 ----------
                isInterceptTouchEvent = false
                parent.requestDisallowInterceptTouchEvent(false)
            }*/
        }

        return isInterceptTouchEvent
    }

    private fun handleScrollGesture(
        distanceX: Float,
        distanceY: Float,
    ) {
        scrollY += distanceY.toInt()
    }

    private fun handleFlingAnimation(
        totalDistanceX: Int,
        totalDistanceY: Int,
    ) {
        scrollY = totalDistanceY
    }

    private fun handleFling(velocityX: Float, velocityY: Float) {
        if (Math.abs(velocityY) > pageChangeVelocity) {
            // ------------ 加速度大于设定好的翻页阈值，直接执行翻页动画 ------------
            if (velocityY > 0) {
                resetValueAnimator.setObjectValues(
                    intArrayOf(scrollY),
                    intArrayOf(0)
                )
            } else {
                resetValueAnimator.setObjectValues(
                    intArrayOf(scrollY),
                    intArrayOf(height)
                )
            }
            resetValueAnimator.start()

            postOnAnimation(resetRunnable)
        }
//        if (Math.abs(velocityY) > ViewConfiguration.get(context).scaledMinimumFlingVelocity) {
//            val absMaximumVelocity =
//                Math.abs(ViewConfiguration.get(context).scaledMaximumFlingVelocity)
//            flingScroller.fling(
//                0,
//                scrollY,
//                velocityX.toInt(),
//                -velocityY.toInt().coerceIn(-absMaximumVelocity, absMaximumVelocity),
//                0,
//                0,
//                0,
//                height
//            )
//            logx { "VerticalTwoViewPager: handleFling velocityY=$velocityY startY=${flingScroller.startY} finalY=${flingScroller.finalY}" }
//
//            postOnAnimation(flingRunnable)
//        }
    }

    /**
     * @return true表示要拦截并处理后续事件，false表示不拦截
     **/
    private fun handleDownGesture(downEvent: MotionEvent): Boolean {
        isInterceptTouchEvent = false

        if (flingScroller.computeScrollOffset()) {
            flingScroller.forceFinished(true)
            isInterceptTouchEvent = true
        }

        if (resetValueAnimator.isRunning) {
            removeCallbacks(resetRunnable)
            resetValueAnimator.end()
            isInterceptTouchEvent = true
        }

        return isInterceptTouchEvent
    }

    private fun reset() {
//        if (flingScroller.isFinished) {
        if (!resetValueAnimator.isRunning) {
            val scrollY = scrollY
            if (scrollY > pageChangeDistance) {
                resetValueAnimator.setObjectValues(
                    intArrayOf(scrollY),
                    intArrayOf(height)
                )
            } else {
                resetValueAnimator.setObjectValues(
                    intArrayOf(scrollY),
                    intArrayOf(0)
                )
            }

            resetValueAnimator.start()

            postOnAnimation(resetRunnable)
        }
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        logx { "VerticalTwoViewPager: onInterceptTouchEvent   e=$e" }
        return gestureDetector.onTouchEvent(e)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        logx { "VerticalTwoViewPager: onTouchEvent   e=$e" }
        var handle = false
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // ---------- 如果这里能收到down事件，说明子view没有处理，这里就是我们最后处理的机会 ----------
                handle = true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                reset()
            }
        }

        return handle || gestureDetector.onTouchEvent(e)
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return if (direction > 0) {
            scrollY < height
        } else {
            scrollY > 0
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val height = height
        val width = width
        val childLeft = 0
        var childTop = 0
        val childRight = width
        var childBottom = height

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.layout(childLeft, childTop, childRight, childBottom)
            childTop += height
            childBottom += height
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pageChangeDistance = (h * pageChangeDistanceRatio).toInt()
    }

    fun scrollToUpPage(isSmooth: Boolean = true) {

    }

    fun scrollToDownPage(isSmooth: Boolean = true) {

    }
}