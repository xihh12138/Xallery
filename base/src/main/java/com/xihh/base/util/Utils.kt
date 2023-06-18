package com.xihh.base.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Size
import android.util.SizeF
import org.json.JSONObject

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

fun Long.toFormatString(): String {
    val sb = StringBuilder()
    var temp = this
    while (temp > 1000) {
        sb.insert(0, String.format(",%03d", temp % 1000))
        temp /= 1000
    }
    sb.insert(0, temp)

    return sb.toString()
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
