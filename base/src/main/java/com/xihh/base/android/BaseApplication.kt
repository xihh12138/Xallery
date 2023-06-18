package com.xihh.base.android

import android.app.Application

abstract class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this

        registerActivityLifecycleCallbacks(ActivityTask)
    }

}

lateinit var appContext: Application