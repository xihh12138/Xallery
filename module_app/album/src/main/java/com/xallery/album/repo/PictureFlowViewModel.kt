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

    fun fetchPicture() = viewModelScope.launch {
        showLoading()

        fetchSource()
    }.invokeOnCompletion {
        hideLoading()
    }

    private fun fetchSource() = viewModelScope.launch {
        withContext(Dispatchers.IO) {

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

            val sortOrder = "${MediaStore.Images.Media._ID} DESC"

            appContext.contentResolver.query(
                queryUri,
                projection,
                selections,
                selectionsArgs,
                sortOrder
            )?.use {
                val count = it.count
                val sourceList = ArrayList<Source>(count)
                var i = 1
                if (it.moveToFirst()) {
                    do {
                        val id = it.getLongValue(MediaStore.Images.Media._ID)
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(0)
                        )
                        val path = it.getStringValue(MediaStore.Images.Media.DATA)
                        val mimeType = it.getStringValue(MediaStore.Images.Media.MIME_TYPE)
                        val size = it.getLongValue(MediaStore.Images.Media.SIZE)
                        val takenTimestamp = it.getLongValue(MediaStore.Images.Media.DATE_TAKEN)
                        val addedTimestamp = it.getLongValue(MediaStore.Images.Media.DATE_ADDED)
                        val lastModifiedTimestamp =
                            it.getLongValue(MediaStore.Images.Media.DATE_MODIFIED)
                        val name = it.getStringValue(MediaStore.Images.Media.DISPLAY_NAME)
                        val album = it.getStringValueNullable(MediaStore.Images.Media.ALBUM)
                        val artist = it.getStringValueNullable(MediaStore.Images.Media.ARTIST)
                        val width = it.getIntValueNullable(MediaStore.MediaColumns.WIDTH)
                        val height = it.getIntValueNullable(MediaStore.MediaColumns.HEIGHT)
                        val duration = it.getLongValueNullable(MediaStore.MediaColumns.DURATION)

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
                    } while (it.moveToNext())
                }
                _dataFlow.emit(sourceList)
            }
        }
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