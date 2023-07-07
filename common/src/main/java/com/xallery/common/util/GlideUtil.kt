package com.xallery.common.util

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target


fun ImageView.loadUri(
    uri: Uri,
    signature: Key? = null,
    centerCrop: Boolean = true,
    getThumbnail: Boolean = true,
    onLoadFinish: ((loadSuccess: Boolean) -> Unit)? = null,
) {
    val options = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .priority(Priority.LOW)
        .format(DecodeFormat.PREFER_ARGB_8888)

    if (getThumbnail) {
        options.encodeQuality(thumbnailQuality)
    } else {
        options.encodeQuality(100)
    }

    if (signature != null) {
        options.signature(signature)
    }
    if (centerCrop) {
        options.centerCrop()
    }

    Glide.with(this)
        .load(uri)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean,
            ): Boolean {
                onLoadFinish?.invoke(false)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean,
            ): Boolean {
                onLoadFinish?.invoke(true)
                return false
            }
        })
        .apply(options)
        .into(this)
}

fun ImageView.loadPath(
    path: String,
    signature: Key? = null,
    centerCrop: Boolean = true,
) {
    val options = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .priority(Priority.LOW)
        .format(DecodeFormat.PREFER_ARGB_8888)

    if (signature != null) {
        options.signature(signature)
    }
    if (centerCrop) {
        options.centerCrop()
    }

    Glide.with(context)
        .load(path)
        .apply(options)
        .into(this)
}

var thumbnailQuality = 50