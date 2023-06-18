package com.xihh.base.model


data class StateData<T>(val data: T, var state: Int = REFRESH, var msg: String? = null) {

    init {
        // ---------- 如果消息为空那就不显示 ----------
        if (msg != null && msg!!.isBlank()) {
            msg = null
        }
    }

    fun isSuccess() = state == SUCCESS
    fun isSuccessMore() = state == SUCCESS_MORE
    fun isRefresh() = state == REFRESH
    fun isLoadMore() = state == LOAD_MORE
    fun isFailed() = state == FAILED
    fun isFailedMore() = state == FAILED_MORE
    fun isNoData() = state == NO_DATA
    fun isNoMoreData() = state == NO_MORE_DATA

    fun asSuccess(data: T = this.data, msg: String? = null): StateData<T> {
        return copy(data = data, state = SUCCESS, msg = msg)
    }

    fun asSuccessMore(data: T = this.data): StateData<T> {
        return copy(data = data, state = SUCCESS_MORE)
    }

    fun asRefresh(data: T = this.data, msg: String? = null): StateData<T> {
        return copy(data = data, state = REFRESH, msg = msg)
    }

    fun asLoadMore(data: T = this.data): StateData<T> {
        return copy(data = data, state = LOAD_MORE)
    }

    fun asFailed(msg: String? = null): StateData<T> {
        return copy(data = data, state = FAILED, msg = msg)
    }

    fun asFailedMore(msg: String? = null): StateData<T> {
        return copy(data = data, state = FAILED_MORE, msg = msg)
    }

    fun asNoData(msg: String? = null): StateData<T> {
        return copy(data = data, state = NO_DATA, msg = msg)
    }

    fun asNoMoreData(msg: String? = null): StateData<T> {
        return copy(data = data, state = NO_MORE_DATA, msg = msg)
    }

    fun ifSuccess(
        volatileMsg: Boolean = true,
        block: (data: T, msg: String?) -> Unit,
    ): StateData<T> {
        if (isSuccess()) {
            block(data, msg)
            if (volatileMsg) {
                msg = null
            }
        }
        return this
    }

    fun ifSuccessMore(block: (data: T) -> Unit): StateData<T> {
        if (isSuccessMore()) {
            block(data)
        }
        return this
    }

    fun ifRefresh(volatileMsg: Boolean = true, block: (String?) -> Unit): StateData<T> {
        if (isRefresh()) {
            block(msg)
            if (volatileMsg) {
                msg = null
            }
        }
        return this
    }

    fun ifLoadMore(volatileMsg: Boolean = true, block: (String?) -> Unit): StateData<T> {
        if (isLoadMore()) {
            block(msg)
            if (volatileMsg) {
                msg = null
            }
        }
        return this
    }

    fun ifFailed(volatileMsg: Boolean = true, block: (String?) -> Unit): StateData<T> {
        if (isFailed()) {
            block(msg)
            if (volatileMsg) {
                msg = null
            }
        }
        return this
    }

    fun ifFailedMore(volatileMsg: Boolean = true, block: (String?) -> Unit): StateData<T> {
        if (isFailedMore()) {
            block(msg)
            if (volatileMsg) {
                msg = null
            }
        }
        return this
    }

    fun ifNoData(volatileMsg: Boolean = true, block: (String?) -> Unit): StateData<T> {
        if (isNoData()) {
            block(msg)
            if (volatileMsg) {
                msg = null
            }
        }
        return this
    }

    fun ifNoMoreData(volatileMsg: Boolean = true, block: (String?) -> Unit): StateData<T> {
        if (isNoMoreData()) {
            block(msg)
            if (volatileMsg) {
                msg = null
            }
        }
        return this
    }

    val stateString: String
        get() = when (state) {
            SUCCESS -> "SUCCESS"
            SUCCESS_MORE -> "SUCCESS_MORE"
            REFRESH -> "REFRESH"
            LOAD_MORE -> "LOAD_MORE"
            FAILED -> "FAILED"
            FAILED_MORE -> "FAILED_MORE"
            NO_DATA -> "NO_DATA"
            NO_MORE_DATA -> "NO_MORE_DATA"
            else -> "ELSE"
        }

    override fun toString(): String {
        return "StateData(state=$stateString, data=$data, msg=$msg){${hashCode()}}"
    }

    companion object {
        const val SUCCESS = 0
        const val SUCCESS_MORE = 1
        const val REFRESH = 2
        const val LOAD_MORE = 3
        const val FAILED = 4
        const val FAILED_MORE = 5
        const val NO_DATA = 6
        const val NO_MORE_DATA = 7

        fun <T> success(data: T, msg: String? = null): StateData<T> =
            if (data == null || (data is Collection<*>) && data.isEmpty()) {
                StateData(data, NO_DATA, msg)
            } else {
                StateData(data, SUCCESS, msg)
            }

        fun <T> successMore(data: T, msg: String? = null): StateData<T> =
            if (data == null || (data is Collection<*>) && data.isEmpty()) {
                StateData(data, NO_MORE_DATA, msg)
            } else {
                StateData(data, SUCCESS_MORE, msg)
            }

        fun <T> refresh(data: T, msg: String? = null): StateData<T> =
            StateData(data, REFRESH, msg)

        fun <T> loadMore(data: T, msg: String? = null): StateData<T> =
            StateData(data, LOAD_MORE, msg)

        fun <T> failed(data: T, msg: String? = null): StateData<T> = StateData(data, FAILED, msg)
        fun <T> failedMore(data: T, msg: String? = null): StateData<T> =
            StateData(data, FAILED_MORE, msg)

        fun <T> noData(data: T, msg: String? = null): StateData<T> = StateData(data, NO_DATA, msg)
        fun <T> noMoreData(data: T, msg: String? = null): StateData<T> =
            StateData(data, NO_MORE_DATA, msg)
    }

}