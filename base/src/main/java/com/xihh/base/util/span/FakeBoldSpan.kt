package com.xihh.base.util.span

import android.text.TextPaint
import android.text.style.CharacterStyle


class FakeBoldSpan : CharacterStyle() {
    override fun updateDrawState(tp: TextPaint) {
        tp.isFakeBoldText = true
    }
}