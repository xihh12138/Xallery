package com.xallery.picture.repo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.xallery.common.repository.config
import com.xallery.common.repository.db.model.Source
import com.xallery.common.repository.delegate.ISourceRepository
import com.xallery.common.repository.delegate.SourceRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PictureDetailsViewModel() : ViewModel(), ISourceRepository by SourceRepositoryImpl() {

    private var filterType = 0

    private var position = 0

    private val _curSourceFlow = MutableStateFlow<Pair<Source, Int>?>(null)
    val curSourceFlow = _curSourceFlow.asStateFlow()

    private val _sourceListFlow = MutableStateFlow<List<Source>?>(null)
    val sourceListFlow = _sourceListFlow.asStateFlow()

    init {
        viewModelScope.launch {
            val sortColumn = config.sortColumn
            val isSortDesc = config.isSortDesc
            val sourceList = getSourceList(filterType, sortColumn, isSortDesc)

            _curSourceFlow.emit(sourceList[position] to position)

            _sourceListFlow.emit(sourceList)
        }
    }

    constructor(filterType: Int, position: Int) : this() {
        this.filterType = filterType
        this.position = position
    }

    fun updateCurSource(source: Source, position: Int) = viewModelScope.launch {
        _curSourceFlow.emit(source to position)
    }

    class Factory(private val filterType: Int, private val position: Int) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return modelClass.getConstructor(Int::class.java, Int::class.java)
                .newInstance(filterType, position)
        }
    }
}