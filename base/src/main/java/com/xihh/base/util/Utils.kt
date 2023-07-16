package com.xihh.base.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Size
import android.util.SizeF
import android.view.View
import android.webkit.MimeTypeMap
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.util.Calendar

fun isVersionGreater(sdkInt: Int) = Build.VERSION.SDK_INT >= sdkInt

fun Context.goBrowser(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Exception) {
        logf { "jumpToUriPage: e=${e.stackTraceToString()}" }
    }
}

fun Context.goAppDetailPage() {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Exception) {
        logf { "go AppDetailPage error: ${e.stackTraceToString()}" }
    }
}

fun jsonOf(vararg kvs: Pair<String, String>): String {
    val ans = JSONObject()

    kvs.forEach {
        ans.put(it.first, it.second)
    }

    return ans.toString()
}

fun Paint.getTextSizeF(text: String): SizeF {
    val textPath = Path()
    getTextPath(text, 0, text.length, 0f, 0f, textPath)
    val rect = RectF()
    textPath.computeBounds(rect, true)
    return SizeF(rect.width(), rect.height())
}

fun Paint.getTextSize(text: String): Size {
    val textPath = Path()
    getTextPath(text, 0, text.length, 0f, 0f, textPath)
    val rect = RectF()
    textPath.computeBounds(rect, true)
    return Size(rect.width().toInt(), rect.height().toInt())
}

fun getExtension(path: String): String? {
    return MimeTypeMap.getFileExtensionFromUrl(path).ifBlank { path.substringAfterLast(".") }
}

fun getMimeType(path: String?): String? {
    var mime: String? = null
    path ?: return mime
    val mmr = MediaMetadataRetriever()
    try {
        mmr.setDataSource(path)
        mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        mmr.release()
    }
    return mime
}

/**
 * @return 返回媒体文件时长(毫秒)
 **/
fun getMediaDuration(path: String?): Long? {
    var mime: Long? = null
    path ?: return mime
    val mmr = MediaMetadataRetriever()
    try {
        mmr.setDataSource(path)
        mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        mmr.release()
    }
    return mime
}

fun getContentUri(mimeType: String?): Uri = when {
    mimeType == null -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    mimeType.startsWith("image") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    mimeType.startsWith("video") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    mimeType.startsWith("audio") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    else -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
}

fun getUri(mimeType: String?, mediaStoreId: Long): Uri =
    ContentUris.withAppendedId(getContentUri(mimeType), mediaStoreId)

fun RecyclerView.scrollToPositionIfNeed(position: Int) {
    if (isComputingLayout || isInLayout || layoutManager?.isAttachedToWindow == false) {
        addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int,
            ) {
                removeOnLayoutChangeListener(this)

                scrollToFullVisible(position)
            }
        })
    } else {
        scrollToFullVisible(position)
    }
}

fun RecyclerView.scrollToFullVisible(position: Int, isSmooth: Boolean = false) {
    // Scroll to position if the view for the current position is null (not currently part of
    // layout manager children), or it's not completely visible.
    if (!isPositionFullVisible(position)) {
        if (isSmooth) {
            smoothScrollToPosition(position)
        } else {
            layoutManager?.scrollToPosition(position)
        }
    }
}

fun RecyclerView.isPositionFullVisible(position: Int): Boolean {
    val view = findViewHolderForAdapterPosition(position)?.itemView
    // Scroll to position if the view for the current position is null (not currently part of
    // layout manager children), or it's not completely visible.
    return !(view == null || layoutManager?.isViewPartiallyVisible(view, false, true) == true)
}