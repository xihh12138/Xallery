package com.xallery.album.repo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xallery.common.reposity.db.model.Source
import com.xallery.common.util.MediaStoreFetcher
import com.xihh.base.delegate.ILoading
import com.xihh.base.delegate.LoadingDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PictureFlowViewModel : ViewModel(), ILoading by LoadingDelegate() {

    private val _dataFlow = MutableStateFlow<Pair<Int, List<Source>?>?>(null)
    val dataFlow = _dataFlow.asStateFlow()

    private val mediaStoreFetcher = MediaStoreFetcher()

    fun fetchSource(requestCode: Int, isFirst: Boolean) = viewModelScope.launch {
        showLoading()
        val filterType = when (requestCode) {
            0 -> MediaStoreFetcher.FilterType.FILTER_ALL
            1 -> MediaStoreFetcher.FilterType.FILTER_VIDEOS
            2 -> MediaStoreFetcher.FilterType.FILTER_GIFS
            else -> MediaStoreFetcher.FilterType.FILTER_ALL
        }
        if (isFirst) {
            // ------------ pre fetch ------------
            fetchSource(requestCode, MediaStoreFetcher.QueryParams(filterType, queryNum = 50))
        }

        fetchSource(requestCode, MediaStoreFetcher.QueryParams(filterType))
    }.invokeOnCompletion {
        hideLoading()
    }

    private fun fetchSource(requestCode: Int, queryParams: MediaStoreFetcher.QueryParams) =
        viewModelScope.launch {
            _dataFlow.emit(requestCode to mediaStoreFetcher.fetchSource(queryParams))
        }
}