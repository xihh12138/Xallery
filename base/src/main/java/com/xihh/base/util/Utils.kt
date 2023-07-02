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
import android.webkit.MimeTypeMap
import org.json.JSONObject
import java.util.Calendar
import java.util.regex.Pattern

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

fun getCurDayStartCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar
}

fun Calendar.setToDayStart(timeMills: Long? = null): Calendar {
    timeMills?.let { timeInMillis = timeMills }
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}

fun Long.toHMSString(): String {
    val ans = StringBuilder()
    var temp = this / 1000
    val hour = temp / 3600
    if (hour > 0) {
        ans.append(String.format("%02d", hour))
        ans.append(':')
    }
    temp %= 3600
    val minute = temp / 60
    ans.append(String.format("%02d", minute))
    ans.append(':')
    temp %= 60
    val second = temp
    ans.append(String.format("%02d", second))

    return ans.toString()
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

fun String?.extractInt(defaultValue: Int = 0): Int {
    if (this == null) return defaultValue
    val matcher = Pattern.compile("-?\\d+").matcher(this)
    while (matcher.find()) {
        return matcher.group(0)?.toIntOrNull() ?: defaultValue
    }
    return defaultValue
}

fun String?.extractFloat(defaultValue: Float): Float {
    if (this == null) return defaultValue
    val matcher = Pattern.compile("-?\\d+.\\d+").matcher(this)
    while (matcher.find()) {
        return matcher.group(0)?.toFloatOrNull() ?: defaultValue
    }
    return defaultValue
}