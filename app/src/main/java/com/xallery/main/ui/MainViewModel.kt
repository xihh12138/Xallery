package com.xallery.main.ui

import androidx.annotation.IntDef
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xallery.common.repository.db.db
import com.xallery.common.util.MediaStoreFetcher
import com.xallery.common.util.SourceDBReadyBroadcaster
import com.xihh.base.android.appContext
import com.xihh.base.delegate.ILoading
import com.xihh.base.delegate.IToast
import com.xihh.base.delegate.LoadingDelegate
import com.xihh.base.delegate.ToastDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(), ILoading by LoadingDelegate(), IToast by ToastDelegate() {

    private val mediaStoreFetcher = MediaStoreFetcher()

    private val _mainPageFlow = MutableStateFlow(0)
    val mainPageFlow = _mainPageFlow.asStateFlow()

    fun goMainPage(page: Int) = viewModelScope.launch {
        _mainPageFlow.emit(page)
    }

    fun fetchAllSource() = viewModelScope.launch {
        val oldSourceCount = db.sourceDao.getCount()

        val sourceList =
            mediaStoreFetcher.fetchSource(MediaStoreFetcher.QueryParams(MediaStoreFetcher.FilterType.FILTER_NONE))

        db.sourceDao.addAll(sourceList)

        SourceDBReadyBroadcaster(appContext).notifySourceDBReady(oldSourceCount, sourceList.size)
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