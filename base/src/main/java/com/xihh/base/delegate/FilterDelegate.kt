package com.xihh.base.delegate

import kotlinx.coroutines.flow.*

interface IFilter<Content> {
    fun updateFilterContent(content: Content)

    fun getFilterContentFlow(): StateFlow<Content>
}

class FilterDelegate<Content>(defaultContent:Content) : IFilter<Content> {

    private val _filterContentFlow = MutableStateFlow(defaultContent)
    private val filterContentFlow = _filterContentFlow.asStateFlow()

    override fun updateFilterContent(content: Content) {
        _filterContentFlow.update { content }
    }

    override fun getFilterContentFlow() = filterContentFlow
}