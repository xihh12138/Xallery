package com.xallery.picture.repo

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.xallery.common.repository.config
import com.xallery.common.repository.db.model.Source
import com.xallery.common.repository.delegate.ISourceRepository
import com.xallery.common.repository.delegate.SourceRepositoryImpl
import com.xallery.picture.repo.bean.SourcePageInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SourceDetailsViewModel() : ViewModel(), ISourceRepository by SourceRepositoryImpl() {

    private var filterType = 0

    private var position = 0

    private val _sourcePageInfoFlow = MutableStateFlow<SourcePageInfo?>(null)
    val sourcePageInfoFlow = _sourcePageInfoFlow.asStateFlow()

    private val _sourceListFlow = MutableStateFlow<List<Source>?>(null)
    val sourceListFlow = _sourceListFlow.asStateFlow()

    init {
        viewModelScope.launch {
            val sortColumn = config.sortColumn
            val isSortDesc = config.isSortDesc
            val sourceList = getSourceList(filterType, sortColumn, isSortDesc)

            _sourcePageInfoFlow.emit(
                SourcePageInfo(sourceList[position], position, sourceList.size)
            )

            _sourceListFlow.emit(sourceList)
        }
    }

    @Keep
    constructor(filterType: Int, position: Int) : this() {
        this.filterType = filterType
        this.position = position
    }

    fun updateCurPosition(position: Int) = viewModelScope.launch {
        _sourceListFlow.value?.let { list ->
            list.getOrNull(position)?.let {
                _sourcePageInfoFlow.emit(SourcePageInfo(it, position, list.size))
            }
        }
    }

    class Factory(private val filterType: Int, private val position: Int) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return modelClass.getConstructor(Int::class.java, Int::class.java)
                .newInstance(filterType, position)
        }
    }
}