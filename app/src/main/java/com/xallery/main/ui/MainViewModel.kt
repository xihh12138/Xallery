package com.xallery.main.ui

import androidx.annotation.IntDef
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xihh.base.delegate.ILoading
import com.xihh.base.delegate.IToast
import com.xihh.base.delegate.LoadingDelegate
import com.xihh.base.delegate.ToastDelegate
import com.xihh.base.delegate.UserActionDelegate
import com.xihh.base.delegate.UserActionImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(),
    ILoading by LoadingDelegate(),
    IToast by ToastDelegate(){

    private val _mainPageFlow = MutableStateFlow(0)
    val mainPageFlow = _mainPageFlow.asStateFlow()

    fun goMainPage(page: Int) = viewModelScope.launch {
        _mainPageFlow.emit(page)
    }

    companion object {
        const val MAIN_PAGE_ALL = 0
        const val MAIN_PAGE_MOVIE = 1
        const val MAIN_PAGE_GIF = 2
        const val MAIN_PAGE_ELSE = 3
    }
}

@IntDef(
    MainViewModel.MAIN_PAGE_ALL,
    MainViewModel.MAIN_PAGE_MOVIE,
    MainViewModel.MAIN_PAGE_GIF,
    MainViewModel.MAIN_PAGE_ELSE
)
annotation class MainPageFlags