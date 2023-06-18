package com.xihh.base.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import java.lang.reflect.Method


object ProcessUtil {
    fun getCurrentProcessName(context: Context): String? {
        var currentProcessName: String? = null
        currentProcessName = currentProcessNameByApplication
        if (!currentProcessName.isNullOrEmpty()) {
            return currentProcessName
        }
        currentProcessName = currentProcessNameByActivityThread
        if (!currentProcessName.isNullOrEmpty()) {
            return currentProcessName
        }
        currentProcessName = getCurrentProcessNameByActivityManager(context)

        return currentProcessName
    }

    private val currentProcessNameByApplication: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else null

    private val currentProcessNameByActivityThread: String?
        @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
        get() {
            var processName: String? = null
            try {
                val declaredMethod: Method = Class.forName(
                    "android.app.ActivityThread", false,
                    Application::class.java.classLoader
                ).getDeclaredMethod("currentProcessName")
                declaredMethod.isAccessible = true
                val invoke: Any = declaredMethod.invoke(null)
                if (invoke is String) {
                    processName = invoke
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return processName
        }

    private fun getCurrentProcessNameByActivityManager(context: Context): String? {
        val pid = Process.myPid()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        if (am != null) {
            val runningAppList = am.runningAppProcesses
            if (runningAppList != null) {
                for (processInfo in runningAppList) {
                    if (processInfo.pid == pid) {
                        return processInfo.processName
                    }
                }
            }
        }
        return null
    }

    fun isMainProcess(context: Context): Boolean {
        val processName = getCurrentProcessName(context)
        return context.packageName == processName
    }
}
