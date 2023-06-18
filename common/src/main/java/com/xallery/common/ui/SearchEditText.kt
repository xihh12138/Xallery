package com.xallery.common.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.withTranslation
import androidx.core.widget.addTextChangedListener
import com.xallery.common.R

class SearchEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {

    private val searchIconRect = Rect(
        0,
        0,
        resources.getDimensionPixelOffset(com.xihh.base.R.dimen.dp_16),
        resources.getDimensionPixelOffset(com.xihh.base.R.dimen.dp_16)
    )

    private val searchIconPadding = Rect(
        resources.getDimensionPixelOffset(com.xihh.base.R.dimen.dp_10),
        0,
        resources.getDimensionPixelOffset(com.xihh.base.R.dimen.dp_10),
        0
    )

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                onSearchContentChange?.invoke(text?.toString() ?: "")
            }
        }
    }

    /**
     * 是否要加上搜索的特殊符号输入限制
     **/
    var isFreeInput = true

    var onSearchContentChange: ((String) -> Unit)? = null
    var searchConfirmInterval = 400L

    val length get() = text?.length
    val isEmpty get() = text.isNullOrEmpty()

    init {
        hint = context.getString(R.string.search_hint)
        background =
            ContextCompat.getDrawable(context, R.drawable.selector_bg_stroke_primary_gray_8)

        setPaddingRelative(
            resources.getDimensionPixelOffset(com.xihh.base.R.dimen.dp_10),
            resources.getDimensionPixelOffset(com.xihh.base.R.dimen.dp_8),
            searchIconPadding.left + searchIconRect.right - searchIconRect.left + searchIconPadding.right,
            resources.getDimensionPixelOffset(com.xihh.base.R.dimen.dp_8)
        )

        setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(com.xihh.base.R.dimen.sp_14)
        )
        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        setHintTextColor(ContextCompat.getColor(context, R.color.text_hint))
        isSingleLine = true

        addTextChangedListener {
            handler.removeMessages(1)
            handler.sendEmptyMessageDelayed(1, searchConfirmInterval)

//            if (!isFreeInput) {
//
//            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        ContextCompat.getDrawable(context, R.drawable.ic_search)?.let {
            it.bounds = searchIconRect
            canvas.withTranslation(
                width.toFloat() - (searchIconRect.right - searchIconRect.left) - searchIconPadding.right + scrollX,
                (height - (searchIconRect.bottom - searchIconRect.top) + scrollY) / 2f
            ) {
                it.draw(this)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeMessages(1)
    }
}