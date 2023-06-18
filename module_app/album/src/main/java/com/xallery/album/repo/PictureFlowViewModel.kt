package com.xallery.album.repo

import android.content.ContentUris
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xallery.common.reposity.constant.Constant
import com.xallery.common.reposity.db.model.Source
import com.xihh.base.android.appContext
import com.xihh.base.delegate.ILoading
import com.xihh.base.delegate.LoadingDelegate
import com.xihh.base.util.getIntValueNullable
import com.xihh.base.util.getLongValue
import com.xihh.base.util.getLongValueNullable
import com.xihh.base.util.getStringValue
import com.xihh.base.util.getStringValueNullable
import com.xihh.base.util.logx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList

class PictureFlowViewModel : ViewModel(), ILoading by LoadingDelegate() {

    private val _dataFlow = MutableStateFlow<List<Source>?>(null)
    val dataFlow = _dataFlow.asStateFlow()

    fun fetchPicture(isFirst: Boolean) = viewModelScope.launch {
        showLoading()

        if (isFirst) {
            // ------------ pre fetch ------------
            fetchSource(50)
        }

        fetchSource()
    }.invokeOnCompletion {
        hideLoading()
    }

    private suspend fun fetchSource(fetchNum: Int = -1) = withContext(Dispatchers.IO) {
        val filterMedia = Int.MAX_VALUE
        val queryUri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE,
            MediaStore.MediaColumns.DURATION
        )

        val selections = getSelections(filterMedia)
        val selectionsArgs = getSelectionArgs(filterMedia)
        val sortColumn = MediaStore.Images.Media._ID
        val isEsc = true

        val sortOrder = getSortOrder(fetchNum,sortColumn,isEsc)

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
                    val addedTimestamp = cursor.getLongValue(MediaStore.Images.Media.DATE_ADDED) * 1000
                    val lastModifiedTimestamp =
                        cursor.getLongValue(MediaStore.Images.Media.DATE_MODIFIED) * 1000
                    val name = cursor.getStringValue(MediaStore.Images.Media.DISPLAY_NAME)
                    val album = cursor.getStringValueNullable(MediaStore.Images.Media.ALBUM)
                    val artist = cursor.getStringValueNullable(MediaStore.Images.Media.ARTIST)
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
                        artist,
                        width,
                        height,
                        duration
                    )
                    logx { "fetchSource($i/$count): $source" }
                    sourceList.add(source)
                    i++
                } while (cursor.moveToNext())
            }
            _dataFlow.emit(sourceList)
        }
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

    private fun getSelections(filterMedia: Int): String {
        val query = StringBuilder()
        if (filterMedia and Constant.FileType.IMAGES != 0) {
            query.append("${MediaStore.Images.Media.MIME_TYPE} LIKE ? OR ")
        }

        if (filterMedia and Constant.FileType.VIDEOS != 0) {
            query.append("${MediaStore.Images.Media.MIME_TYPE} LIKE ? OR ")
        }

        return query.toString().trim().removeSuffix("OR")
    }

    private fun getSelectionArgs(filterMedia: Int): Array<String> {
        val args = LinkedList<String>()
        if (filterMedia and Constant.FileType.IMAGES != 0) {
            args.add("${Constant.MimeType.IMAGE}%")
        }

        if (filterMedia and Constant.FileType.VIDEOS != 0) {
            args.add("${Constant.MimeType.VIDEO}%")
        }

        return args.toTypedArray()
    }
}