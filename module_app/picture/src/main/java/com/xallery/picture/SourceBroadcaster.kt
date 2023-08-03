package com.xallery.picture

import android.content.Context
import android.content.Intent
import com.xallery.common.repository.db.model.Source
import com.xihh.base.android.BaseBroadcaster

open class SourceBroadcaster(context: Context) : BaseBroadcaster(context, ACTION) {

    fun updateSource(source: Source, position: Int) {
        sendBroadcast(
            Intent(ACTION).putExtra(EXTRA_SOURCE, source).putExtra(EXTRA_POSITION, position)
        )
    }

    open fun onSourceChange(source: Source, position: Int) {

    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action != ACTION) return

        onSourceChange(
            intent.getParcelableExtra(EXTRA_SOURCE)!!,
            intent.getIntExtra(EXTRA_POSITION, 0)
        )
    }

    companion object {
        const val ACTION = "com.xallery.picture.SourceBroadcaster"

        const val EXTRA_SOURCE = "EXTRA_SOURCE"
        const val EXTRA_POSITION = "EXTRA_POSITION"
    }
}