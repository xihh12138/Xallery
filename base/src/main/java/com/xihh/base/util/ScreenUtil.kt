package com.xihh.base.util

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object ScreenUtil {

    fun getScreenWidth(context: Context): Int {
        val windowManager =
            ContextCompat.getSystemService(context, WindowManager::class.java) ?: return 0
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.let {
                logx { "ScreenUtil: getScreenWidth=${it.right - it.left}" }
                it.right - it.left
            }
        } else {
            val outMetrics = DisplayMetrics()
            windowManager.getDefaultDisplay().getMetrics(outMetrics)
            logx { "ScreenUtil: getScreenWidth=${outMetrics.widthPixels}" }
            outMetrics.widthPixels
        }
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager =
            ContextCompat.getSystemService(context, WindowManager::class.java) ?: return 0
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.let {
                logx { "ScreenUtil: getScreenHeight=${it.bottom - it.top}" }
                it.bottom - it.top
            }
        } else {
            val outMetrics = DisplayMetrics()
            windowManager.getDefaultDisplay().getMetrics(outMetrics)
            logx { "ScreenUtil: getScreenHeight=${outMetrics.heightPixels}" }
            outMetrics.heightPixels
        }
    }

    fun getAbsStatusAndNavHeight(rootView: View): Pair<Int, Int> {
        val inset = ViewCompat.getRootWindowInsets(rootView) ?: return 0 to 0
        return inset.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()).let {
            Math.abs(it.bottom - it.top)
        } to inset.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()).let {
            Math.abs(it.bottom - it.top)
        }
    }

    fun getStatusBarHeight(rootView: View): Int {
        return ViewCompat.getRootWindowInsets(rootView)
            ?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars())?.let {
                it.bottom - it.top
            } ?: 0
    }

    fun getAbsStatusBarHeight(rootView: View): Int {
        return Math.abs(getStatusBarHeight(rootView))
    }

    fun getNavigationBarHeight(rootView: View): Int {
        return ViewCompat.getRootWindowInsets(rootView)
            ?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars())?.let {
                it.bottom - it.top
            } ?: 0
    }
}