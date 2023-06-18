package com.xallery.common.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.xallery.common.R

/**
 * 版权：中国麦风科技有限公司
 * @author Hem
 * @version 1.0.0
 * @创建日期 2023/3/28,14:55
 * @修改日期 2023/3/28
 * @description ---
 */
class GradientTextView : AppCompatTextView {

    companion object {
        const val DEFAULT_START_COLOR = 0xFF7A2FEA.toInt()
        const val DEFAULT_END_COLOR = 0xFF1F81FF.toInt()
    }

    private var startColor: Int = DEFAULT_START_COLOR
    private var endColor: Int = DEFAULT_END_COLOR

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ): this(context, attrs, android.R.attr.textViewStyle)

    constructor(context: Context): super(context, null)

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView)
        startColor = a.getColor(R.styleable.GradientTextView_startColor, DEFAULT_START_COLOR)
        endColor = a.getColor(R.styleable.GradientTextView_endColor, DEFAULT_END_COLOR)
        a.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            paint.shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), startColor, endColor, Shader.TileMode.CLAMP)
        }
    }
}