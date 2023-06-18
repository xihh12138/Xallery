package com.xihh.base.util

import java.io.Closeable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.timerTask

class Looper(val interval: Long = 1000, private val execute: () -> Unit):Closeable {

    private val state = AtomicInteger(STATE_PAUSE)

    private val timer = Timer("loop")

    private val task = AtomicReference<TimerTask>()

    fun start() {
        if (isFinished) {
            throw IllegalStateException()
        }
        if (state.compareAndSet(STATE_PAUSE, STATE_RUNNING)) {
            val newTask = timerTask {
                if (isRunning) {
                    execute()
                }
            }
            task.set(newTask)
            timer.scheduleAtFixedRate(newTask, 0L, interval)
        }
    }

    fun pause() {
        if (isFinished) {
            return
        }
        task.get()?.cancel()
        state.compareAndSet(STATE_RUNNING, STATE_PAUSE)
    }

    override fun close() {
        state.set(STATE_FINISH)
        timer.cancel()
    }

    val isFinished get() = state.get() == STATE_FINISH
    val isPause get() = state.get() == STATE_PAUSE
    val isRunning get() = state.get() == STATE_RUNNING

    companion object {
        const val STATE_FINISH = 0
        const val STATE_PAUSE = 1
        const val STATE_RUNNING = 2
    }
}