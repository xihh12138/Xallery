package com.xallery.common.ui

import android.content.Context
import androidx.fragment.app.Fragment

interface LoadingHost {

    fun Context.showLoadingCancelable()

    fun Context.showLoading()

    fun Context.hideLoading()

    fun Fragment.showLoadingCancelable() {
        requireContext().showLoadingCancelable()
    }

    fun Fragment.showLoading() {
        requireContext().showLoading()
    }

    fun Fragment.hideLoading() {
        requireContext().hideLoading()
    }
}

class LoadingHostImpl : LoadingHost {
    private var view: LoadingDialog? = null

    override fun Context.showLoadingCancelable() {
        if (view == null) {
            view = LoadingDialog(this)
        }
        view?.showCancelable()
    }

    override fun Context.showLoading() {
        if (view == null) {
            view = LoadingDialog(this)
        }
        view?.show()
    }

    override fun Context.hideLoading() {
        view?.dismiss()
    }
}