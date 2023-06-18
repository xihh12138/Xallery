package com.xallery.common.util

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions


fun ImageView.loadUri(
    uri: Uri,
    signature: Key? = null
) {
    val options = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .priority(Priority.LOW)
        .format(DecodeFormat.PREFER_ARGB_8888)

    if (signature != null) {
        options.signature(signature)
    }

    Glide.with(context)
        .load(uri)
        .apply(options)
        .into(this)
}