package com.xihh.base.util

import android.annotation.SuppressLint
import android.text.TextUtils
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {


    const val YMD = "yyyy-MM-dd"
    const val YMD_HMS = "yyyy-MM-dd HH:mm:ss"
    const val YMD_HM = "yyyy-MM-dd HH:mm"
    const val HM = "HH:mm"
    const val MD = "MM-dd"
    const val IMAGE_DATE = "yyyy-MM-dd_HH_mm_ss"
    const val MDY = "MM/dd/yyyy"
    val dayOfWeek = arrayOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")

    val YMD_FORMAT: DateFormat = SimpleDateFormat(YMD)
    val YMD_HMS_FORMAT: DateFormat = SimpleDateFormat(YMD_HMS)
    val YMD_HM_FORMAT: DateFormat = SimpleDateFormat(YMD_HM)
    val HM_FORMAT: DateFormat = SimpleDateFormat(HM)
    val MD_FORMAT: DateFormat = SimpleDateFormat(MD)

    const val CONSTANT_SECOND_MS = 1000
    const val CONSTANT_MINUTE_MS = 60 * CONSTANT_SECOND_MS
    const val CONSTANT_HOUR_MS = 60 * CONSTANT_MINUTE_MS


    @SuppressLint("SimpleDateFormat")
    fun getFormat(format: String): DateFormat {
        val df = when (format) {
            YMD -> YMD_FORMAT
            YMD_HMS -> YMD_HMS_FORMAT
            YMD_HM -> YMD_HM_FORMAT
            HM -> HM_FORMAT
            MD -> MD_FORMAT
            else -> SimpleDateFormat(format)
        }
        df.timeZone = TimeZone.getDefault()


        return df

    }


    fun long2Str(timemillis: Long, format: String): String {
        return date2Str(Date(timemillis), format)
    }

    fun long2Str(format: String): String {
        return long2Str(getTimeNow(), format)
    }

    fun date2Str(date: Date?, format: String): String {
        return if (date == null) "" else getFormat(format).format(date)
    }

    fun str2date(str: String, format: String): Date {
        return Date(str2Long(str, format))
    }

    fun str2Long(time: String, format: String): Long {
        val s = getFormat(format)
        try {
            return s.parse(time).time
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return getTimeNow()
    }

    fun str2Long(time: String, format: DateFormat): Long {
        try {
            return format.parse(time).time
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }

    fun getTodayStartTime(): Long {
        //获取当前时间戳
        val time = getTimeNow()
        //一天的时间毫秒数
        val daySecond = 24L * 60 * 60 * 1000
        return time - (time + TimeZone.getDefault().rawOffset) % daySecond
    }

    /**
     * 获取两个时间点所相差的时间戳
     *
     * @param start 开始时间 HH:mm
     * @param end   结束时间 HH:mm
     * @return 时间长度差（s）
     */
    @Throws(Exception::class)
    fun getDifferSS(start: String, end: String): Int {
        val startTime = long2Str(YMD) + " " + start + ":00"
        var endTime: String? = null
        endTime = if (start > end) {
            long2Str(getTimeNow() + 24 * 60 * 60 * 1000, YMD) + " " + end + ":00"
        } else
            long2Str(YMD) + " " + end + ":00"
        return ((str2Long(endTime, YMD_HMS) - str2Long(startTime, YMD_HMS)) / 1000).toInt()
    }


    fun hhmm2Long(hhMM: String): Long {
        if (TextUtils.isEmpty(hhMM)) return 0
        val che = long2Str(YMD) + " " + hhMM + ":00"//当天下班时间
        return str2Long(che, YMD_HMS)
    }


    fun defHHmm(hm: String, defLong: Long): String {
        val s = getFormat(YMD_HM)
        try {
            val mDate = s.parse(long2Str(YMD) + " " + hm)
            mDate.time = mDate.time + defLong
            return date2Str(mDate, HM)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return hm
    }

    private fun addTimeFormat(time: Long): String {
        return if (time > 9) time.toString() else "0$time"
    }

    fun milliseconds2String(milliseconds: Long): String {
        return seconds2String(milliseconds / 1000)
    }

    //秒转成字符
    fun seconds2String(seconds: Long): String {
        val builder = StringBuilder()
        when {
            seconds > 3600 -> {
                builder.append(addTimeFormat(seconds / 3600))
                    .append(":")
                    .append(addTimeFormat(seconds % 3600 / 60))
                    .append(":")
                    .append(addTimeFormat(seconds % 3600 % 60))
            }
            seconds > 60 -> {
                builder.append(addTimeFormat(seconds / 60))
                    .append(":")
                    .append(addTimeFormat(seconds % 60))
            }
            else -> {
                builder.append("00:").append(addTimeFormat(seconds))
            }
        }
        return builder.toString()
    }

    fun getStartTimeOfDay(currentTimeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    fun getStartTimeOfMinute(currentTimeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getDayString(currentTimeMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        return if (dayOfMonth > 9) dayOfMonth.toString() else "0$dayOfMonth"
    }


    fun buildTimeStr(hour: Int, min: Int): String {
        return StringBuilder(if (hour < 10) "0" else "").append(hour.toString()).append(":")
            .append(if (min < 10) "0" else "").append(
                min.toString()
            ).toString()
    }

    /**
     * 获取今天是星期几
     */
    fun getTodayWeek(): Int {
        val c = Calendar.getInstance()
        val t = c.get(Calendar.DAY_OF_WEEK) - 1
        return if (t == 0) {
            7
        } else {
            t
        }
    }

    /**
     * 时分转成 秒
     *
     * @param time HH:mm
     * @return
     */
    fun Hm2Duration(time: String): Long {
        var totalSec: Long = 0
        val my = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (my != null) {
            var multiply = 60
            for (i in my.indices.reversed()) {
                if (!TextUtils.isEmpty(my[i])) {
                    try {
                        totalSec += (Integer.parseInt(my[i]) * multiply).toLong()
                    } catch (e: Exception) {

                    }

                    multiply *= 60
                }
            }
        }
        return totalSec
    }

    fun getTimeNow(): Long {
        return System.currentTimeMillis()
    }

    fun getWeek(timeInMillis: Long): Int {
        val c = Calendar.getInstance()
        c.timeInMillis = timeInMillis
        val week = c.get(Calendar.DAY_OF_WEEK) - 1
//        return week
        return if (week == 0) 7 else week

    }

    /**
     * 计算时间间隔
     * @param duration 时间戳 s
     * @return HH:mm
     */
    fun duration2Time(duration: Int): String {
        var h = 0
        var m = 0
        var s = 0
        val temp: Int = duration % 3600
        if (duration > 3600) {
            h = duration / 3600
            if (temp != 0) {
                if (temp > 60) {
                    m = temp / 60
                    if (temp % 60 != 0) {
                        s = temp / 60
                    }
                } else {
                    s = temp
                }
            }
        } else {
            m = duration / 60
            if (duration % 60 != 0) {
                s = duration % 60
            }
        }
        return if (h > 0) {
            String.format(Locale.CHINA, "%d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    fun longToYMDU(time:Long):String{
        return "${long2Str(time," yyyy/MM/dd")} ${dayOfWeek[getWeek(time) - 1]}"
    }
    @SuppressLint("SimpleDateFormat")
    fun formatTime(time:Long?):String{
        if(time == null)
            return ""
        return if(time> getStartTimeOfDay(System.currentTimeMillis())){
            val sdf = HM_FORMAT
            sdf.format(Date(time)).replace("-","/")
        }else{
            if(Date().year == Date(time).year){
                val sdf = MD_FORMAT
                sdf.format(Date(time)).replace("-","/")
            }else{
                val sdf = SimpleDateFormat("MM/dd/yyyy")
                sdf.format(Date(time))
            }
        }

    }

}