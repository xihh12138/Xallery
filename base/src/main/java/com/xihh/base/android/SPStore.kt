package com.xihh.base.android

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils

/**
 * 创建本地持久化保存工具
 */
fun createStore(
    context: Context = appContext,
    name: String,
    mode: SPStoreMode = SPStoreMode.PRIVATE
): SPStore = SPStore(context, name, mode)

enum class SPStoreMode {
    PRIVATE, PUBLIC
}


class SPStore(context: Context, name: String, mode: SPStoreMode) {

    private val mSharedPreferences =
        context.getSharedPreferences(name, getSharedPreferencesMode(mode))

    private fun getSharedPreferencesMode(mode: SPStoreMode) = when (mode) {
        SPStoreMode.PUBLIC -> Context.MODE_WORLD_WRITEABLE
        else -> Context.MODE_PRIVATE
    }


    fun edit() = KEditor(mSharedPreferences)

    fun getInt(key: String, defaultValue: Int? = null): Int =
        mSharedPreferences.getInt(key, defaultValue ?: 0)


    fun getString(key: String, defaultValue: String? = null): String? {
        val value = mSharedPreferences.getString(key, defaultValue)
        return if (value.isNullOrEmpty()) {
            defaultValue
        } else value
    }


    fun getLong(key: String, defaultValue: Long? = null): Long =
        mSharedPreferences.getLong(key, defaultValue ?: 0L)

    fun getBoolean(key: String, defaultValue: Boolean? = null): Boolean =
        mSharedPreferences.getBoolean(key, defaultValue ?: false)


    fun getFloat(key: String, defaultValue: Float? = null): Float =
        mSharedPreferences.getFloat(key, defaultValue ?: 0F)


    class KEditor(mSharedPreferences: SharedPreferences) {
        private val editor = mSharedPreferences.edit()
        fun clear(): KEditor {
            editor.clear()
            return this
        }

        fun putLong(key: String, value: Long): KEditor {
            editor.putLong(key, value)
            return this
        }

        fun putInt(key: String, value: Int): KEditor {
            editor.putInt(key, value)
            return this
        }

        fun remove(key: String): KEditor {
            editor.remove(key)
            return this
        }

        fun putString(key: String, value: String?): KEditor {
            editor.putString(key, value)
            return this
        }

        fun putBoolean(key: String, value: Boolean): KEditor {
            editor.putBoolean(key, value)
            return this
        }

        fun putFloat(key: String, value: Float): KEditor {
            editor.putFloat(key, value)
            return this
        }

        fun putStringSet(key: String, value: Set<String>): KEditor {
            editor.putStringSet(key, value)
            return this
        }

        /**
         * 同步提交
         */
        fun sync(): Boolean = editor.commit()

        /**
         * 异步
         */
        fun async() = editor.apply()

    }

}

