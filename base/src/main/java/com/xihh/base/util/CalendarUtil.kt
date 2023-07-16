package com.xihh.base.util

import com.xihh.base.R
import com.xihh.base.android.appContext
import java.util.Calendar
import java.util.Locale


fun getCurYearStartCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.set(Calendar.DAY_OF_YEAR, 1)
    return calendar
}

fun getCurDayStartCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar
}

fun Calendar.setToDayStart(timeMills: Long? = null): Calendar {
    timeMills?.let { timeInMillis = timeMills }
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}

fun Calendar.setTimeMills(timeMills: Long): Calendar {
    timeInMillis = timeMills
    return this
}

fun Calendar.getMDString(locale: Locale): String = "${
    getDisplayName(Calendar.MONTH, Calendar.SHORT_STANDALONE, locale)
} ${get(Calendar.DATE)}${appContext.getString(R.string.calendar_day)}"

fun Calendar.getYMDString(locale: Locale): String =
    "${get(Calendar.YEAR)}${appContext.getString(R.string.calendar_year)} ${
        getDisplayName(Calendar.MONTH, Calendar.SHORT_STANDALONE, locale)
    } ${get(Calendar.DATE)}${appContext.getString(R.string.calendar_day)}"

fun Calendar.get24HourHMString(): String =
    "${get(Calendar.HOUR_OF_DAY)}:${get(Calendar.MINUTE)}"

fun Calendar.get12HourHMString(locale: Locale): String =
    "${getDisplayName(Calendar.AM_PM, Calendar.SHORT, locale)} ${
        get(Calendar.HOUR)
    }:${get(Calendar.MINUTE)}"

fun Long.toFormatString(): String {
    val sb = StringBuilder()
    var temp = this
    while (temp > 1000) {
        sb.insert(0, String.format(",%03d", temp % 1000))
        temp /= 1000
    }
    sb.insert(0, temp)

    return sb.toString()
}

fun Long.toHMSString(): String {
    val ans = StringBuilder()
    var temp = this / 1000
    val hour = temp / 3600
    if (hour > 0) {
        ans.append(String.format("%02d", hour))
        ans.append(':')
    }
    temp %= 3600
    val minute = temp / 60
    ans.append(String.format("%02d", minute))
    ans.append(':')
    temp %= 60
    val second = temp
    ans.append(String.format("%02d", second))

    return ans.toString()
}
