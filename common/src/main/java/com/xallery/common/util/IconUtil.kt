package com.xallery.common.util

import androidx.annotation.DrawableRes

sealed interface Icon {

    data class LocalResIcon(@DrawableRes val resId: Int) : Icon

    data class UriIcon(
        val uri: String,
        @DrawableRes val placeholderRes: Int? = null,
        @DrawableRes val errorRes: Int? = null
    ) : Icon

}