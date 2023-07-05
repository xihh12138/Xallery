package com.xallery.common.repository

import com.xihh.base.android.appContext
import com.xihh.base.android.createStore


val spStore by lazy {
    createStore(appContext, "common")
}