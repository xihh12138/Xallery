package com.xihh.base.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

object ShareUtil {

    /**
     * 先复制再分享，不然无法指定分享的名字
     **/
    suspend fun startShareFile(
        context: Context,
        authority: String,
        file: File,
        shareFileName: String? = null,
    ): Boolean {
        try {
            val extension = MimeTypeMap.getFileExtensionFromUrl(file.absolutePath)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

            logx { "ShareUtil: startShareFile file=$file extension=$extension mimeType=$mimeType" }

            val shareFile = withContext(Dispatchers.IO) {
                // ---------- 先创建文件夹，如果文件夹已经存在那就清空之前复制的缓存文件 ----------
                val shareFileDir = FileUtil.getCacheDir(context, "share")
                if (shareFileDir.exists()) {
                    shareFileDir.listFiles()?.forEach {
                        it.delete()
                    }
                } else {
                    shareFileDir.mkdirs()
                }

                val shareFile = File(shareFileDir, "$shareFileName.$extension")
                if (shareFile.exists()) {
                    shareFile.delete()
                }

                file.copyTo(shareFile)
                logx { "ShareUtil: startShareFile   复制分享文件成功$shareFile" }
                shareFile
            }

            val uri = FileProvider.getUriForFile(context, authority, shareFile)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                if (shareFileName != null) {
                    putExtra(Intent.EXTRA_TITLE, shareFileName)
                    putExtra(Intent.EXTRA_SUBJECT, shareFileName)
                }

                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share File"))
            logx { "ShareUtil: startShareFile   分享文件成功$uri" }
            return true
        } catch (e: Exception) {
            logf { "ShareUtil: startShareFile  e=${e.stackTraceToString()}" }
            return false
        }
    }

    fun sharePhotoAndText(
        context: Context,
        url: String,
        drawable: Drawable?,
        title: String,
        desc: String,
    ) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"

                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, url)

                drawable?.toBitmap(128, 128)?.let {
                    putExtra(Intent.EXTRA_STREAM, getImageUri(context, it, title, desc))
                }

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share To"))
        } catch (e: Exception) {
            logf { "ShareUtil: shareUrl   e=${e.stackTraceToString()}" }
        }
    }

    private fun getImageUri(
        context: Context,
        bmp: Bitmap,
        title: String,
        desc: String? = null,
    ): Uri {
        val bytes = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, bytes)
        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bmp,
            title,
            desc
        )
        return Uri.parse(path)
    }
}