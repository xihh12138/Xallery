package com.xihh.base.delegate

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface ILoading {
    fun showLoading()
    fun hideLoading()

    val loadingFlow: Flow<Boolean>
}

open class LoadingDelegate : ILoading {

    private val loading = MutableStateFlow(false)

    override fun showLoading() {
        loading.update { true }
    }

    override fun hideLoading() {
        loading.update { false }
    }

    override val loadingFlow: Flow<Boolean>
        get() = loading

}