package com.xallery.common.util

import android.content.Context
import android.content.Intent
import com.xihh.base.android.BaseBroadcaster

open class SourceDBReadyBroadcaster(context: Context) : BaseBroadcaster(context, ACTION) {

    fun notifySourceDBReady(oldCount: Int, newCount: Int) {
        sendBroadcast(
            Intent(ACTION).putExtra(EXTRA_OLD_COUNT, oldCount).putExtra(EXTRA_NEW_COUNT, newCount)
        )
    }

    open fun onSourceDBReady(oldCount: Int, newCount: Int) {

    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action != ACTION) return

        onSourceDBReady(
            intent.getIntExtra(EXTRA_OLD_COUNT, 0),
            intent.getIntExtra(EXTRA_NEW_COUNT, 0)
        )
    }

    companion object {
        const val ACTION = "com.xallery.common.util.SourceReadyBroadcaster"

        const val EXTRA_OLD_COUNT = "EXTRA_OLD_COUNT"
        const val EXTRA_NEW_COUNT = "EXTRA_NEW_COUNT"
    }
}