package com.xihh.base.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.lang.reflect.ParameterizedType

abstract class BaseSheetDialogFragment<VB : ViewBinding> : BottomSheetDialogFragment() {

    protected val vb: VB by lazy { getViewBinding() }

    abstract fun initView(savedInstanceState: Bundle?)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? = vb.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setBackgroundDrawable(null)
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        }
        initView(savedInstanceState)
    }

    protected fun stringRes(@StringRes id: Int) = getString(id)

    protected fun colorRes(@ColorRes id: Int) = ContextCompat.getColor(requireContext(), id)

    protected fun drawableRes(@DrawableRes id: Int) =
        ContextCompat.getDrawable(requireContext(), id)

    protected fun dimensionRes(@DimenRes id: Int) = resources.getDimension(id)

    protected fun dimensionPixelOffsetRes(@DimenRes id: Int) = resources.getDimensionPixelOffset(id)

    open fun getViewBinding(): VB {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val clazz = type.actualTypeArguments[0] as Class<VB>
            val method = clazz.getMethod("inflate", LayoutInflater::class.java)
            return method.invoke(null, layoutInflater) as VB
        }
        throw RuntimeException()
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, this::class.simpleName)
    }
}