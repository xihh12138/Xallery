package com.xihh.base.ui

import android.view.View
import com.xihh.base.util.TimeUtils

class AntiDitherClicker(
    private val intervals: Int = 400,
    private val click: (View?) -> Unit,
) : View.OnClickListener {

    override fun onClick(v: View) {
        val stamp = TimeUtils.getTimeNow()
        if (stamp - lastClickStamp > intervals) {
            lastClickStamp = stamp
            click(v)
        }
    }

    companion object {
        @JvmStatic
        private var lastClickStamp = 0L
    }
}