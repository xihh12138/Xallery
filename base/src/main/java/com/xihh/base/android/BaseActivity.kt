package com.xihh.base.android

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.xihh.base.util.ImmerseUtil
import com.xihh.base.util.ScreenUtil
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), OnGlobalLayoutListener {

    protected val vb: VB by lazy { getViewBinding() }

    @CallSuper
    open fun adaptWindowInsets(insets: Rect) {
        supportFragmentManager.fragments.forEach {
            if (it is BaseFragment<*, *>) {
                it.adaptWindowInsets(insets)
            }
        }
    }

    abstract fun initView(savedInstanceState: Bundle?)

    override fun onGlobalLayout() {
        val (statusHeight, navigationHeight) = ScreenUtil.getAbsStatusAndNavHeight(vb.root)
        val insets = Rect(0, statusHeight, 0, navigationHeight)
        adaptWindowInsets(insets)
    }

    open fun onPrepareAnimation() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        onPrepareAnimation()
        super.onCreate(savedInstanceState)
        transparentSystemBar(window)
        applySystemBarIsLight(true)
        setContentView(vb.root)
        initView(savedInstanceState)
        window.decorView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        window.decorView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    /**
     * 分发点击事件.点击外部键盘消失
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // 获取当前获得当前焦点所在View
            val view = currentFocus
            if (notClickEditText(view, event)) {
                // 如果不是edittext，则隐藏键盘
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view!!.windowToken, 0)
                whenEditLoseFocus(event)
            }
            if (notClickSelectedTextView(view, event)) {
                view?.clearFocus()
            }
            return super.dispatchTouchEvent(event)
        }
        /**
         * 看源码可知superDispatchTouchEvent 是个抽象方法，用于自定义的Window
         * 此处目的是为了继续将事件由dispatchTouchEvent (MotionEvent event) 传递到onTouchEvent
         * (MotionEvent event) 必不可少，否则所有组件都不能触发 onTouchEvent (MotionEvent event)
         */
        return if (window.superDispatchTouchEvent(event)) {
            true
        } else onTouchEvent(event)
    }

    /**
     * 对于所有的Activity都适用,所以定义在BaseActivity里面,被子类继承
     * 点击外部隐藏输入软键盘,获取到EditText的位置,做出点击判断
     */
    private fun notClickEditText(view: View?, event: MotionEvent): Boolean {
        if (view != null && view is EditText) {
            val leftTop = intArrayOf(0, 0)
            // 获取输入框当前的 location 位置
            view.getLocationInWindow(leftTop)
            val left = leftTop[0]
            val top = leftTop[1]
            // 此处根据输入框左上位置和宽高获得右下位置
            val bottom = top + view.getHeight()
            val right = left + view.getWidth()
            return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
        }
        return false
    }

    private fun notClickSelectedTextView(view: View?, event: MotionEvent): Boolean {
        if (view != null && view is TextView && view.hasSelection()) {
            val leftTop = intArrayOf(0, 0)
            // 获取输入框当前的 location 位置
            view.getLocationInWindow(leftTop)
            val left = leftTop[0]
            val top = leftTop[1]
            // 此处根据输入框左上位置和宽高获得右下位置
            val bottom = top + view.getHeight()
            val right = left + view.getWidth()
            return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
        }
        return false
    }

    fun transparentSystemBar(window: Window) {
        ImmerseUtil.transparentStatusBar(window)
        ImmerseUtil.transparentNavigationBar(window)
    }

    fun applySystemBarIsLight(statusIsLight: Boolean, navIsLight: Boolean = statusIsLight) {
        ImmerseUtil.applyStatusBarIsLight(window, statusIsLight)
        ImmerseUtil.applyNavBarIsLight(window, navIsLight)
    }

    protected fun stringRes(@StringRes id: Int) = getString(id)

    protected fun colorRes(@ColorRes id: Int) = ContextCompat.getColor(this, id)

    protected fun drawableRes(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

    protected fun dimensionRes(@DimenRes id: Int) = resources.getDimension(id)

    protected fun dimensionPixelOffsetRes(@DimenRes id: Int) = resources.getDimensionPixelOffset(id)

    protected fun start(clazz: KClass<out Activity>) {
        startActivity(Intent(this, clazz.java))
    }

    open fun whenEditLoseFocus(event: MotionEvent) {
    }

    open fun <T> getViewBinding(): T {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val clazz = type.actualTypeArguments[0] as Class<T>
            val method = clazz.getMethod("inflate", LayoutInflater::class.java)
            return method.invoke(null, layoutInflater) as T
        }
        throw RuntimeException()
    }
}