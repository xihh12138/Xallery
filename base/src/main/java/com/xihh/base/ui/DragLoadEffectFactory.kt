package com.xihh.base.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView
import com.xihh.base.util.getTextSizeF
import com.xihh.base.util.logx

open class DragLoadEffectFactory(private val dragParams: DragParams, reboundDuration: Long = 500) :
    DampEdgeEffectFactory(reboundDuration) {

    private val paint = Paint().apply {
        color = dragParams.textColor
        textSize = dragParams.textSize
    }

    override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
        return DragLoadEdgeEffect(recyclerView, direction)
    }

    open inner class DragLoadEdgeEffect(view: View, direction: Int) :
        DampEdgeEffect(view, direction) {

        private val lastTextSize = paint.getTextSizeF(dragParams.lastText)
        private val nextTextSize = paint.getTextSizeF(dragParams.nextText)

        override fun draw(canvas: Canvas): Boolean {
            logx { "DampEdgeEffectFactory: draw   " }
            when (direction) {
                DIRECTION_TOP -> {
                    if (dragParams.canPullLast()) {
                        val top = -dragParams.padding
                        val left = (view.width - lastTextSize.width) / 2
                        canvas.drawText(dragParams.lastText, left, top, paint)
                    }
                }
                DIRECTION_BOTTOM -> {
                    if (dragParams.canPullNext()) {
                        // ---------- rv默认给我旋转了180，要恢复成原样 ----------
                        canvas.restore()
                        val top =
                            view.height + dragParams.padding + nextTextSize.height + view.paddingTop - view.paddingBottom
                        val left = (view.width - nextTextSize.width) / 2
                        canvas.drawText(dragParams.nextText, left, top, paint)
                    }
                }
            }

            return super.draw(canvas)
        }

        override fun onReleaseFirst(finalPullDistance: Float) {
            logx { "DragLoadEdgeEffect: onReleaseFirst   finalPullDistance=$finalPullDistance" }
            when (direction) {
                DIRECTION_TOP -> {
                    val triggerDistance = lastTextSize.height + dragParams.padding
                    logx { "DragLoadEdgeEffect: onReleaseFirst   triggerDistance=$triggerDistance" }
                    if (dragParams.canPullLast() && finalPullDistance > triggerDistance) {
                        dragParams.onPullLast()
                    }
                }
                DIRECTION_BOTTOM -> {
                    val triggerDistance = nextTextSize.height + dragParams.padding
                    logx { "DragLoadEdgeEffect: onReleaseFirst   triggerDistance=$triggerDistance" }
                    if (dragParams.canPullNext() && finalPullDistance > triggerDistance) {
                        dragParams.onPullNext()
                    }
                }
            }
        }
    }

    data class DragParams(
        val padding: Float,
        val nextText: String,
        val lastText: String,
        val textSize: Float,
        val textColor: Int,
        val canPullNext: () -> Boolean,
        val canPullLast: () -> Boolean,
        val onPullNext: () -> Unit,
        val onPullLast: () -> Unit,
    )

    companion object {

        const val FLING_TRANSLATION_MAGNITUDE = 1
    }
}
