package com.xallery.album.repo.bean

import com.xallery.common.reposity.db.model.Source

data class SourceUIInfo(
    val mimeType: String,
    val durationMillis: Long?,
    val location: String?
) {
    companion object {
        fun fromSource(source: Source) = SourceUIInfo(
            mimeType = source.mimeType,
            durationMillis = source.durationMillis,
            location = if (source.lat == null || source.lng == null) null else "${source.lat}${source.lng}"
        )
    }
}