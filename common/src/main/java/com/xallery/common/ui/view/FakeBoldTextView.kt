package com.xallery.common.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.xihh.base.util.span.spannableBuilder

class FakeBoldTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text?.spannableBuilder {
            applyFakeBoldText()
        }, type)
    }
}

