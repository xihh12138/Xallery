package com.xihh.base.android

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VB : ViewBinding, VM : ViewModel> : Fragment() {

    protected val vm: VM by lazy { getViewModel() }

    protected val vb: VB by lazy { getViewBinding() }

    @CallSuper
    open fun adaptWindowInsets(insets: Rect){
        childFragmentManager.fragments.forEach {
            if (it is BaseFragment<*, *>) {
                it.adaptWindowInsets(insets)
            }
        }
    }

    abstract fun getViewModel(): VM

    abstract fun initView(savedInstanceState: Bundle?)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return vb.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView(savedInstanceState)
    }

    protected fun stringRes(@StringRes id: Int) = getString(id)

    protected fun colorRes(@ColorRes id: Int) = ContextCompat.getColor(requireContext(), id)

    protected fun drawableRes(@DrawableRes id: Int) = ContextCompat.getDrawable(requireContext(), id)

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
}