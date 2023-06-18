package com.xallery.common.ui

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.os.Bundle
import com.xallery.common.databinding.DialogLottieBinding
import com.xihh.base.android.BaseDialog

class LottieDialog(context: Context) : BaseDialog<DialogLottieBinding>(context) {

    private var loop: Boolean = false

    override fun getViewBinding() = DialogLottieBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        vb.root.addAnimatorListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!loop) {
                    dismiss()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
    }

    fun show(
        lottieAssetsName: String,
        autoDismiss: Boolean = true,
        bgTransparent: Boolean = false,
        scale: Float = 1f,
        cancelable: Boolean = false,
    ) {
        loop = !autoDismiss

        vb.root.setAnimation(lottieAssetsName)
        vb.root.loop(!autoDismiss)
        vb.root.scaleX = scale
        vb.root.scaleY = scale
        if (bgTransparent) {
            window?.setDimAmount(0f)
        } else {
            window?.setDimAmount(0.5f)
        }
        setCanceledOnTouchOutside(cancelable)
        setCancelable(cancelable)

        super.show()
        vb.root.playAnimation()
    }

    override fun dismiss() {
        vb.root.clearAnimation()
        super.dismiss()
        window?.setDimAmount(0.5f)
    }
}