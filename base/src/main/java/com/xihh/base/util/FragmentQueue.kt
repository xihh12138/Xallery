package com.xihh.base.util

import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference
import java.util.*

class FragmentQueue private constructor(fragmentManager: FragmentManager) {

    private val fragmentManager = WeakReference(fragmentManager)

    private val queue = LinkedList<OrderFragment>()

    fun add(orderFragment: OrderFragment) {
        queue.add(orderFragment)
    }

    fun remove(orderFragment: OrderFragment) {
        queue.remove(orderFragment)
    }

    fun remove(flag: String) {
        queue.removeAll { it.getFlag() == flag }
    }

    fun removeRest() {
        if (queue.size > 1) {
            val cur = queue.poll()
            queue.clear()
            queue.add(cur)
        }
    }

    fun next() {
        if (queue.isNotEmpty()) {
            queue.poll()
        }

        if (queue.isNotEmpty()) {
            show()
        }
    }

    fun show() {
        queue.firstOrNull()?.show(this)
    }

    fun getFragmentManager() = fragmentManager.get()

    companion object {

        fun build(fragmentManager: FragmentManager): FragmentQueue {
            return FragmentQueue(fragmentManager)
        }
    }

}

interface OrderFragment {
    fun show(fragmentQueue: FragmentQueue)

    fun dismiss(fragmentQueue: FragmentQueue)

    fun getFlag(): String
}