package com.xihh.base.util

import androidx.annotation.CallSuper

abstract class IntentInterceptChain(
    private val interceptors: MutableList<Interceptor>
) {
    private var index = 0

    fun process() {
        when {
            index < interceptors.size -> {
                interceptors[index++].intercept(this)
            }
            index == interceptors.size -> {
                clear()
            }
        }
    }

    private fun clear() {
        interceptors.clear()
    }

}

interface Interceptor {
    fun intercept(chain: IntentInterceptChain)
}

abstract class BaseInterceptor() : Interceptor {

    protected var chain: IntentInterceptChain? = null

    @CallSuper
    override fun intercept(chain: IntentInterceptChain) {
        this.chain = chain
    }
}