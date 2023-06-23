package com.xallery.album.repo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.xallery.album.R
import com.xallery.album.ui.PictureFlowAdapter
import com.xallery.common.reposity.db.model.Source
import com.xallery.common.util.MediaStoreFetcher
import com.xihh.base.android.appContext
import com.xihh.base.delegate.ILoading
import com.xihh.base.delegate.LoadingDelegate
import com.xihh.base.util.getCurDayStartCalendar
import com.xihh.base.util.setToDayStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class PictureFlowViewModel : ViewModel(), ILoading by LoadingDelegate() {

    private val _dataFlow = MutableStateFlow<Pair<Int, List<IItemBean>?>?>(null)
    val dataFlow = _dataFlow.asStateFlow()

    private val mediaStoreFetcher = MediaStoreFetcher()

    val imageRecyclerViewPool = RecycledViewPool().apply {
        setMaxRecycledViews(PictureFlowAdapter.VIEW_TYPE_SOURCE, 15)
        setMaxRecycledViews(PictureFlowAdapter.VIEW_TYPE_GROUP, 6)
    }

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
            fetchSource(requestCode, MediaStoreFetcher.QueryParams(filterType, resultNum = 50))
        }

        fetchSource(requestCode, MediaStoreFetcher.QueryParams(filterType))
    }.invokeOnCompletion {
        hideLoading()
    }

    private fun fetchSource(requestCode: Int, queryParams: MediaStoreFetcher.QueryParams) =
        viewModelScope.launch {
            val sourceList = mediaStoreFetcher.fetchSource(queryParams)
            val itemBeanList = withContext(Dispatchers.IO) {
                val itemBeanList = ArrayList<IItemBean>(sourceList.size)

                val locale = Locale.getDefault()
                val calendar = getCurDayStartCalendar()
                val todayTimeMillis = calendar.timeInMillis
                val yesterdayTimeMillis = calendar.timeInMillis - 86400000
                val beforeYesterdayTimeMillis = calendar.timeInMillis - 86400000 * 2
                if (sourceList.size > 0) {
                    val first = sourceList[0]
                    val groupBean = if (first.addTimestamp >= todayTimeMillis) {
                        GroupBean(appContext.getString(R.string.today))
                    } else if (first.addTimestamp >= yesterdayTimeMillis) {
                        GroupBean(appContext.getString(R.string.yesterday))
                    } else if (first.addTimestamp >= beforeYesterdayTimeMillis) {
                        GroupBean(appContext.getString(R.string.before_yesterday))
                    } else {
                        GroupBean(
                            "${
                                calendar.getDisplayName(
                                    Calendar.MONTH,
                                    Calendar.SHORT_FORMAT,
                                    locale
                                )
                            }${
                                calendar.get(Calendar.DAY_OF_MONTH)
                            }${appContext.getString(R.string.day)}"
                        )
                    }
                    itemBeanList.add(groupBean)
                    itemBeanList.add(SourceBean(first))
                    calendar.setToDayStart(first.addTimestamp)
                }
                for (i in 1 until sourceList.size) {
                    val source = sourceList[i]
                    if (source.addTimestamp < calendar.timeInMillis) {
                        val groupBean = if (source.addTimestamp >= todayTimeMillis) {
                            GroupBean(appContext.getString(R.string.today))
                        } else if (source.addTimestamp >= yesterdayTimeMillis) {
                            GroupBean(appContext.getString(R.string.yesterday))
                        } else if (source.addTimestamp >= beforeYesterdayTimeMillis) {
                            GroupBean(appContext.getString(R.string.before_yesterday))
                        } else {
                            GroupBean(
                                "${
                                    calendar.getDisplayName(
                                        Calendar.MONTH,
                                        Calendar.SHORT_FORMAT,
                                        locale
                                    )
                                }${
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                }${appContext.getString(R.string.day)}"
                            )
                        }
                        itemBeanList.add(groupBean)
                        calendar.setToDayStart(source.addTimestamp)
                    }
                    itemBeanList.add(SourceBean(source))
                }
                itemBeanList
            }

            _dataFlow.emit(requestCode to itemBeanList)
        }

    data class GroupBean(
        val name: String
    ) : IItemBean {

        override fun isItemsTheSame(other: IItemBean): Boolean {
            if (javaClass != other.javaClass) return false

            other as GroupBean

            if (name != other.name) return false

            return true
        }

        override fun isContentTheSame(other: IItemBean): Boolean {
            return isItemsTheSame(other)
        }
    }

    data class SourceBean(
        val source: Source
    ) : IItemBean {

        override fun isItemsTheSame(other: IItemBean): Boolean {
            if (javaClass != other.javaClass) return false

            other as SourceBean

            if (source.id == other.source.id) return false

            return true
        }

        override fun isContentTheSame(other: IItemBean): Boolean {
            if (javaClass != other.javaClass) return false

            other as SourceBean

            if (source != other.source) return false

            return true
        }
    }

    interface IItemBean {
        fun isItemsTheSame(other: IItemBean): Boolean

        fun isContentTheSame(other: IItemBean): Boolean
    }
}