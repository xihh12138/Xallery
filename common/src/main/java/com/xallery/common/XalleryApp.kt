package com.xallery.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.hjq.toast.Toaster
import com.hjq.toast.style.BlackToastStyle
import com.xihh.base.android.ActivityTask
import com.xihh.base.android.AppBackgroundListener
import com.xihh.base.android.BaseApplication
import com.xihh.base.util.ProcessUtil
import com.xihh.base.util.logf
import com.xihh.base.util.logx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class XalleryApp : BaseApplication(), CoroutineScope by MainScope(), AppBackgroundListener {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()

        val isMainProcess = ProcessUtil.isMainProcess(this)
        logx { "XalleryApp: onCreate   isMainProcess=$isMainProcess" }
        if (isMainProcess) {
            ActivityTask.addAppBackgroundListener(this)
            // 初始化 Toast 框架
            Toaster.init(this, object : BlackToastStyle() {
                override fun getBackgroundDrawable(context: Context?): Drawable {
                    return GradientDrawable().apply {
                        setColor(ContextCompat.getColor(this@XalleryApp, R.color.fourthly))
                        cornerRadius = resources.getDimension(com.xihh.base.R.dimen.dp_16)
                    }
                }

                override fun getTextColor(context: Context?): Int {
                    return ContextCompat.getColor(this@XalleryApp, R.color.text_primary)
                }

                override fun getTranslationZ(context: Context?): Float {
                    return 1f
                }
            })

            Thread.setDefaultUncaughtExceptionHandler { t, e ->
                logf { "XalleryApp:exception:thread=$t e=${e.stackTraceToString()}" }
                throw e
            }
        }
    }

    override fun onBackground() {
    }
}