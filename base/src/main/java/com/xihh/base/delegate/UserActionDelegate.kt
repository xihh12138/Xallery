package com.xihh.base.delegate

import com.xihh.base.android.ActivityTask
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*

interface UserActionDelegate {

    fun getUserActionFlow(): Flow<Int>

    fun addActionNow(flag: Int)

    val cannotCommitNow: Boolean

    val pendingActionSize: Int

    fun addPendingAction(block: () -> Unit)

    fun pollPendingAction(): (() -> Unit)?

    fun execAllPendingAction(block: (() -> Unit) -> Unit)

    fun arrangeAction(block: () -> Unit)
}

class UserActionImpl : UserActionDelegate {

    private val _userActionFlow = MutableSharedFlow<Int>()
    private val userActionFlow = _userActionFlow.asSharedFlow()

    private val pendingActionQueue = LinkedList<() -> Unit>()

    override fun getUserActionFlow() = userActionFlow

    override fun addActionNow(flag: Int) {
        MainScope().launch { _userActionFlow.emit(flag) }
    }

    override val cannotCommitNow: Boolean
        get() = !ActivityTask.isAppInFront

    override val pendingActionSize: Int
        get() = pendingActionQueue.size

    override fun addPendingAction(block: () -> Unit) {
        pendingActionQueue.add(block)
    }

    override fun pollPendingAction(): (() -> Unit)? {
        return pendingActionQueue.poll()
    }

    override fun execAllPendingAction(block: (() -> Unit) -> Unit) {
        var size = pendingActionQueue.size
        while (size-- >= 0) {
            val action = pollPendingAction() ?: continue
            block(action)
        }
    }

    override fun arrangeAction(block: () -> Unit) {
        if (cannotCommitNow) {
            addPendingAction(block)
        } else {
            block()
        }
    }
}