package com.xihh.base.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.xihh.base.android.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.*


object FileUtil : IFileUtil {

    const val DIR_TYPE_EXTERNAL = 0
    const val DIR_TYPE_EXTERNAL_CACHE = 1
    const val DIR_TYPE_PRIVATE_CACHE = 2

    fun getCacheDir(context: Context, docName: String, type: Int = DIR_TYPE_PRIVATE_CACHE): File {
        val dir = when (type) {
            DIR_TYPE_EXTERNAL -> context.getExternalFilesDir(docName)
                ?: File(context.externalCacheDir, docName)

            DIR_TYPE_EXTERNAL_CACHE -> File(context.externalCacheDir, docName)
            else -> File(context.cacheDir, docName)
        }
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getFileFromCache(context: Context, cacheType: String, fileName: String): File {
        return File(getCacheDir(context, cacheType), fileName)
    }

    /**
     * 将ResponseBody的二进制流保存到cache文件夹里
     * @return 保存到的文件类[File]，如果发生异常返回null
     **/
    suspend fun saveFileToCache(
        context: Context,
        cacheType: String,
        fileName: String,
        responseBody: ResponseBody,
        dirType: Int = DIR_TYPE_PRIVATE_CACHE,
    ): File? {
        val contentType = responseBody.contentType()
        val contentLength = responseBody.contentLength()

        logx { "FileUtil:  saveFileToCache  contentType=${contentType?.toString()} contentLength=$contentLength" }

        return withContext(Dispatchers.IO) {
            File(getCacheDir(context, cacheType, dirType), fileName).let {
                lateinit var ins: InputStream
                lateinit var ous: FileOutputStream
                try {
                    ins = responseBody.byteStream()
                    ous = FileOutputStream(it)
                    val buffer = ByteArray(8192)
                    var len: Int
                    while (ins.read(buffer).also { len = it } != -1) {
                        logx { "read $len bytes.." }
                        ous.write(buffer, 0, len)
                    }
                    ous.flush()
                    it
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    ous.close()
                    ins.close()
                }
            }
        }
    }

    suspend fun saveFileFromUri(
        context: Context,
        fileUri: Uri,
        targetDir: File? = null,
        namePrefix: String? = null
    ): File {
        return withContext(context = Dispatchers.IO) {
            // ---------- 获取文件名字 ----------
            val documentFile = DocumentFile.fromSingleUri(context, fileUri)
                ?: throw NullPointerException("fileName for given input Uri is null")
            val fileName = (namePrefix ?: "") + documentFile.name
            // ---------- 创建新文件 ----------
            val outputFile = File(targetDir ?: context.cacheDir, fileName)
            if (outputFile.exists()) {
                outputFile.delete()
            }
            outputFile.createNewFile()
            // ---------- 把从文件选择器里拿到的文件复制到自己的缓存文件夹里 ----------
            copyFile(context, fileUri, outputFile)
            return@withContext outputFile
        }
    }

    private suspend fun copyFile(context: Context, inputUri: Uri, outputFile: File) =
        withContext(Dispatchers.IO) {
            val fos = FileOutputStream(outputFile)
            var fis: InputStream? = null
            try {
                fis = context.contentResolver.openInputStream(inputUri)
                fis?.copyTo(fos)
            } catch (e: Exception) {
                logf { "FileUtil: copyFile   e=${e.stackTraceToString()}" }
            } finally {
                fos.close()
                fis?.close()
            }
        }

    suspend fun getFileLengthFromUri(context: Context, uri: Uri): Long? {
        return when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                File(uri.path).length()
            }

            ContentResolver.SCHEME_CONTENT -> {
                try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.statSize
                } catch (e: Exception) {
                    logx { e.stackTraceToString() }
                    null
                }
            }

