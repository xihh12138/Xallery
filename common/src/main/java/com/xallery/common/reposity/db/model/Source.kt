package com.xallery.common.reposity.db.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bumptech.glide.signature.ObjectKey

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
    val artist: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val durationMillis: Long? = null,
    val sourceRotationDegrees: Int? = null,
){


    val key = ObjectKey(id)
}