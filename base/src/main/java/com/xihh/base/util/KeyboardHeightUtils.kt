package com.xihh.base.util

import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

/**
 * 做了生命周期关联，防止内存泄漏
 * 使用方法：
 * 1、new
 * 2、注册监听
 * 3、start
 **/
class KeyboardHeightUtils(activity: ComponentActivity) : PopupWindow(activity) {

    private var hasNavBar: Boolean? = null
    private var screenRect: Rect? = null

    private var listener: ((height: Int) -> Unit)? = null

    private val popupView = View(activity)

    // ---------- 因为拿的是activity的View引用，为了防止内存泄漏还是用弱引用吧 ----------
    private val activityView = WeakReference<View>(activity.findViewById(android.R.id.content))

    private val popupLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        handleOnGlobalLayout()
    }

    private var isStart = false

    private val activityViewAttachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {
            val activityView = activityView.get() ?: return
            if (isStart && !isShowing && activityView.windowToken != null) {
                setBackgroundDrawable(ColorDrawable(0))
                showAtLocation(activityView, Gravity.NO_GRAVITY, 0, 0)
            }
        }

        override fun onViewDetachedFromWindow(view: View) {
            logx { "KeyboardHeightUtils: onViewDetachedFromWindow   " }
        }
    }

    init {
        setContentView(popupView)
        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT

        softInputMode =
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = INPUT_METHOD_NEEDED

        popupView.viewTreeObserver.addOnGlobalLayoutListener(popupLayoutListener)
        activityView.get()?.addOnAttachStateChangeListener(activityViewAttachListener)
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                close()
            }
        })
    }

    fun start(): KeyboardHeightUtils {
        isStart = true
        val activityView = activityView.get() ?: return this
        if (!isShowing && activityView.windowToken != null) {
            setBackgroundDrawable(ColorDrawable(0))
            showAtLocation(activityView, Gravity.NO_GRAVITY, 0, 0)
        }
        return this
    }

    fun close() {
        activityView.clear()
        popupView.viewTreeObserver.removeOnGlobalLayoutListener(popupLayoutListener)
        listener = null
        dismiss()
    }

    fun registerKeyboardHeightListener(listener: (height: Int) -> Unit): KeyboardHeightUtils {
        this.listener = listener
        return this
    }

    private fun handleOnGlobalLayout() {
        val parentView = activityView.get() ?: return
        // ---------- 这个方法会获取到PopupWindow能显示的最大边界，一般就是除了导航栏和状态栏以外屏幕的其他区域 ----------
        val rect = Rect()
        popupView.getWindowVisibleDisplayFrame(rect)

        val navHeight = parentView.let { ScreenUtil.getNavigationBarHeight(it) }
        if (hasNavBar == null) {
            // ---------- 记录首次是否有导航栏 ----------
            hasNavBar = navHeight > 0
        }
        if (screenRect == null || screenRect!!.bottom < rect.bottom) {
            // ---------- 保存屏幕的最大宽高 ----------
            screenRect = rect
        }
        val keyboardHeight =
            if ((navHeight > 0 && !hasNavBar!!) || (navHeight <= 0 && hasNavBar!!)) {
                // ---------- 如果当前导航栏高度和之前记录的高度不一样，说明用户改了系统导航方式，更新记录的屏幕高度并更新是否有导航栏 ----------
                logx { "用户修改了系统导航方式" }
                screenRect = rect
                hasNavBar = navHeight > 0
                // ---------- 发现更改之后第一次默认当作没有键盘返回了 ----------
                0
            } else {
                screenRect!!.bottom - rect.bottom
            }
        notifyKeyboardHeightChanged(keyboardHeight)
    }

    private fun notifyKeyboardHeightChanged(height: Int) {
        listener?.invoke(height)
    }
}