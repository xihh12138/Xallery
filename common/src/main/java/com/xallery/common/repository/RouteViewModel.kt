package com.xallery.common.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.xihh.base.delegate.NavigationDelegate
import com.xihh.base.delegate.NavigationImpl

class RouteViewModel : ViewModel(), NavigationDelegate by NavigationImpl() {

    companion object {
        const val ROUTE_FLAG_MAIN = 1
        const val ROUTE_FLAG_PICTURE = 2
    }
}

fun getRouter(owner: ViewModelStoreOwner) = ViewModelProvider(owner)[RouteViewModel::class.java]