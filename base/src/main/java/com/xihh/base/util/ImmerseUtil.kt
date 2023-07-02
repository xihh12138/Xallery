package com.xihh.base.util

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager

object ImmerseUtil {


    /**
     * 改变状态栏图标/文字颜色
     * @param isLight 如果状态栏背景是暗色的就传true，是亮色的就传false
     *
     * 只兼容到23
     **/
    fun applyStatusBarIsLight(window: Window, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isLight) {
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                window.insetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isLight) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility and (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv())
            }
        }
    }

    /**
     * 改变导航栏标/文字颜色
     * @param isLight 如果导航栏背景是暗色的就传true，是亮色的就传false
     *
     * 只兼容到23
     **/
    fun applyNavBarIsLight(window: Window, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = if (isLight) { //白色按钮
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else { //黑色按钮
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            window.decorView.systemUiVisibility = systemUiVisibility
        }
    }

    fun transparentStatusBar(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility =
            systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = systemUiVisibility
        window.statusBarColor = Color.TRANSPARENT
    }

    fun transparentNavigationBar(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility =
            systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.decorView.systemUiVisibility = systemUiVisibility
        window.navigationBarColor = Color.TRANSPARENT
    }

    fun setStatusBarVisible(window: Window, isVisible: Boolean) {
        var systemUiVisibility = window.decorView.systemUiVisibility
        if (isVisible) {
//            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
//            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_IMMERSIVE.inv()
//            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_STABLE.inv()
//            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.inv()
            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
//            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LOW_PROFILE.inv()

//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
//            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_IMMERSIVE
//            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_FULLSCREEN
//            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LOW_PROFILE

//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LOW_PROFILE or
//                    View.SYSTEM_UI_FLAG_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_IMMERSIVE
        }
        window.decorView.systemUiVisibility = systemUiVisibility
    }
}