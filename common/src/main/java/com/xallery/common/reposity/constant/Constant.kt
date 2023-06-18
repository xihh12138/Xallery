package com.xallery.common.reposity.constant

object Constant {
    object File {
    }

    object Cache {
    }

    object SPKey {
        const val HAS_ACCESS_STORAGE = "HAS_ACCESS_STORAGE"
        const val COLUMN_COUNT = "COLUMN_COUNT"
    }

    object FileType {
        // media types
        const val IMAGES = 1
        const val VIDEOS = 1 shl 1
        const val GIFS = 1 shl 2
        const val RAWS = 1 shl 3
        const val SVGS = 1 shl 4
        const val PORTRAITS = 1 shl 5
    }

    object MimeType {
        const val IMAGE = "image/"
        const val VIDEO = "video/"
    }

    object Extension {
        val photoExtensions = arrayOf(".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif", ".apng", ".avif")
        val videoExtensions = arrayOf(".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v", ".3gpp")
        val audioExtensions = arrayOf(".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac")
        val rawExtensions = arrayOf(".dng", ".orf", ".nef", ".arw", ".rw2", ".cr2", ".cr3")

        val extensionsSupportingEXIF = arrayOf(".jpg", ".jpeg", ".png", ".webp", ".dng")
    }
}