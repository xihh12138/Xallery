package com.xihh.base.util

import android.util.ArrayMap

class ReuseReferenceProvider<Value>(private val maxPoolSize: Int = 3) {

    private val poolCopy = ArrayList<ClearWrapper<Value>>(maxPoolSize)

    private val pool = ArrayList<Value>(maxPoolSize)

    // ---------- 引用处理 ----------

    private var totalReferenceCount = 0

    private val referenceCounter = ArrayMap<Value, Int>(maxPoolSize)

    // ---------- 引用处理 ----------

    fun init(
        builder: (index: Int) -> Value, onClear: ((Value) -> Unit)?,
    ): ReuseReferenceProvider<Value> {
        for (i in 0 until maxPoolSize) {
            val value = builder(i)

            pool.add(value)
            poolCopy.add(ClearWrapper(value) { onClear?.invoke(value) })
        }

        return this
    }

    /**
     * 优先获取没有被引用的对象，如果都有引用了就获取被引用数最少的对象
     **/
    fun acquire(): Value {
        if (totalReferenceCount != 0) {
            pool.sortWith { o1, o2 ->
                (referenceCounter[o1] ?: 0) - (referenceCounter[o2] ?: 0)
            }
        }

        return pool.first().also { value ->
            totalReferenceCount++
            referenceCounter[value] = (referenceCounter[value] ?: 0) + 1
            logx { "ReuseReferenceProvider: acquire[${referenceCounter[value]}] $value" }
        }
    }

    fun release(value: Value): Boolean {
        if (pool.contains(value)) {
            totalReferenceCount--
            referenceCounter[value] = Math.max(0, (referenceCounter[value] ?: 0) - 1)
            logx { "ReuseReferenceProvider: release[${referenceCounter[value]}] $value" }

            return true
        }

        return false
    }

    fun clear() {
        poolCopy.forEach {
            it.onClear.invoke()
        }
        poolCopy.clear()
        pool.clear()
        referenceCounter.clear()
    }

    class ClearWrapper<Value>(val value: Value, val onClear: () -> Unit)
}