package com.xihh.base.util

import android.database.Cursor


fun Cursor.getStringValue(key: String) = getString(getColumnIndex(key))

fun Cursor.getStringValueNullable(key: String) = if (isNull(getColumnIndex(key))) null else getString(getColumnIndex(key))

fun Cursor.getIntValue(key: String) = getInt(getColumnIndex(key))

fun Cursor.getIntValueNullable(key: String) = if (isNull(getColumnIndex(key))) null else getInt(getColumnIndex(key))

fun Cursor.getLongValue(key: String) = getLong(getColumnIndex(key))

fun Cursor.getLongValueNullable(key: String) = if (isNull(getColumnIndex(key))) null else getLong(getColumnIndex(key))

fun Cursor.getBlobValue(key: String) = getBlob(getColumnIndex(key))

fun Cursor.getBlobValueNullable(key: String) = if (isNull(getColumnIndex(key))) null else getBlob(getColumnIndex(key))
