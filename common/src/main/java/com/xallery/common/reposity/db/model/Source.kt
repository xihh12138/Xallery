package com.xallery.common.reposity.db.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bumptech.glide.signature.ObjectKey
import com.xallery.common.reposity.constant.Constant

@Entity
data class Source(
    @PrimaryKey val id: Long,
    val uri: Uri, // content or file URI
    val path: String? = null, // best effort to get local path
    val mimeType: String,
    val sizeBytes: Long,
    val takenTimestamp: Long,
    val addTimestamp: Long,
    val modifiedTimestamp: Long,
    val name: String? = null,
    val album: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val durationMillis: Long? = null,
) {

    val key = ObjectKey(id)

    val isImage = mimeType.startsWith(Constant.MimeType.IMAGE_START)

    val isVideo = mimeType.startsWith(Constant.MimeType.VIDEO_START)
}