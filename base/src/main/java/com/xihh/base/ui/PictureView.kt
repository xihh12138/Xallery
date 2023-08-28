package com.xihh.base.ui

import android.animation.FloatArrayEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.util.AttributeSet
import android.view.*
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import androidx.core.view.GestureDetectorCompat
import com.xihh.base.util.logx
import java.util.*
import kotlin.math.abs
import kotlin.math.pow


class PictureView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs),
    PictureViewActionListener by PictureViewActionListenerHelper() {

    private val touchUpSlop: Int
    private val touchLeftSlop: Int
    private val touchRightSlop: Int
    private val touchDownSlop: Int
    private var nestedOrientation = ORIENTATION_HORIZONTAL
    private var firstScrollDirection = DIRECTION_NONE
    private var floatingDraggableDirection = if (nestedOrientation == ORIENTATION_HORIZONTAL) {
        DIRECTION_DOWN
    } else {
        DIRECTION_RIGHT
    }

    private val dragDistanceRatioInterpolator = AccelerateInterpolator()
    private var dragDistanceRatio = 0f
    private var dragCancelDistance = 0

    private var downX = 0f
    private var downY = 0f

    private var dragX = 0f
    private var dragY = 0f

    private val flingCancelVelocity =
        ViewConfiguration.get(context).scaledMinimumFlingVelocity shl 2

    private val scaleFlingScroller = OverScroller(context, DecelerateInterpolator())
    private var smallScale = 0f
    private var bigScale = 0f
    private var zoomScale = 1f
    private val isZoom: Boolean get() = zoomScale > smallScale
    private var zoomSmallestDistance = 0

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
                downX = e.x
                downY = e.y

                // ---------- 如果是放大模式直接不让父布局拦截 ----------
                if (isZoom) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                return true
            }

            override fun onScroll(
                e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float,
            ): Boolean {
                logx { "PictureView: onScroll e2=$e2" }
                val totalDistanceX = e2.x - e1.x
                val totalDistanceY = e2.y - e1.y

                handleNestedScroll(
                    totalDistanceX,
                    totalDistanceY,
                    firstScrollDirection,
                    nestedOrientation
                )

                if (isZoom) {
                    handleScaleScrollGesture(distanceX, distanceY)
                } else {
                    handleFloatingScrollGesture(totalDistanceX, totalDistanceY)
                }

                return true
            }

            override fun onFling(
                e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float,
            ): Boolean {
                // ---------- 求速度的矢量和 ----------
                if (!resetValueAnimator.isRunning) {
                    handleScaleFling(velocityX, velocityY)
                }

                return true
            }

            /**
             * onDoubleTap()方法是只要用户的手指第二次down，就会立刻触发，如果需要监听用户双击抬起操作需要在onDoubleTapEvent()里判断
             **/
            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                handleDoubleClickEvent(e)
                return true
            }
        }).apply {
            setIsLongpressEnabled(false)
        }

    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                logx { "PictureView: onScale   scaleFactor=${detector.scaleFactor} (${detector.previousSpan}-${detector.currentSpan}) focus=(${detector.focusX},${detector.focusY})" }
                handleScaleGesture(detector.scaleFactor)
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                logx { "PictureView: onScaleBegin   " }
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                // ---------- 缩放结束，需要计算边界值，自动消除图片空隙 ----------
                val drawableWidth = drawable.bounds.width().toFloat()
                val drawableHeight = drawable.bounds.height().toFloat()
                val horizonClamp = Math.abs((drawableWidth * zoomScale - width) / 2)
                val verticalClamp = Math.abs((drawableHeight * zoomScale - height) / 2)
                val newDragX = dragX.coerceIn(-horizonClamp, horizonClamp)
                val newDragY = dragY.coerceIn(-verticalClamp, verticalClamp)

                resetValueAnimator.setObjectValues(
                    floatArrayOf(dragX, dragY, zoomScale),
                    floatArrayOf(newDragX, newDragY, zoomScale)
                )
                resetValueAnimator.start()

                postOnAnimation(resetRunnable)
            }
        })

    private val resetValueAnimator =
        ValueAnimator.ofObject(FloatArrayEvaluator(), floatArrayOf()).setDuration(
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        )

    private val resetRunnable = object : Runnable {
        override fun run() {
            val value = resetValueAnimator.animatedValue as? FloatArray ?: return
            dragX = value[0]
            dragY = value[1]
            zoomScale = value[2]

            invalidate()

            if (resetValueAnimator.isRunning) {
                postOnAnimation(this)
            }
        }
    }

    private val zoomValueAnimator =
        ValueAnimator.ofObject(FloatArrayEvaluator(), floatArrayOf()).setDuration(
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        )

    private val zoomRunnable = object : Runnable {
        override fun run() {
            val value = zoomValueAnimator.animatedValue as? FloatArray ?: return
            dragX = value[0]
            dragY = value[1]
            zoomScale = value[2]

            invalidate()

            if (zoomValueAnimator.isRunning) {
                postOnAnimation(this)
            }
        }
    }

    private val scaleFlingRunnable = object : Runnable {

        override fun run() {
            if (scaleFlingScroller.computeScrollOffset()) {
                if (isZoom) {
                    handleScaleFlingAnimation(
                        scaleFlingScroller.currX.toFloat(), scaleFlingScroller.currY.toFloat()
                    )
                }

                postOnAnimation(this)
            } else {
                if (!isZoom) {
                    reset()
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handle = scaleGestureDetector.onTouchEvent(event)
        if (!scaleGestureDetector.isInProgress) {
            handle = gestureDetector.onTouchEvent(event)
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                reset()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
            }
        }

        logx { "PictureView: onTouchEvent($handle) event=$event" }

        return handle
    }

    private fun handleNestedScroll(
        totalDistanceX: Float,
        totalDistanceY: Float,
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
            if (isZoom) {
                logx { "PictureView: handleNestedScroll  isZoom,requestDisallowInterceptTouchEvent " }
                parent.requestDisallowInterceptTouchEvent(true)
            } else if (firstScrollDirection.isHorizontalDirection) {
                // ---------- 滑动方向和父布局需要处理的方向一致，直接不处理 ----------
                logx { "PictureView: handleNestedScroll   滑动方向和父布局需要处理的方向一致，直接不处理 " }
                reset()
            } else if (floatingDraggableDirection == firstScrollDirection) {
                logx { "PictureView: handleNestedScroll   requestDisallowInterceptTouchEvent " }
                parent.requestDisallowInterceptTouchEvent(true)
            }
        } else {
            when {
                totalDistanceX > touchRightSlop && !curScrollDirection.isHorizontalDirection -> {
                    firstScrollDirection = DIRECTION_RIGHT
                }

                totalDistanceX < touchLeftSlop && !curScrollDirection.isHorizontalDirection -> {
                    firstScrollDirection = DIRECTION_LEFT
                }

                totalDistanceY < touchUpSlop && !curScrollDirection.isVerticalDirection -> {
                    firstScrollDirection = DIRECTION_UP
                }

                totalDistanceY > touchDownSlop && !curScrollDirection.isVerticalDirection -> {
                    firstScrollDirection = DIRECTION_DOWN
                }
            }

            logx { "PictureView: handleNestedScroll scrollDirection=$firstScrollDirection totalDistanceX=$totalDistanceX totalDistanceY=$totalDistanceY" }
            if (isZoom) {
                logx { "PictureView: handleNestedScroll  isZoom,requestDisallowInterceptTouchEvent " }
                parent.requestDisallowInterceptTouchEvent(true)
            } else if (firstScrollDirection.isVerticalDirection) {
                // ---------- 滑动方向和父布局需要处理的方向一致，直接不处理 ----------
                logx { "PictureView: handleNestedScroll   滑动方向和父布局需要处理的方向一致，直接不处理 " }
                reset()
            } else if (floatingDraggableDirection == firstScrollDirection) {
                logx { "PictureView: handleNestedScroll   requestDisallowInterceptTouchEvent " }
                parent.requestDisallowInterceptTouchEvent(true)
            }
        }
    }

    private fun handleScaleScrollGesture(distanceX: Float, distanceY: Float) {
        if (!scaleFlingScroller.isFinished) {
            scaleFlingScroller.forceFinished(true)
        }
        dragX -= distanceX
        dragY -= distanceY

        val drawable = drawable ?: return
        val drawableWidth = drawable.bounds.width().toFloat()
        val drawableHeight = drawable.bounds.height().toFloat()
        val width = width
        val height = height
        val horizonClamp = Math.abs((drawableWidth * zoomScale - width) / 2)
        val verticalClamp = Math.abs((drawableHeight * zoomScale - height) / 2)
        dragX = dragX.coerceIn(-horizonClamp, horizonClamp)
        dragY = dragY.coerceIn(-verticalClamp, verticalClamp)

        logx { "PictureView: handleScaleScrollGesture dragX=$dragX dragY=$dragY" }

        invalidate()
    }

    private fun handleScaleFlingAnimation(totalDistanceX: Float, totalDistanceY: Float) {
        dragX = totalDistanceX
        dragY = totalDistanceY

        val drawable = drawable ?: return
        val drawableWidth = drawable.bounds.width().toFloat()
        val drawableHeight = drawable.bounds.height().toFloat()
        val width = width
        val height = height
        val horizonClamp = Math.abs((drawableWidth * zoomScale - width) / 2)
        val verticalClamp = Math.abs((drawableHeight * zoomScale - height) / 2)
        dragX = dragX.coerceIn(-horizonClamp, horizonClamp)
        dragY = dragY.coerceIn(-verticalClamp, verticalClamp)

        logx { "PictureView: handleScaleFling totalDistanceX=$totalDistanceX totalDistanceY=$totalDistanceY dragX=$dragX dragY=$dragY" }

        invalidate()
    }

    private fun handleFloatingScrollGesture(totalDistanceX: Float, totalDistanceY: Float) {
        val dragCancelDistance = dragCancelDistance
        val totalDistance = Math.sqrt(
            Math.pow(Math.abs(totalDistanceX).toDouble(), 2.0)
                    + Math.pow(Math.abs(totalDistanceY).toDouble(), 2.0)
        ).toFloat()
        val distanceRatio = dragDistanceRatioInterpolator.getInterpolation(
            (totalDistance / dragCancelDistance).coerceIn(0f, 1f)
        )
        val zoomRatio = totalDistance / zoomSmallestDistance

        dragX = totalDistanceX
        dragY = totalDistanceY
        zoomScale = (1 - zoomRatio)
        dragDistanceRatio = distanceRatio

        notifyListenersDrag(totalDistanceX, totalDistanceY, totalDistance, distanceRatio)

//        logx { "PictureView: handleFloatingScrollGesture dragX=$dragX dragY=$dragY" }

        invalidate()
    }

    private var isPostingDoubleClick = false

    private fun handleDoubleClickEvent(e: MotionEvent) {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPostingDoubleClick = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isPostingDoubleClick) {
                    val totalDistanceX = e.x - downX
                    val totalDistanceY = e.y - downY
                    val totalDistance = Math.sqrt(
                        Math.pow(Math.abs(totalDistanceX).toDouble(), 2.0)
                                + Math.pow(Math.abs(totalDistanceY).toDouble(), 2.0)
                    ).toFloat()
                    if (totalDistance > touchDownSlop) {
                        isPostingDoubleClick = false
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isPostingDoubleClick) {
                    // ---------- 响应双击缩放 ----------
                    if (isZoom) {
                        // ---------- 缩小 ----------
                        zoomValueAnimator.setObjectValues(
                            floatArrayOf(dragX, dragY, zoomScale),
                            floatArrayOf(0f, 0f, smallScale)
                        )
                    } else {
                        // ---------- 放大 ----------
                        val drawable = drawable ?: return
                        val drawableWidth = drawable.bounds.width().toFloat()
                        val drawableHeight = drawable.bounds.height().toFloat()
                        val width = width
                        val height = height
                        var newDragX = (e.x - width / 2f) - (e.x - width / 2) * bigScale / zoomScale
                        var newDragY =
                            (e.y - height / 2f) - (e.y - height / 2) * bigScale / zoomScale
                        val horizonClamp = Math.abs((drawableWidth * bigScale - width) / 2)
                        val verticalClamp = Math.abs((drawableHeight * bigScale - height) / 2)
                        newDragX = newDragX.coerceIn(-horizonClamp, horizonClamp)
                        newDragY = newDragY.coerceIn(-verticalClamp, verticalClamp)
                        zoomValueAnimator.setObjectValues(
                            floatArrayOf(dragX, dragY, zoomScale),
                            floatArrayOf(newDragX, newDragY, bigScale)
                        )
                    }
                    zoomValueAnimator.start()

                    postOnAnimation(zoomRunnable)
                }
            }
        }
    }

    private fun handleScaleFling(velocityX: Float, velocityY: Float) {
        val totalVelocity = Math.sqrt(
            abs(velocityX).toDouble().pow(2.0) + abs(velocityY).toDouble().pow(2.0)
        ).toFloat()

        val drawable = drawable ?: return
        val drawableWidth = drawable.bounds.width()
        val curDrawableHeight = drawable.bounds.height()
        val width = width.toFloat()
        val height = height.toFloat()

        val horizonClamp = Math.abs((drawableWidth * zoomScale - width) / 2).toInt()
        val verticalClamp = Math.abs((curDrawableHeight * zoomScale - height) / 2).toInt()

        if (totalVelocity > ViewConfiguration.get(context).scaledMinimumFlingVelocity) {
            scaleFlingScroller.fling(
                dragX.toInt(), dragY.toInt(),
                velocityX.toInt(), velocityY.toInt(),
                -horizonClamp,
                horizonClamp,
                -verticalClamp,
                verticalClamp
            )

            postOnAnimation(scaleFlingRunnable)

            if (!isZoom && totalVelocity >= flingCancelVelocity) {
                notifyListenersFlingCancel()
            }
        }
    }

    private fun handleScaleGesture(scaleFactor: Float) {
        zoomScale = (zoomScale * scaleFactor).coerceIn(0.1f, 20f)

        invalidate()
    }

    private fun reset() {
        // ---------- 如果在惯性滑动，则延迟重置 ----------
        if (/*!flingOverScroller.computeScrollOffset() && */!(isZoom || zoomValueAnimator.isRunning)) {
            firstScrollDirection = DIRECTION_NONE
            parent.requestDisallowInterceptTouchEvent(false)

            resetValueAnimator.setObjectValues(
                floatArrayOf(dragX, dragY, zoomScale),
                floatArrayOf(0f, 0f, smallScale)
            )
            resetValueAnimator.start()

            postOnAnimation(resetRunnable)

            notifyListenersDragFinish(dragDistanceRatio)
            dragDistanceRatio = 0f
        }
    }

    private fun notifyListenersDrag(
        totalDistanceX: Float, totalDistanceY: Float, totalDistance: Float, distanceRatio: Float,
    ) {
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
            canvas.withScale(zoomScale, zoomScale, width / 2f, height / 2f) {
                super.onDraw(canvas)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // ---------- 因为入场出场动画会导致activity和view大小改变，所以这里加一个延时处理，避免在入场和出场时改变大小 ----------
        removeCallbacks(sizeChangeRunnable)
        postDelayed(sizeChangeRunnable, 100)
    }

    private val sizeChangeRunnable = object : Runnable {
        override fun run() {
            val width = width.toFloat()
            val height = height.toFloat()

            dragCancelDistance = Math.min(width, height).toInt()
            zoomSmallestDistance = Math.max(width, height).toInt()

            val drawable = drawable ?: return
            val drawableWidth = drawable.bounds.width().toFloat()
            val drawableHeight = drawable.bounds.height().toFloat()

            if (drawableWidth / drawableHeight > width / height) {
                smallScale = width / drawableWidth
                bigScale = height / drawableHeight
            } else {
                smallScale = height / drawableHeight
                bigScale = width / drawableWidth
            }
            logx { "PictureView: sizeChangeRunnable drawableWidth=$drawableWidth drawableHeight=$drawableHeight width=$width height=$height smallScale=$smallScale  bigScale=$bigScale" }

            zoomScale = smallScale

            invalidate()
        }
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

    interface DragListener {
        fun onDrag(
            totalDistanceX: Float,
            totalDistanceY: Float,
            totalDistance: Float,
            distanceRatio: Float,
        )

        fun onFlingCancel()

        fun onFinish(finalDistanceRatio: Float)
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


interface PictureViewActionListener {

    val dragListenerList: List<PictureView.DragListener>

    fun addDragListener(listener: PictureView.DragListener)

    fun removeDragListener(listener: PictureView.DragListener)

    fun removeAllDragListener()
}

class PictureViewActionListenerHelper : PictureViewActionListener {

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

interface BitmapLoader {

    fun init(view: View)

    fun loadUri(uri: Uri)

    fun getBitmap(): Bitmap?

}

class BitmapLoaderImpl : BitmapLoader {

    private var bitmap: Bitmap? = null

    private lateinit var view: View

    override fun init(view: View) {
        this.view = view
    }

    override fun loadUri(uri: Uri) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        view.context.contentResolver.openFileDescriptor(uri, "r")?.use {
            BitmapFactory.decodeFileDescriptor(it.fileDescriptor, null, options)
            options.inJustDecodeBounds = false
            options.inDensity = options.outWidth
            options.inTargetDensity = view.width
            bitmap = BitmapFactory.decodeFileDescriptor(it.fileDescriptor, null, options)
        }
    }

    override fun getBitmap(): Bitmap? = bitmap
}