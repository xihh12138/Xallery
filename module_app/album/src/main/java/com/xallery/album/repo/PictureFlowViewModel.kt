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

    private val _dataFlow = MutableStateFlow<List<Source>?>(null)
    val dataFlow = _dataFlow.asStateFlow()

    private val mediaStoreFetcher = MediaStoreFetcher()

    fun fetchPicture(isFirst: Boolean) = viewModelScope.launch {
        showLoading()

        if (isFirst) {
            // ------------ pre fetch ------------
            fetchSource(50)
        }

        fetchSource()
    }.invokeOnCompletion {
        hideLoading()
    }

    private fun fetchSource(fetchNum: Int = -1) = viewModelScope.launch {
        _dataFlow.emit(mediaStoreFetcher.fetchSource(fetchNum))
    }
}