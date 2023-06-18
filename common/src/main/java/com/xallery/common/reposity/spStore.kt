package com.xallery.common.reposity

import com.xihh.base.android.appContext
import com.xihh.base.android.createStore


val spStore by lazy {
    createStore(appContext, "common")
}