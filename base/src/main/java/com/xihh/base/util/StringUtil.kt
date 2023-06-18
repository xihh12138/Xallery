package com.xihh.base.util

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)

fun String.getFilenameExtension() = substring(lastIndexOf(".") + 1)

fun String.getParentPath() = removeSuffix("/${getFilenameFromPath()}")