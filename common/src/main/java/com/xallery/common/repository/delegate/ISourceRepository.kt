package com.xallery.common.repository.delegate

import androidx.sqlite.db.SimpleSQLiteQuery
import com.xallery.common.repository.constant.Constant
import com.xallery.common.repository.db.db
import com.xallery.common.repository.db.model.Source
import com.xallery.common.util.MediaStoreFetcher
import com.xihh.base.util.logx

interface ISourceRepository {

    suspend fun getSourceList(
        filterType: Int, sortColumn: String?, isSortDesc: Boolean, resultNum: Int,
    ): List<Source>

}

class SourceRepositoryImpl : ISourceRepository {

    override suspend fun getSourceList(
        filterType: Int, sortColumn: String?, isSortDesc: Boolean, resultNum: Int,
    ): List<Source> {
        logx { "getSourceList: filterType=$filterType sortColumn=$sortColumn isSortDesc=$isSortDesc resultNum=$resultNum" }
        val selectionMimeType =
            if (filterType and MediaStoreFetcher.FilterType.FILTER_IMAGES != 0) {
                when {
                    filterType and MediaStoreFetcher.FilterType.FILTER_GIFS != 0 -> {
                        Constant.MimeType.GIF
                    }

                    else -> {
                        Constant.MimeType.IMAGE_START
                    }
                }
            } else if (filterType and MediaStoreFetcher.FilterType.FILTER_VIDEOS != 0) {
                Constant.MimeType.VIDEO_START
            } else {
                null
            }

        val sql = StringBuilder("SELECT * FROM Source")
        if (selectionMimeType != null) {
            sql.append(" WHERE mimeType LIKE '$selectionMimeType%'")
        }
        if (sortColumn != null) {
            sql.append(" ORDER BY $sortColumn")
            if (isSortDesc) {
                sql.append(" DESC")
            }
        }
        if (resultNum > 0) {
            sql.append(" LIMIT $resultNum")
        }

        return db.sourceDao.query(SimpleSQLiteQuery(sql.toString()))
    }
}