package com.xallery.common.reposity

import com.xallery.common.R
import com.xallery.common.reposity.constant.Constant
import com.xihh.base.android.appContext
import com.xihh.base.android.createStore


val config by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Config()
}

class Config internal constructor() {

    private val sp = createStore(appContext, "config")

    var hasAccessStorage: Boolean
        get() = sp.getBoolean(Constant.SPKey.HAS_ACCESS_STORAGE, false)
        set(value) = sp.edit().putBoolean(Constant.SPKey.HAS_ACCESS_STORAGE, value).async()

    var columnNum: Int
        get() = sp.getInt(Constant.SPKey.COLUMN_COUNT, appContext.resources.getInteger(R.integer.album_column_count))
        set(value) = sp.edit().putInt(Constant.SPKey.COLUMN_COUNT, value).async()

}