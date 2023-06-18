package com.xihh.base.ui

import android.animation.RectEvaluator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.EdgeEffect
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView

open class DampEdgeEffectFactory(private val reboundDuration: Long = 500) :
    RecyclerView.EdgeEffectFactory() {

    override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
        return DampEdgeEffect(recyclerView, direction, reboundDuration)
    }

    open class DampEdgeEffect(
        protected val view: View,
        protected val direction: Int,
        private val reboundDuration: Long = 500,
    ) : EdgeEffect(view.context) {

        protected var mState = STATE_IDLE

        protected var dragDistance = 0

        private val cachePadding = Rect()

        private var releaseAnimator: ValueAnimator? = null

        override fun onPull(deltaDistance: Float, displacement: Float) {
            super.onPull(deltaDistance, displacement)
            handlePull(deltaDistance)
        }

        private fun handlePull(deltaDistance: Float) {
            var paddingLeft = view.paddingLeft
            var paddingRight = view.paddingRight
            var paddingTop = view.paddingTop
            var paddingBottom = view.paddingBottom
            if (mState != STATE_PULL) {
                val releaseAnimator = releaseAnimator
                if (releaseAnimator == null || (releaseAnimator.isStarted && !releaseAnimator.isRunning)) {
                    cachePadding.left = paddingLeft
                    cachePadding.right = paddingRight
                    cachePadding.top = paddingTop
                    cachePadding.bottom = paddingBottom
                } else {
                    releaseAnimator.cancel()
                }
            }
            mState = STATE_PULL

            val translationDelta = view.width * deltaDistance * 0.8f

            when (direction) {
                DIRECTION_TOP -> {
                    paddingTop = Math.min(paddingTop + translationDelta.toInt(), view.height - 1)
                    dragDistance = paddingTop
                }
                DIRECTION_BOTTOM -> {
                    paddingBottom = Math.min(paddingBottom + translationDelta.toInt(), view.height - 1)
                    dragDistance = paddingBottom
                }
                DIRECTION_LEFT -> {
                    paddingLeft = Math.min(paddingLeft + translationDelta.toInt(), view.width - 1)
                    dragDistance = paddingLeft
                }
                DIRECTION_RIGHT -> {
                    paddingRight = Math.min(paddingRight + translationDelta.toInt(), view.width - 1)
                    dragDistance = paddingRight
                }
            }
            view.setPadding(
                paddingLeft, paddingTop, paddingRight, paddingBottom
            )
        }

        override fun onRelease() {
            super.onRelease()

            if (mState != STATE_PULL && mState != STATE_PULL_DECAY) {
                return
            }
            mState = STATE_RECEDE
            dragDistance = 0

            val startRect = Rect(
                view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
            )

            val finalDragDistance = when (direction) {
                DIRECTION_TOP -> {
                    startRect.top
                }
                DIRECTION_BOTTOM -> {
                    startRect.bottom
                }
                DIRECTION_LEFT -> {
                    startRect.left
                }
                else -> {
                    startRect.right
                }
            }

            val cachePadding = Rect(cachePadding)
            releaseAnimator =
                ValueAnimator.ofObject(RectEvaluator(Rect()), startRect, cachePadding).apply {
                    duration = reboundDuration
                    interpolator = DecelerateInterpolator(2.0f)
                    addUpdateListener { valueAnimator ->
                        val padding = valueAnimator.animatedValue as Rect
                        view.setPadding(
                            padding.left, padding.top, padding.right, padding.bottom
                        )
                    }
                    addListener(onEnd = {
                        mState = STATE_IDLE
                    }, onCancel = {
                        mState = STATE_RECEDE
                    })
                    start()
                }

            onReleaseFirst(finalDragDistance.toFloat())
        }

        override fun onAbsorb(velocity: Int) {
            super.onAbsorb(velocity)
        }

        override fun draw(canvas: Canvas): Boolean {
            // ---------- 为了保证EdgeEffect状态不异常，必须要调用父类的draw方法，又要不绘制原来的波纹，那就把size设为0 ----------
            setSize(0, 0)
            return super.draw(canvas)
        }

        open fun onReleaseFirst(finalPullDistance: Float) {

        }
    }

    companion object {

        const val STATE_IDLE = 0
        const val STATE_PULL = 1
        const val STATE_ABSORB = 2
        const val STATE_RECEDE = 3
        const val STATE_PULL_DECAY = 4

        const val FLING_TRANSLATION_MAGNITUDE = 1
    }
}
