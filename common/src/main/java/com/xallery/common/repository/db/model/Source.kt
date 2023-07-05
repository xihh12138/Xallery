package com.xallery.common.repository.db.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.bumptech.glide.signature.ObjectKey
import com.xallery.common.repository.constant.Constant
import com.xihh.base.util.getUri

@Entity
data class Source(
    @PrimaryKey val id: Long,
    val mimeType: String?,
    val path: String? = null, // best effort to get local path
    val sizeBytes: Long,
    val takenTimestamp: Long,
    val addTimestamp: Long,
    val modifiedTimestamp: Long,
    val name: String? = null,
    val album: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    var lat: Double? = null,
    var lng: Double? = null,
    val durationMillis: Long? = null,
) : Parcelable {

    @Ignore val uri: Uri = getUri(mimeType, id) // content or file URI

    @Ignore
    val key = ObjectKey(id)

    @Ignore
    val isImage = mimeType?.startsWith(Constant.MimeType.IMAGE_START)

    @Ignore
    val isVideo = mimeType?.startsWith(Constant.MimeType.VIDEO_START)

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(mimeType)
        parcel.writeString(path)
        parcel.writeLong(sizeBytes)
        parcel.writeLong(takenTimestamp)
        parcel.writeLong(addTimestamp)
        parcel.writeLong(modifiedTimestamp)
        parcel.writeString(name)
        parcel.writeString(album)
        parcel.writeValue(width)
        parcel.writeValue(height)
        parcel.writeValue(lat)
        parcel.writeValue(lng)
        parcel.writeValue(durationMillis)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Source> {
        override fun createFromParcel(parcel: Parcel): Source {
            return Source(parcel)
        }

        override fun newArray(size: Int): Array<Source?> {
            return arrayOfNulls(size)
        }
    }
}