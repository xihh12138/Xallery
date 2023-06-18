package com.xihh.base.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatButton

class LoadingButton(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    AppCompatButton(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.buttonStyle)

    private val loadingDrawable = LoadingDrawable(context)

    init {
        viewTreeObserver.addOnGlobalLayoutListener {
            loadingDrawable.setBounds(0, 0, width, height)
        }
    }

    private var memoryText: CharSequence = ""

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return loadingDrawable.isRunning || super.onTouchEvent(event)
    }

    fun loading(isLoading: Boolean) {
        if (isLoading) {
            loadingDrawable.start()
            memoryText = text
            text = ""
        } else {
            loadingDrawable.stop()
            text = memoryText
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (loadingDrawable.isRunning) {
            loadingDrawable.draw(canvas)

            postInvalidate()
        }
    }
}