package com.xihh.base.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseBroadcaster(private val context: Context, private val intentAction: String) :
    BroadcastReceiver(), Closeable, DefaultLifecycleObserver {

    private val localBroadcastManager = LocalBroadcastManager.getInstance(context)

    private val hasRegistered = AtomicBoolean(false)

    fun register(lifecycle: Lifecycle? = null): BaseBroadcaster {
        if (hasRegistered.get()) {
            return this
        }
        hasRegistered.set(true)
        localBroadcastManager.registerReceiver(this, IntentFilter(intentAction))
        lifecycle?.addObserver(this)
        return this
    }

    fun register(viewModel: ViewModel): BaseBroadcaster {
        if (hasRegistered.get()) {
            return this
        }
        hasRegistered.set(true)
        localBroadcastManager.registerReceiver(this, IntentFilter(intentAction))
        viewModel.addCloseable(this)
        return this
    }

    fun sendBroadcast(intent: Intent) {
        localBroadcastManager.sendBroadcast(intent)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        close()
    }

    override fun close() {
        localBroadcastManager.unregisterReceiver(this)
    }
}