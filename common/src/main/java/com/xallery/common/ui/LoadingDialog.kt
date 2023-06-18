package com.xallery.common.ui

import android.content.Context
import android.os.Bundle
import com.xallery.common.databinding.DialogLoadingBinding
import com.xihh.base.android.BaseDialog
import com.xihh.base.ui.LoadingDrawable

class LoadingDialog(context: Context) : BaseDialog<DialogLoadingBinding>(context) {

    private val drawable by lazy { LoadingDrawable(context) }

    override fun getViewBinding() = DialogLoadingBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        applyWindowSetting()
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        vb.root.setImageDrawable(drawable)
    }

    fun showCancelable() {
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        window?.setDimAmount(0f)
        show()
    }

    override fun show() {
        super.show()
        drawable.start()
    }

    override fun dismiss() {
        super.dismiss()
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window?.setDimAmount(0.5f)
        drawable.stop()
    }
}