package com.xihh.base.android

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

/**
 * 必须要在Activity的初始化代码里实例化
 **/
class SuspendActivityResultContract<I, O> private constructor(
    componentActivity: ComponentActivity, contract: ActivityResultContract<I, O>,
) {

    private val activityResultFlow = MutableSharedFlow<O>(onBufferOverflow = BufferOverflow.DROP_LATEST, extraBufferCapacity = 1)

    private val requestLauncher = componentActivity.registerForActivityResult(contract) {
        activityResultFlow.tryEmit(it)
    }

    suspend fun get(input: I): O {
        requestLauncher.launch(input)
        return activityResultFlow.first()
    }

    companion object {
        fun <I, O> ComponentActivity.registerForActivityResult(contract: ActivityResultContract<I, O>): SuspendActivityResultContract<I, O> {
            return SuspendActivityResultContract(this, contract)
        }
    }
}

/**
 * 必须要在Fragment的初始化代码里实例化
 **/
class SuspendFragmentResultContract<I, O> private constructor(
    fragment: Fragment, contract: ActivityResultContract<I, O>,
) {

    private val activityResultFlow = MutableSharedFlow<O>(onBufferOverflow = BufferOverflow.DROP_LATEST, extraBufferCapacity = 1)

    private val requestLauncher = fragment.registerForActivityResult(contract) {
        activityResultFlow.tryEmit(it)
    }

    suspend fun get(input: I): O {
        requestLauncher.launch(input)
        return activityResultFlow.first()
    }

    companion object {
        fun <I, O> Fragment.registerForActivityResult(contract: ActivityResultContract<I, O>): SuspendFragmentResultContract<I, O> {
            return SuspendFragmentResultContract(this, contract)
        }
    }
}
