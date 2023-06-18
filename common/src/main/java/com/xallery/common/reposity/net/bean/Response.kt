package com.xallery.common.reposity.net.bean

import androidx.annotation.Keep


@Keep
data class Response<T> @JvmOverloads constructor(
    val status: Int = 200,
    val message: String = "",
    val data: T,
) {
    @Transient
    var originThrowable: Throwable? = null

    val isSuccess get() = message == MSG_SUCCESS

    fun onSuccess(block: (T) -> Unit) {
        if (isSuccess) {
            block(data)
        }
    }

    fun onError(block: (Int, String, T) -> Unit) {
        if (!isSuccess) {
            block(status, message, data)
        }
    }

    companion object {
        const val MSG_SUCCESS = "success"

        const val CODE_SUCCESS = 200
        const val CODE_EXCEPTION_EMPTY = -10
    }
}
