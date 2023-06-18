package com.xihh.base.android

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.xihh.base.R

abstract class BaseDialog<VB : ViewBinding>(context: Context) :
    Dialog(context, R.style.Theme_NoTitle_Dialog), LifecycleEventObserver {

    protected val vb: VB by lazy { getViewBinding() }

    abstract fun getViewBinding(): VB

    abstract fun initView(savedInstanceState: Bundle?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindLifecycle()
        setContentView(vb.root)
        this.window?.apply {
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        }
        initView(savedInstanceState)
    }

    protected fun applyWindowSetting(
        width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        gravity: Int = Gravity.CENTER,
    ) {
        this.window?.apply {
            setGravity(gravity)
            attributes.width = width
            attributes.height = height
        }
    }

    private fun bindLifecycle() {
        val context = if (context is ContextWrapper) {
            (context as ContextWrapper).baseContext
        } else {
            context
        }
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(this)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            if (isShowing) dismiss()
        }
    }

    override fun dismiss() {
        val context = if (context is ContextWrapper) {
            (context as ContextWrapper).baseContext
        } else {
            context
        }
        if (context is LifecycleOwner) {
            context.lifecycle.removeObserver(this)
        }
        super.dismiss()
    }

    protected open fun stringRes(@StringRes id: Int) = context.getString(id)

    protected open fun colorRes(@ColorRes id: Int) = ContextCompat.getColor(context, id)

    protected open fun drawableRes(@DrawableRes id: Int) = ContextCompat.getDrawable(context, id)

    protected open fun dimensionRes(@DimenRes id: Int) = context.resources.getDimension(id)

    protected open fun dimensionPixelOffsetRes(@DimenRes id: Int) =
        context.resources.getDimensionPixelOffset(id)
}