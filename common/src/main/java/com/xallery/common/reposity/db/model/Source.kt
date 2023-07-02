package com.xallery.common.reposity.db.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bumptech.glide.signature.ObjectKey
import com.xallery.common.reposity.constant.Constant

@Entity
data class Source(
    @PrimaryKey val id: Long,
    val uri: Uri, // content or file URI
    val path: String? = null, // best effort to get local path
    val mimeType: String?,
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
) : Parcelable {

    val key = ObjectKey(id)

    val isImage = mimeType?.startsWith(Constant.MimeType.IMAGE_START)

    val isVideo = mimeType?.startsWith(Constant.MimeType.VIDEO_START)

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readParcelable(Uri::class.java.classLoader)!!,
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
        parcel.writeParcelable(uri, flags)
        parcel.writeString(path)
        parcel.writeString(mimeType)
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