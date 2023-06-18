package com.xallery.common.ui

import android.content.Context

interface LottieHost {

    fun Context.showLottie(
        assetsName: String,
        autoDismiss: Boolean,
        bgTransparent: Boolean = false,
        scale: Float = 1f,
        cancelable: Boolean = false,
    )

    fun Context.hideLottie()
}

class LottieHostImpl : LottieHost {
    private var view: LottieDialog? = null

    override fun Context.showLottie(
        assetsName: String,
        autoDismiss: Boolean,
        bgTransparent: Boolean,
        scale: Float,
        cancelable: Boolean,
    ) {
        if (view == null) {
            view = LottieDialog(this)
        }
        view?.show(assetsName, autoDismiss, bgTransparent, scale, cancelable)
    }

    override fun Context.hideLottie() {
        view?.dismiss()
    }
}