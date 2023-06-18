package com.xallery.common.util

import android.content.res.Resources.NotFoundException
import android.view.Gravity
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.hjq.toast.style.LocationToastStyle
import com.xihh.base.android.appContext

fun toast(
    id: Int,
    gravity: Int? = null,
    offsetX: Int? = null,
    offsetY: Int? = null,
) {
    toast(
        try {
            // 如果这是一个资源 id
            appContext.resources.getText(id)
        } catch (ignored: NotFoundException) {
            // 如果这是一个 int 整数
            id.toString()
        },
        gravity,
        offsetX,
        offsetY,
    )
}

fun toast(
    `object`: Any?,
    gravity: Int? = null,
    offsetX: Int? = null,
    offsetY: Int? = null,
) {
    toast(`object`?.toString() ?: return, gravity, offsetX, offsetY)
}

fun toast(
    charSequence: CharSequence,
    gravity: Int? = null,
    offsetX: Int? = null,
    offsetY: Int? = null,
) {
    if (gravity == null && offsetX == null && offsetY == null) {
        Toaster.show(charSequence)
    } else {
        Toaster.show(ToastParams().apply {
            text = charSequence
            style = LocationToastStyle(
                Toaster.getStyle(),
                Gravity.BOTTOM,
                offsetX ?: 0,
                offsetY ?: 0,
                0f,
                0f
            )
        })
    }
}
