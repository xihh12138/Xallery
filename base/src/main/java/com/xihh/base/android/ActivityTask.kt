package com.xihh.base.android

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.xihh.base.util.logx
import java.util.concurrent.ConcurrentLinkedDeque

object ActivityTask : ActivityLifecycleCallbacks {

    private val frontActivityList = ConcurrentLinkedDeque<Activity>()
    private val visibleActivityList = ConcurrentLinkedDeque<Activity>()
    private val activityList = ConcurrentLinkedDeque<Activity>()

    private val appBackgroundListeners = ConcurrentLinkedDeque<AppBackgroundListener>()

    val isAppInFront: Boolean
        get() = frontActivityList.isNotEmpty()

    val topActivity: Activity?
        get() {
            var mActivity: Activity? = null
            if (activityList.isNotEmpty()) {
                mActivity = activityList.last
            }
            return mActivity
        }

    fun addAppBackgroundListener(listener: AppBackgroundListener) {
        appBackgroundListeners.add(listener)
    }

    fun removeAppBackgroundListener(listener: AppBackgroundListener) {
        appBackgroundListeners.remove(listener)
    }

    /**
     * 判断某个页面是否在前台
     *
     * @return
     */
    fun isActivityFront(activity: Activity): Boolean {
        return frontActivityList.contains(activity)
    }

    /**
     * 判断某个页面是否在前台
     *
     * @return
     */
    fun isActivityFront(clazz: Class<out Activity?>): Boolean {
        for (activity in frontActivityList) {
            if (activity.javaClass == clazz) {
                return true
            }
        }
        return false
    }

    /**
     * 判断某个页面是否存活
     *
     * @return
     */
    fun isActivityExist(activity: Activity): Boolean {
        return activityList.contains(activity)
    }

    fun isActivityExist(clazz: Class<out Activity?>): Boolean {
        for (activity in activityList) {
            if (activity.javaClass == clazz) {
                return true
            }
        }
        return false
    }

    fun getActivity(clazz: Class<out Activity?>): Activity? {
        for (activity in activityList) {
            if (activity.javaClass == clazz) {
                return activity
            }
        }
        return null
    }

    fun getFrontActivity(): Activity? {
        return frontActivityList.last
    }

    fun exit() {
        var size = activityList.size
        while (size >= 0) {
            activityList.pollLast()?.finish()
            size--
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityList.add(activity)
        logx { "ActivityTask: onActivityCreated   $activityList" }
    }

    override fun onActivityStarted(activity: Activity) {
        visibleActivityList.add(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        frontActivityList.add(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        frontActivityList.remove(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        visibleActivityList.remove(activity)
        if (visibleActivityList.isEmpty()) {
            appBackgroundListeners.forEach {
                it.onBackground()
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activityList.remove(activity)
        logx { "ActivityTask: onActivityDestroyed   $activityList" }
    }
}

interface AppBackgroundListener {
    fun onBackground()
}