package com.xihh.base.delegate

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface ILottie {

    suspend fun showLottie(assetsName: String)
    suspend fun hideLottie()

    val lottieFlow: Flow<String?>
}

open class LottieDelegate : ILottie {

    private val lottie = MutableSharedFlow<String?>()

    override suspend fun showLottie(assetsName: String) {
        lottie.emit(assetsName)
    }

    override suspend fun hideLottie() {
        lottie.emit(null)
    }

    override val lottieFlow: Flow<String?>
        get() = lottie

}