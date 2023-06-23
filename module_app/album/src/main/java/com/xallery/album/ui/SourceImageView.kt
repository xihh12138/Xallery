package com.xallery.album.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.xallery.album.repo.bean.SourceUIInfo
import com.xallery.common.reposity.constant.Constant
import com.xallery.common.reposity.db.model.Source
import com.xallery.common.util.loadUri
import com.xihh.base.android.appContext
import com.xihh.base.util.drawableRes
import com.xihh.base.util.getTextSize
import com.xihh.base.util.toHMSString

class SourceImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private var sourceUIInfo: SourceUIInfo? = null

    private val durationTextPaint = TextPaint().apply {
        textSize = context.resources.getDimension(com.xihh.base.R.dimen.sp_13)
        color = ContextCompat.getColor(context, com.xallery.common.R.color.white)
    }

    private val infoBgPaint = Paint().apply {
        color = Color.BLACK
        alpha = (255 * 0.6).toInt()
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    fun loadSource(source: Source) {
        sourceUIInfo = SourceUIInfo.fromSource(source)
        loadUri(source.uri, source.key)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val sourceInfo = sourceUIInfo ?: return
        drawMimeType(sourceInfo, canvas)
        drawLocation(sourceInfo, canvas)
    }

    /**
     * 画在右上角
     **/
    private fun drawMimeType(sourceUIInfo: SourceUIInfo, canvas: Canvas) {
        val padding = context.resources.getDimensionPixelSize(com.xihh.base.R.dimen.dp_3)
        val cWidth = canvas.width
        if (sourceUIInfo.mimeType == Constant.MimeType.GIF) {
            gifDrawable?.let { d ->
                val bgPadding = context.resources.getDimension(com.xihh.base.R.dimen.dp_1)
                val size = context.resources.getDimensionPixelSize(com.xihh.base.R.dimen.dp_18)
                d.setBounds(
                    cWidth - padding - size,
                    padding,
                    cWidth - padding,
                    padding + size
                )
                canvas.drawRoundRect(
                    d.bounds.left - bgPadding,
                    d.bounds.top - bgPadding,
                    d.bounds.right + bgPadding,
                    d.bounds.bottom + bgPadding,
                    100f,
                    100f,
                    infoBgPaint
                )
                d.draw(canvas)
            }
        } else if (sourceUIInfo.mimeType.startsWith(Constant.MimeType.VIDEO_START)) {
            sourceUIInfo.durationMillis?.toHMSString()?.let { durationText ->
                val fontMetricsInt = durationTextPaint.fontMetricsInt
                val size = fontMetricsInt.descent - fontMetricsInt.ascent
                val textSize = durationTextPaint.getTextSize(durationText)
                videoDrawable?.let { d ->
                    val bgPadding = context.resources.getDimension(com.xihh.base.R.dimen.dp_2)
                    d.setBounds(
                        cWidth - padding - size,
                        padding,
                        cWidth - padding,
                        padding + size
                    )
                    canvas.drawRoundRect(
                        d.bounds.left - 15f - textSize.width - bgPadding,
                        d.bounds.top - bgPadding,
                        d.bounds.right + bgPadding,
                        d.bounds.bottom + bgPadding,
                        100f,
                        100f,
                        infoBgPaint
                    )

                    d.draw(canvas)

                    canvas.drawText(
                        durationText,
                        d.bounds.left - 15f - textSize.width,
                        d.bounds.bottom.toFloat() - fontMetricsInt.descent,
                        durationTextPaint
                    )
                }
            }
        } else {
        }
    }

    /**
     * 画在左下角
     **/
    private fun drawLocation(sourceUIInfo: SourceUIInfo, canvas: Canvas) {
        if (sourceUIInfo.location != null) {
            val padding = context.resources.getDimensionPixelSize(com.xihh.base.R.dimen.dp_3)
            val bgPadding = context.resources.getDimension(com.xihh.base.R.dimen.dp_2)
            val cHeight = canvas.height
            locationDrawable?.let { d ->
                val size = context.resources.getDimensionPixelSize(com.xihh.base.R.dimen.dp_18)
                d.setBounds(
                    padding,
                    cHeight - padding - size,
                    padding + size,
                    cHeight - padding
                )
                canvas.drawRoundRect(
                    d.bounds.left - bgPadding,
                    d.bounds.top - bgPadding,
                    d.bounds.right + bgPadding,
                    d.bounds.bottom + bgPadding,
                    100f,
                    100f,
                    infoBgPaint
                )
                d.draw(canvas)
            }
        }
    }

    companion object {
        private val gifDrawable by lazy {
            appContext.drawableRes(com.xallery.common.R.drawable.ic_gif)
        }

        private val videoDrawable by lazy {
            appContext.drawableRes(com.xallery.common.R.drawable.ic_play_white_stroke)
        }

        private val locationDrawable by lazy {
            appContext.drawableRes(com.xallery.common.R.drawable.ic_location)
        }
    }
}