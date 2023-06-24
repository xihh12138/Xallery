package com.xallery.common.ui.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.widget.ProgressBar

class SmoothProgressBar: ProgressBar {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

    companion object {
        private const val WHAT_PROGRESS_START = 0
        private const val WHAT_PROGRESS_GOING = 1
    }



    private var realProgress: Int = 0

    private var onProgressFinished: (SmoothProgressBar.() -> Unit)? = null

    private var onProgressStart: (SmoothProgressBar.() -> Unit)? = null

    private val mHandler by lazy {
        object: Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    WHAT_PROGRESS_GOING -> {
                        val needRefresh = if (progress < realProgress) {
                            progress += 1
                            progress < realProgress
                        } else {
                            progress = realProgress
                            false
                        }

                        if (progress >= max) {
                            onProgressFinished?.invoke(this@SmoothProgressBar)
                        } else if (needRefresh) {
                            val newMsg = Message()
                            newMsg.what = WHAT_PROGRESS_GOING
                            sendMessageDelayed(newMsg, 10)
                        }
                    }
                    WHAT_PROGRESS_START -> {
                        progress = 0
                        onProgressStart?.invoke(this@SmoothProgressBar)
                    }
                }
            }
        }
    }

    fun postProgress(value: Int) {
        realProgress = value
        val msg = Message()
        msg.what = WHAT_PROGRESS_GOING
        mHandler.sendMessage(msg)
    }

    fun startProgress() {
        realProgress = 0
        mHandler.removeMessages(WHAT_PROGRESS_GOING)
        val msg = Message()
        msg.what = WHAT_PROGRESS_START
        mHandler.sendMessage(msg)
    }

    fun setOnProgressFinished(block: SmoothProgressBar.() -> Unit) {
        onProgressFinished = block
    }

    fun setOnProgressStart(block: SmoothProgressBar.() -> Unit) {
        onProgressStart = block
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacksAndMessages(null)

    }

}