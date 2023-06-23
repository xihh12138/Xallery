package com.xihh.base.util

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.xihh.base.android.appContext

fun Application.drawableRes(@DrawableRes id: Int): Drawable? =
    ContextCompat.getDrawable(appContext, id)

@ColorInt
fun Application.colorRes(@ColorRes id: Int): Int =
    ContextCompat.getColor(appContext, id)

fun Application.dimensionRes(@DimenRes id: Int): Float =
    appContext.resources.getDimension(id)

fun Application.dimensionPixelOffsetRes(@DimenRes id: Int): Int =
    appContext.resources.getDimensionPixelOffset(id)