package com.xallery.common.ui.view

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * 版权：中国麦风科技有限公司
 * @author Hem
 * @version 1.0.0
 * @创建日期 2023/5/9,17:41
 * @修改日期 2023/5/9
 * @description ---
 */
class SliderBar(context: Context, attrs: AttributeSet? = null): View(context, attrs) {

    private val inactiveTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val activeTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)

}