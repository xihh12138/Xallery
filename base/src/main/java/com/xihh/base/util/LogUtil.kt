package com.xihh.base.util

import android.util.Log
import com.xihh.base.BuildConfig

object LogUtil {

}

fun logx(block: () -> String) {
    if (BuildConfig.LOGGABLE) {
        Log.d("xihh", block())
    }
}

fun logf(block: () -> String) {
    if (BuildConfig.LOGGABLE) {
        Log.wtf("xihh", block())
    }
}

fun logD(block: () -> Pair<String, String>) {
    if (BuildConfig.LOGGABLE) {
        block().let { Log.d(it.first, it.second) }
    }
}

suspend fun logxs(block: suspend () -> String) {
    if (BuildConfig.LOGGABLE) {
        Log.d("xihh", block())
    }
}