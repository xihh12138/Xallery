package com.xallery.picture

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.xallery.common.repository.db.model.Source
import com.xihh.base.android.appContext
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

open class SourceBroadcaster(private val context: Context) : BroadcastReceiver(), Closeable,
    DefaultLifecycleObserver {

    private val hasRegistered = AtomicBoolean(false)

    fun register(lifecycle: Lifecycle? = null): SourceBroadcaster {
        if (hasRegistered.get()) {
            return this
        }
        hasRegistered.set(true)
        context.registerReceiver(this, IntentFilter(ACTION))
        lifecycle?.addObserver(this)
        return this
    }

    fun register(viewModel: ViewModel): SourceBroadcaster {
        if (hasRegistered.get()) {
            return this
        }
        hasRegistered.set(true)
        context.registerReceiver(this, IntentFilter(ACTION))
        viewModel.addCloseable(this)
        return this
    }

    fun updateSource(source: Source, position: Int) {
        appContext.sendBroadcast(
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

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        close()
    }

    override fun close() {
        context.unregisterReceiver(this)
    }

    companion object {
        const val ACTION = "com.xallery.picture.SourceBroadcaster"

        const val EXTRA_SOURCE = "EXTRA_SOURCE"
        const val EXTRA_POSITION = "EXTRA_POSITION"
    }
}