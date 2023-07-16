package com.xallery.picture.repo.bean

import com.xallery.common.repository.db.model.Source

data class SourcePageInfo(
    val source: Source,
    val position: Int,
    val listSum: Int
)
