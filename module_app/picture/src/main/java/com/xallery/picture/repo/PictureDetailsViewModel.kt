package com.xallery.picture.repo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xallery.common.reposity.db.model.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PictureDetailsViewModel : ViewModel() {

    private val _curSourceFlow = MutableStateFlow<Source?>(null)
    val curSourceFlow = _curSourceFlow.asStateFlow()

    fun updateCurSource(source: Source?) = viewModelScope.launch {
        _curSourceFlow.emit(source)
    }

}