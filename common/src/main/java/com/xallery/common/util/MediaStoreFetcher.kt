package com.xallery.common.util

import android.content.ContentUris
import android.provider.MediaStore
import androidx.annotation.IntDef
import com.xallery.common.reposity.constant.Constant
import com.xallery.common.reposity.db.model.Source
import com.xihh.base.android.appContext
import com.xihh.base.util.getIntValueNullable
import com.xihh.base.util.getLongValue
import com.xihh.base.util.getLongValueNullable
import com.xihh.base.util.getStringValue
import com.xihh.base.util.getStringValueNullable
import com.xihh.base.util.logx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.LinkedList

class MediaStoreFetcher {

    suspend fun fetchSource(queryParams: QueryParams? = null) = withContext(Dispatchers.IO) {
        val queryParams = queryParams ?: QueryParams()
        val queryUri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DURATION
        )

        val (selections, selectionsArgs) = getSelectionAndArgs(queryParams.filterType)

        val sortOrder =
            getSortOrder(queryParams.resultNum, queryParams.sortColumn, queryParams.desc)

        appContext.contentResolver.query(
            queryUri,
            projection,
            selections,
            selectionsArgs,
            sortOrder
        )?.use { cursor ->
            val count = cursor.count
            val sourceList = ArrayList<Source>(count)
            var i = 1
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getLongValue(MediaStore.Images.Media._ID)
                    val mimeType = cursor.getStringValue(MediaStore.Images.Media.MIME_TYPE)

                    val contentUri = if (mimeType.startsWith(Constant.MimeType.VIDEO)) {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    val uri = ContentUris.withAppendedId(contentUri, id)

                    val path = cursor.getStringValue(MediaStore.Images.Media.DATA)
                    val size = cursor.getLongValue(MediaStore.Images.Media.SIZE)
                    val takenTimestamp = cursor.getLongValue(MediaStore.Images.Media.DATE_TAKEN)
                    val addedTimestamp =
                        cursor.getLongValue(MediaStore.Images.Media.DATE_ADDED) * 1000
                    val lastModifiedTimestamp =
                        cursor.getLongValue(MediaStore.Images.Media.DATE_MODIFIED) * 1000
                    val name = cursor.getStringValue(MediaStore.Images.Media.DISPLAY_NAME)
                    val album = cursor.getStringValueNullable(MediaStore.Images.Media.ALBUM)
                    val width = cursor.getIntValueNullable(MediaStore.MediaColumns.WIDTH)
                    val height = cursor.getIntValueNullable(MediaStore.MediaColumns.HEIGHT)
                    val duration = cursor.getLongValueNullable(MediaStore.MediaColumns.DURATION)

                    val source = Source(
                        id,
                        uri,
                        path,
                        mimeType,
                        size,
                        takenTimestamp,
                        addedTimestamp,
                        lastModifiedTimestamp,
                        name,
                        album,
                        width,
                        height,
                        duration,
                    )
                    logx { "fetchSource($i/$count): $source" }
                    sourceList.add(source)
                    i++
                } while (cursor.moveToNext())
            }
            sourceList
        } ?: arrayListOf()
    }

    private fun getSortOrder(fetchNum: Int, sortColumn: String, desc: Boolean): String {
        val builder = StringBuilder()

        builder.append(sortColumn)

        if (desc) {
            builder.append(" DESC")
        }

        if (fetchNum != -1) {
            builder.append(" LIMIT ")
            builder.append(fetchNum)
        }

        return builder.toString()
    }

    private fun getSelectionAndArgs(@FilterType filterType: Int): Pair<String, Array<String>> {
        val selection = StringBuilder()
        val args = LinkedList<String>()
        if (filterType and FilterType.FILTER_IMAGES != 0) {
            selection.append("${MediaStore.Images.Media.MIME_TYPE} LIKE ? OR ")
            args.add("${Constant.MimeType.IMAGE}%")
        } else {
            if (filterType and FilterType.FILTER_GIFS != 0) {
                selection.append("${MediaStore.Images.Media.MIME_TYPE} = ? OR ")
                args.add(Constant.MimeType.GIF)
            }
        }

        if (filterType and FilterType.FILTER_VIDEOS != 0) {
            selection.append("${MediaStore.Images.Media.MIME_TYPE} LIKE ? OR ")
            args.add("${Constant.MimeType.VIDEO}%")
        }

        return selection.trim().removeSuffix("OR").toString() to args.toTypedArray()
    }

    data class QueryParams(
        @FilterType val filterType: Int = FilterType.FILTER_ALL,
        val resultNum: Int = -1,
        val sortColumn: String = MediaStore.Images.Media._ID,
        val desc: Boolean = true
    )

    @IntDef(
        value = [
            FilterType.FILTER_IMAGES,
            FilterType.FILTER_VIDEOS,
            FilterType.FILTER_GIFS,
            FilterType.FILTER_ALL,
        ]
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class FilterType {
        companion object {
            const val FILTER_IMAGES = 1
            const val FILTER_VIDEOS = 1 shl 1
            const val FILTER_GIFS = 1 shl 2
            const val FILTER_ALL = Int.MAX_VALUE
        }
    }
}