package com.xihh.base.model

/**
 * 多选操作的包装类
 **/
data class SelectState<T>(
    val isSelectMode: Boolean = false,
    val selectedCount: Int = 0,
    val totalCount: Int = -1,
    val selectBeanMap: Wrapper<MutableMap<T, Boolean>> = Wrapper(mutableMapOf()),
) {
    val isAllSelected get() = selectedCount == totalCount
}