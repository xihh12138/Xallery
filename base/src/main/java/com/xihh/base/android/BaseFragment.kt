package com.xihh.base.android

import android.content.Context
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
import com.xihh.base.util.logx
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VB : ViewBinding, VM : ViewModel> : Fragment() {

    protected val vm: VM by lazy { getViewModel() }

    protected val vb: VB by lazy { getViewBinding() }

    @CallSuper
    open fun adaptWindowInsets(insets: Rect) {
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
        savedInstanceState: Bundle?,
    ): View {
        logx { "${this.javaClass.simpleName}: onCreateView   savedInstanceState is ${if (savedInstanceState == null) "" else "not"} null" }
        return vb.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logx { "${this.javaClass.simpleName}: onViewCreated   savedInstanceState is ${if (savedInstanceState == null) "" else "not"} null" }
        initView(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        logx { "${this.javaClass.simpleName}: onAttach   " }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        logx { "${this.javaClass.simpleName}: onViewStateRestored   " }
    }

    override fun onStart() {
        super.onStart()
        logx { "${this.javaClass.simpleName}: onStart   " }
    }

    override fun onResume() {
        super.onResume()
        logx { "${this.javaClass.simpleName}: onResume   " }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        logx { "${this.javaClass.simpleName}: onHiddenChanged   hidden=$hidden" }
    }

    override fun onPause() {
        super.onPause()
        logx { "${this.javaClass.simpleName}: onPause   " }
    }

    override fun onStop() {
        super.onStop()
        logx { "${this.javaClass.simpleName}: onStop   " }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        logx { "${this.javaClass.simpleName}: onSaveInstanceState   " }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logx { "${this.javaClass.simpleName}: onDestroyView   " }
    }

    override fun onDestroy() {
        super.onDestroy()
        logx { "${this.javaClass.simpleName}: onDestroy   " }
    }

    override fun onDetach() {
        super.onDetach()
        logx { "${this.javaClass.simpleName}: onDetach   " }
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
        throw RuntimeException("Can't inflate $type")
    }
}