package com.xallery.common.repository.constant

object Constant {
    object File {
    }

    object Cache {
    }

    object SPKey {
        const val HAS_ACCESS_STORAGE = "HAS_ACCESS_STORAGE"
        const val COLUMN_COUNT = "COLUMN_COUNT"
        const val IS_SORT_DESC = "IS_SORT_DESC"
        const val SORT_COLUMN = "SORT_COLUMN"
    }

    object MimeType {
        const val IMAGE_START = "image/"
        const val VIDEO_START = "video/"
        const val GIF = "image/gif"
    }

    object Extension {
        val photoExtensions = arrayOf(".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif", ".apng", ".avif")
        val videoExtensions = arrayOf(".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v", ".3gpp")
        val audioExtensions = arrayOf(".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac")
        val rawExtensions = arrayOf(".dng", ".orf", ".nef", ".arw", ".rw2", ".cr2", ".cr3")

        val extensionsSupportingEXIF = arrayOf(".jpg", ".jpeg", ".png", ".webp", ".dng")
    }
}