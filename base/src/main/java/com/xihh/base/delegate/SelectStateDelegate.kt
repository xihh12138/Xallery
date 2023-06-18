package com.xihh.base.delegate

import com.xihh.base.model.SelectState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface ISelectState<Bean> {

    fun updateSelectBean(bean: Bean, isSelect: Boolean)

    fun updateSelectBeans(bean: List<Bean>, isSelect: Boolean)

    fun attachSelectBeans(countingBeanList: List<Bean>?)

    fun getSelectStateFlow(): StateFlow<SelectState<Bean>>
}

class SelectStateDelegate<Bean> : ISelectState<Bean> {

    private val _selectStateFlow = MutableStateFlow(SelectState<Bean>())
    private val selectStateFlow = _selectStateFlow.asStateFlow()

    /**
     * 传入的bean必须之前已经调用[attachSelectBeans]进行注册，并且目标的状态和记录里的不一样，才会更新选择状态
     **/
    override fun updateSelectBean(bean: Bean, isSelect: Boolean) {
        val map = _selectStateFlow.value.selectBeanMap()
        if (map.contains(bean) && map[bean] != isSelect) {
            _selectStateFlow.update { selectState ->
                map[bean] = isSelect

                selectState.copy(
                    selectedCount = selectState.selectedCount + (if (isSelect) +1 else -1),
                    selectBeanMap = selectState.selectBeanMap.copy()
                )
            }
        }
    }

    /**
     * 传入的beanList必须之前已经调用[attachSelectBeans]进行注册，并且目标状态和记录里的不一样，才会更新选择状态
     **/
    override fun updateSelectBeans(beanList: List<Bean>, isSelect: Boolean) {
        val map = _selectStateFlow.value.selectBeanMap()
        val changeBeanList = beanList.filter { map.contains(it) && map[it] != isSelect }
        if (changeBeanList.isNotEmpty()) {
            _selectStateFlow.update { selectState ->
                changeBeanList.forEach {
                    selectState.selectBeanMap()[it] = isSelect
                }

                selectState.copy(
                    selectedCount = selectState.selectedCount + (if (isSelect) +changeBeanList.size else -changeBeanList.size),
                    selectBeanMap = selectState.selectBeanMap.copy()
                )
            }
        }
    }

    /**
     * 注册数据源，此后会开始接管数据源的选中状态的更新，传入null代表停止接管数据源
     **/
    override fun attachSelectBeans(countingBeanList: List<Bean>?) {
        _selectStateFlow.update { selectState ->
            selectState.selectBeanMap().clear()

            val newSelectMode = countingBeanList != null
            if (newSelectMode) {
                countingBeanList!!.forEach {
                    selectState.selectBeanMap()[it] = false
                }
            }

            selectState.copy(
                isSelectMode = newSelectMode,
                selectedCount = 0,
                totalCount = countingBeanList?.size ?: -1
            )
        }
    }

    override fun getSelectStateFlow() = selectStateFlow

}