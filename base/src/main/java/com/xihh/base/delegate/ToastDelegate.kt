package com.xihh.base.delegate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

interface IToast {
    fun showToast(str: String)
    val toastFlow: Flow<String>
}

open class ToastDelegate(private val scope: CoroutineScope = MainScope()) : IToast {

    private val toast = MutableSharedFlow<String>()

    override fun showToast(str: String) {
        scope.launch { toast.emit(str) }
    }

    override val toastFlow: Flow<String>
        get() = toast

}