package com.xallery.common.repository.delegate

import com.xallery.common.repository.constant.Constant
import com.xallery.common.repository.db.db
import com.xallery.common.repository.db.model.Source
import com.xallery.common.util.MediaStoreFetcher

interface ISourceRepository {

    suspend fun getSourceList(queryParams: MediaStoreFetcher.QueryParams? = null): List<Source>

}

class SourceRepositoryImpl : ISourceRepository {

    override suspend fun getSourceList(queryParams: MediaStoreFetcher.QueryParams?): List<Source> {
        val queryParams = queryParams ?: MediaStoreFetcher.QueryParams()

        val selectionMimeType =
            if (queryParams.filterType and MediaStoreFetcher.FilterType.FILTER_IMAGES != 0) {
                when {
                    queryParams.filterType and MediaStoreFetcher.FilterType.FILTER_GIFS != 0 -> {
                        Constant.MimeType.GIF
                    }

                    else -> {
                        Constant.MimeType.IMAGE_START
                    }
                }
            } else if (queryParams.filterType and MediaStoreFetcher.FilterType.FILTER_VIDEOS != 0) {
                Constant.MimeType.VIDEO_START
            } else {
                null
            }

        return if (selectionMimeType == null) {
            if (queryParams.resultNum > 0) {
                if (queryParams.desc) {
                    db.sourceDao.getLimitDSort(queryParams.resultNum, queryParams.sortColumn)
                } else {
                    db.sourceDao.getLimitSort(queryParams.resultNum, queryParams.sortColumn)
                }
            } else {
                if (queryParams.desc) {
                    db.sourceDao.getAllDSort(queryParams.sortColumn)
                } else {
                    db.sourceDao.getAllSort(queryParams.sortColumn)
                }
            }
        } else {
            if (queryParams.resultNum > 0) {
                if (queryParams.desc) {
                    db.sourceDao.getByMimeTypeLimitDSort(
                        selectionMimeType, queryParams.resultNum, queryParams.sortColumn
                    )
                } else {
                    db.sourceDao.getByMimeTypeLimitSort(
                        selectionMimeType, queryParams.resultNum, queryParams.sortColumn
                    )
                }
            } else {
                if (queryParams.desc) {
                    db.sourceDao.getByMimeTypeDSort(selectionMimeType, queryParams.sortColumn)
                } else {
                    db.sourceDao.getByMimeTypeSort(selectionMimeType, queryParams.sortColumn)
                }
            }
        }
    }
}