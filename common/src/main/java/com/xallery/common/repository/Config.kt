package com.xallery.common.repository

import com.xallery.common.R
import com.xallery.common.repository.constant.Constant
import com.xihh.base.android.appContext
import com.xihh.base.android.createStore


val config by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Config()
}

class Config internal constructor() {

    private val sp = createStore(appContext, "config")

    var hasAccessStorage: Boolean
        get() = sp.getBoolean(Constant.SPKey.HAS_ACCESS_STORAGE, false)
        set(value) {
            sp.edit().putBoolean(Constant.SPKey.HAS_ACCESS_STORAGE, value).sync()
        }

    var columnNum: Int
        get() = sp.getInt(
            Constant.SPKey.COLUMN_COUNT,
            appContext.resources.getInteger(R.integer.album_column_count)
        )
        set(value) {
            sp.edit().putInt(Constant.SPKey.COLUMN_COUNT, value).sync()
        }

    var sortColumn: String
        get() = sp.getString(Constant.SPKey.SORT_COLUMN, "id")!!
        set(value) {
            sp.edit().putString(Constant.SPKey.SORT_COLUMN, value).sync()
        }

    var isSortDesc: Boolean
        get() = sp.getBoolean(Constant.SPKey.IS_SORT_DESC, true)
        set(value) {
            sp.edit().putBoolean(Constant.SPKey.IS_SORT_DESC, value).sync()
        }

}