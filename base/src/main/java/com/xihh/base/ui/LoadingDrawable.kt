package com.xihh.base.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.graphics.drawable.ShapeDrawable
import com.xihh.base.R

class LoadingDrawable(context: Context) : ShapeDrawable(),
    Animatable, ValueAnimator.AnimatorUpdateListener {
    private var mProgressDegree = 0f
    private val mValueAnimator: ValueAnimator = ValueAnimator.ofInt(0, -3600)

    private val loadingDrawable = context.getDrawable(R.drawable.ic_loading)?.also {
        it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
    }

    override fun getIntrinsicWidth(): Int {
        return loadingDrawable?.intrinsicWidth ?: 0
    }

    override fun getIntrinsicHeight(): Int {
        return loadingDrawable?.intrinsicHeight ?: 0
    }

    init {
        mValueAnimator.duration = 3000
        mValueAnimator.interpolator = null
        mValueAnimator.repeatCount = ValueAnimator.INFINITE
        mValueAnimator.repeatMode = ValueAnimator.RESTART
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val value = animation.animatedValue as Int
        mProgressDegree = 15f * (value / 30)

        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        val drawableWidth = loadingDrawable?.intrinsicWidth ?: 0
        val drawableHeight = loadingDrawable?.intrinsicHeight ?: 0

        canvas.translate(canvas.width / 2f, canvas.height / 2f)
        canvas.rotate(mProgressDegree)
        canvas.translate(-drawableWidth / 2f, -drawableHeight / 2f)
        loadingDrawable?.draw(canvas)
    }

    override fun start() {
        if (!mValueAnimator.isRunning) {
            mValueAnimator.addUpdateListener(this)
            mValueAnimator.start()
        }
    }

    override fun stop() {
        if (mValueAnimator.isRunning) {
            val animator: Animator = mValueAnimator
            animator.removeAllListeners()
            mValueAnimator.removeAllUpdateListeners()
            mValueAnimator.cancel()
        }
    }

    override fun isRunning(): Boolean {
        return mValueAnimator.isRunning
    }
}