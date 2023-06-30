package com.xallery.common.reposity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.xihh.base.delegate.NavigationDelegate
import com.xihh.base.delegate.NavigationImpl

class RouteViewModel : ViewModel(), NavigationDelegate by NavigationImpl() {

    companion object {
        const val ROUTE_FLAG_MAIN = 1 shl 0
        const val ROUTE_FLAG_PICTURE = 1 shl 1
    }
}

fun getRouter(owner: ViewModelStoreOwner) = ViewModelProvider(owner)[RouteViewModel::class.java]