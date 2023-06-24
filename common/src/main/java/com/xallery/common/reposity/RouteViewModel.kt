package com.xallery.common.reposity

import androidx.lifecycle.ViewModel
import com.xihh.base.delegate.UserActionDelegate
import com.xihh.base.delegate.UserActionImpl

class RouteViewModel : ViewModel(), UserActionDelegate by UserActionImpl() {

    companion object {
        const val ROUTE_MAIN = 1 shl 0
        const val ROUTE_PICTURE = 1 shl 1
    }
}