            else -> null
        }
    }

    fun getLatestFileFolders(num: Int): LinkedHashSet<String> {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
        val parents = LinkedHashSet<String>()
        var cursor: Cursor? = null
        try {
            if (isVersionGreater(Build.VERSION_CODES.R)) {
                val bundle = Bundle().apply {
                    putInt(ContentResolver.QUERY_ARG_LIMIT, num)
                    putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(BaseColumns._ID))
                    putInt(
                        ContentResolver.QUERY_ARG_SORT_DIRECTION,
                        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
                }

                cursor = appContext.contentResolver.query(uri, projection, bundle, null)
                if (cursor?.moveToFirst() == true) {
                    do {
                        val path =
                            cursor.getStringValue(MediaStore.Images.ImageColumns.DATA) ?: continue
                        parents.add(path.getParentPath())
                    } while (cursor.moveToNext())
                }
            } else {
                val sorting = "${BaseColumns._ID} DESC LIMIT $num"
                cursor = appContext.contentResolver.query(uri, projection, null, null, sorting)
                if (cursor?.moveToFirst() == true) {
                    do {
                        val path =
                            cursor.getStringValue(MediaStore.Images.ImageColumns.DATA) ?: continue
                        parents.add(path.getParentPath())
                    } while (cursor.moveToNext())
                }
            }
        } catch (e: Exception) {
            logx { "getLatestFileFolders: e=${e.stackTraceToString()}" }
        } finally {
            cursor?.close()
        }

        return parents
    }

    /**
     * 复制私有目录的文件到公有目录
     * @param context 上下文
     * @param file 私有目录的文件路径
     * @return 公有目录的uri，为空则代表复制失败
     */
    suspend fun copyFileToPublicDir(
        context: Context,
        file: String,
        name: String,
        dirName: String,
    ): Uri? {
        val mediaInfo = MediaInfo.fromPath(file)

        return Api29Impl.copyFileToMediaDir(context, file, name, dirName, mediaInfo)
            ?: copyFileToMediaDir(context, file, name, dirName, mediaInfo)
    }

    /**
     * 复制私有目录的文件到公有目录
     * @param context 上下文
     * @param file 私有目录的文件路径
     * @return 公有目录的uri，为空则代表复制失败
     */
    override suspend fun copyFileToMediaDir(
        context: Context,
        file: String,
        name: String,
        dirName: String,
        mediaInfo: MediaInfo,
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            Environment.getExternalStoragePublicDirectory(mediaInfo.externalDir)?.let {
                val dir = File(it, dirName)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val file = File(file)
                val extension = mediaInfo.extension ?: ""
                var target = File(dir, "$name.$extension")
                var i = 1
                while (target.exists()) {
                    target = File(target.parent, "$name ($i).$extension")
                    i++
                }
                file.copyTo(target)
                val uri = Uri.fromFile(target)
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                return@withContext uri
            }
        } catch (e: Exception) {
            logf { "FileUtil: copyFileToMediaDir   e=${e.stackTraceToString()}" }
        }

        return@withContext null
    }

    object Api29Impl : IFileUtil {
        override suspend fun copyFileToMediaDir(
            context: Context,
            file: String,
            name: String,
            dirName: String,
            mediaInfo: MediaInfo,
        ): Uri? = withContext(Dispatchers.IO) {
            try {
                mediaInfo.externalUri ?: return@withContext null

                val oldFile = File(file)
                //设置目标文件的信息
                val values = ContentValues()
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, name)
                values.put(MediaStore.Files.FileColumns.TITLE, name)
                values.put(MediaStore.Files.FileColumns.MIME_TYPE, mediaInfo.mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val relativePath = "${mediaInfo.externalDir}${File.separator}${dirName}"
                    values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, relativePath)
                }
                val resolver = context.contentResolver
                val insertUri = resolver.insert(mediaInfo.externalUri, values)
                if (insertUri != null) {
                    val fis = FileInputStream(oldFile)
                    var fos: OutputStream? = null
                    try {
                        fos = resolver.openOutputStream(insertUri)
                        if (fos != null) {
                            fis.copyTo(fos)
                        }
                    } catch (e: Exception) {
                        logx { "FileUtil(Api29Impl): copyFileToMediaDir   e=${e.stackTraceToString()}" }
                    } finally {
                        fis.close()
                        fos?.close()
                    }
                    return@withContext insertUri
                } else {
                    logx { "FileUtil(Api29Impl): copyFileToMediaDir   insertUri==null" }
                }
            } catch (e: Exception) {
                logf { "FileUtil(Api29Impl): copyFileToMediaDir   e=${e.stackTraceToString()}" }
            }
            return@withContext null
        }
    }
}

data class MediaInfo(
    val mimeType: String?,
    val externalDir: String,
    val externalUri: Uri?,
) {
    val extension: String?
        get() = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

    companion object {
        fun fromPath(path: String): MediaInfo {
            val mimeType = getMimeType(path)

            val externalDir = mapExternalDir(mimeType)
            val externalUri = mapExternalUri(mimeType)

            return MediaInfo(mimeType, externalDir, externalUri)
        }

        private fun mapExternalDir(mimeType: String?) = when {
            mimeType == null -> Environment.DIRECTORY_DOCUMENTS
            mimeType.contains("audio") -> Environment.DIRECTORY_MUSIC
            mimeType.contains("video") -> Environment.DIRECTORY_MOVIES
            mimeType.contains("image") -> Environment.DIRECTORY_PICTURES
            else -> Environment.DIRECTORY_DOWNLOADS
        }

        private fun mapExternalUri(mimeType: String?) = when {
            mimeType == null -> null
            mimeType.contains("audio") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            mimeType.contains("video") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            mimeType.contains("image") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                null
            }
        }
    }
}

interface IFileUtil {
    suspend fun copyFileToMediaDir(
        context: Context,
        file: String,
        name: String,
        dirName: String,
        mediaInfo: MediaInfo,
    ): Uri?
}