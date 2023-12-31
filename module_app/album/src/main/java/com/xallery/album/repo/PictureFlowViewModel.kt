package com.xallery.album.repo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.xallery.album.ui.PictureFlowAdapter
import com.xallery.common.repository.config
import com.xallery.common.repository.db.model.Source
import com.xallery.common.repository.delegate.ISourceRepository
import com.xallery.common.repository.delegate.SourceRepositoryImpl
import com.xallery.common.util.MediaStoreFetcher
import com.xallery.picture.SourceBroadcaster
import com.xihh.base.R
import com.xihh.base.android.appContext
import com.xihh.base.delegate.ILoading
import com.xihh.base.delegate.LoadingDelegate
import com.xihh.base.delegate.NavAction
import com.xihh.base.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PictureFlowViewModel : ViewModel(),
    ILoading by LoadingDelegate(),
    ISourceRepository by SourceRepositoryImpl() {

    private val _dataFlow = MutableStateFlow<Pair<Int, List<IItemBean>?>?>(null)
    val dataFlow = _dataFlow.asStateFlow()

    private val _userActionFlow = MutableSharedFlow<NavAction>()
    val userActionFlow = _userActionFlow.asSharedFlow()

    val imageRecyclerViewPool = RecycledViewPool().apply {
        setMaxRecycledViews(PictureFlowAdapter.VIEW_TYPE_SOURCE, 15)
        setMaxRecycledViews(PictureFlowAdapter.VIEW_TYPE_GROUP, 6)
    }

    var curOriginPosition = 0

    private val sourceBroadcaster = object : SourceBroadcaster(appContext) {
        override fun onSourceChange(source: Source, position: Int) {
            curOriginPosition = position
        }
    }.register(this)

    fun postAction(action: NavAction) = viewModelScope.launch {
        _userActionFlow.emit(action)
    }

    fun refreshSourceList(requestCode: Int, isFirst: Boolean) = viewModelScope.launch {
        showLoading()

        val filterType = filterTypeMap[requestCode]

        if (isFirst) {
            // ------------ pre fetch ------------
            _dataFlow.emit(requestCode to refreshSourceList(filterType, 50))
        }

        _dataFlow.emit(requestCode to refreshSourceList(filterType))

    }.invokeOnCompletion {
        hideLoading()
    }

    private suspend fun refreshSourceList(filterType: Int, num: Int = -1): ArrayList<IItemBean> {
        val sortColumn = config.sortColumn
        val isSortDesc = config.isSortDesc
        val sourceList = getSourceList(filterType, sortColumn, isSortDesc, num)

        return transformToItemBeanList(sortColumn, sourceList)
    }

    private suspend fun transformToItemBeanList(
        sortColumn: String, sourceList: List<Source>,
    ): ArrayList<IItemBean> = withContext(Dispatchers.IO) {
        val filed = Source::class.java.getDeclaredField(sortColumn)
        filed.isAccessible = true
        val itemBeanList = ArrayList<IItemBean>(sourceList.size)

        val locale = Locale.getDefault()
        val calendar = getCurDayStartCalendar()
        val todayTimeMillis = calendar.timeInMillis
        val yesterdayTimeMillis = calendar.timeInMillis - 86400000
        val beforeYesterdayTimeMillis = calendar.timeInMillis - 86400000 * 2
        val curYearTimeMillis = getCurYearStartCalendar().timeInMillis

        sourceList.firstOrNull()?.let { first ->
            val sort = filed.get(first) as Long
            val groupBean = if (sort >= todayTimeMillis) {
                calendar.setToDayStart(sort)
                GroupBean.getToday()
            } else if (sort >= yesterdayTimeMillis) {
                calendar.setToDayStart(sort)
                GroupBean.getYesterday()
            } else if (sort >= beforeYesterdayTimeMillis) {
                calendar.setToDayStart(sort)
                GroupBean.getBeforeYesterday()
            } else if (sort >= curYearTimeMillis) {
                calendar.setToDayStart(sort)
                GroupBean.getMD(calendar, locale)
            } else {
                // ---------- show Year ----------
                calendar.setToDayStart(sort)
                GroupBean.getYMD(calendar, locale)
            }
            itemBeanList.add(groupBean)
            itemBeanList.add(SourceBean(first, 0))
        }
        for (i in 1 until sourceList.size) {
            val source = sourceList[i]
            val sort = filed.get(source) as Long
            if (sort < calendar.timeInMillis) {
                val groupBean = if (sort >= todayTimeMillis) {
                    calendar.setToDayStart(sort)
                    GroupBean.getToday()
                } else if (sort >= yesterdayTimeMillis) {
                    calendar.setToDayStart(sort)
                    GroupBean.getYesterday()
                } else if (sort >= beforeYesterdayTimeMillis) {
                    calendar.setToDayStart(sort)
                    GroupBean.getBeforeYesterday()
                } else if (sort >= curYearTimeMillis) {
                    calendar.setToDayStart(sort)
                    GroupBean.getMD(calendar, locale)
                } else {
                    // ---------- show Year ----------
                    calendar.setToDayStart(sort)
                    GroupBean.getYMD(calendar, locale)
                }
                itemBeanList.add(groupBean)
            }
            itemBeanList.add(SourceBean(source, i))
        }
        itemBeanList
    }

    override fun onCleared() {
        super.onCleared()
        imageRecyclerViewPool.clear()
    }

    data class GroupBean(val name: String) : IItemBean {

        override fun isItemsTheSame(other: IItemBean): Boolean {
            if (javaClass != other.javaClass) return false

            other as GroupBean

            if (name != other.name) return false

            return true
        }

        override fun isContentTheSame(other: IItemBean): Boolean {
            return isItemsTheSame(other)
        }

        companion object {
            fun getToday() = GroupBean(appContext.getString(R.string.calendar_today))
            fun getYesterday() = GroupBean(appContext.getString(R.string.calendar_yesterday))
            fun getBeforeYesterday() =
                GroupBean(appContext.getString(R.string.calendar_before_yesterday))

            fun getMD(calendar: Calendar, locale: Locale) = GroupBean(calendar.getMDString(locale))

            fun getYMD(calendar: Calendar, locale: Locale) =
                GroupBean(calendar.getYMDString(locale))
        }

    }

    data class SourceBean(val source: Source, val originPosition: Int) : IItemBean {

        override fun isItemsTheSame(other: IItemBean): Boolean {
            if (javaClass != other.javaClass) return false

            other as SourceBean

            if (source.id != other.source.id) return false
            if (originPosition != other.originPosition) return false

            return true
        }

        override fun isContentTheSame(other: IItemBean): Boolean {
            if (javaClass != other.javaClass) return false

            other as SourceBean

            if (source != other.source) return false
            if (originPosition != other.originPosition) return false

            return true
        }
    }

    interface IItemBean {
        fun isItemsTheSame(other: IItemBean): Boolean

        fun isContentTheSame(other: IItemBean): Boolean
    }

    companion object {
        const val USER_ACTION_JUMP_POS = 1
        const val USER_ACTION_REFRESH = 2

        const val USER_ACTION_KEY_REQUEST_CODE = "code"
        const val USER_ACTION_KEY_JUMP_POS = "position"

        val filterTypeMap = arrayOf(
            MediaStoreFetcher.FilterType.FILTER_VISUAL_MEDIA,
            MediaStoreFetcher.FilterType.FILTER_VIDEOS,
            MediaStoreFetcher.FilterType.FILTER_GIFS,
            MediaStoreFetcher.FilterType.FILTER_VISUAL_MEDIA
        )
    }
}