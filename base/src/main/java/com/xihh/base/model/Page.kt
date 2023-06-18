package com.xihh.base.model

data class Page<T>(
    val currentPage: Int,//当前页码
    val lastPage: Int = -1,//总页数
    val totalCount: Int = -1,//总记录数
    val data: List<T>? = null,//页面数据
) {
    fun addPage(appendData: List<T>): Page<T> {
        return Page(
            currentPage + 1, lastPage, totalCount, if (data == null) {
                appendData
            } else {
                ArrayList(data + appendData)
            }
        )
    }
}
