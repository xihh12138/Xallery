package com.xallery.common.repository.delegate

import androidx.sqlite.db.SimpleSQLiteQuery
import com.xallery.common.repository.constant.Constant
import com.xallery.common.repository.db.db
import com.xallery.common.repository.db.model.Source
import com.xallery.common.util.MediaStoreFetcher
import com.xihh.base.util.logx

interface ISourceRepository {

    suspend fun getSourceList(
        filterType: Int, sortColumn: String?, isSortDesc: Boolean, resultNum: Int = -1,
    ): List<Source>

}

class SourceRepositoryImpl : ISourceRepository {

    override suspend fun getSourceList(
        filterType: Int, sortColumn: String?, isSortDesc: Boolean, resultNum: Int,
    ): List<Source> {
        logx { "getSourceList: filterType=$filterType sortColumn=$sortColumn isSortDesc=$isSortDesc resultNum=$resultNum" }
        val selectionMimeTypeBuilder = StringBuilder()
        if (filterType and MediaStoreFetcher.FilterType.FILTER_IMAGES != 0) {
            val selectionMimeType = when {
                filterType and MediaStoreFetcher.FilterType.FILTER_GIFS == MediaStoreFetcher.FilterType.FILTER_GIFS -> {
                    Constant.MimeType.GIF
                }

                else -> {
                    Constant.MimeType.IMAGE_START
                }
            }
            selectionMimeTypeBuilder.append(" mimeType LIKE '$selectionMimeType%' OR")
        }
        if (filterType and MediaStoreFetcher.FilterType.FILTER_VIDEOS != 0) {
            selectionMimeTypeBuilder.append(" mimeType LIKE '${Constant.MimeType.VIDEO_START}%' OR")
        }
        selectionMimeTypeBuilder.let {
            val length = it.length
            it.deleteRange(length - 3, length)
        }

        val sql = StringBuilder("SELECT * FROM Source")
        if (selectionMimeTypeBuilder.isNotBlank()) {
            sql.append(" WHERE$selectionMimeTypeBuilder")
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