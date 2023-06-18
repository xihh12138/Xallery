package com.xihh.base.util.span

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.*
import android.view.View
import androidx.annotation.ColorInt
import com.xihh.base.util.TimeUtils

fun CharSequence.spannableBuilder(blocker: SpannableStringFactory.SpannableBuilder.() -> Unit): CharSequence {
    return SpannableStringFactory.SpannableBuilder(this).apply(blocker).build()
}

class SpannableStringFactory {

    companion object {

        /**
         * @param 源SpannableString，没有的话就根据matchText new一个
         * @param colorInt R.color.xx
         * 给@param charSequence中的所有数字上色
         */
        fun getColorfulNumberString(
            charSequence: CharSequence, colorInt: Int,
            sourceSpannableString: SpannableString? = null,
        ): SpannableString {
            val spannableString = sourceSpannableString ?: SpannableString(charSequence)
            return spannableString.apply {
                charSequence.mapIndexed { i, c ->
                    if (c in '0'..'9') {
                        setSpan(
                            ForegroundColorSpan(colorInt),
                            i,
                            i + 1,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        }

    }

    class SpannableBuilder(charSequence: CharSequence) {

        private val output = SpannableString(charSequence)

        val length = output.length

        private var flags = SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE

        fun build(): CharSequence {
            return output
        }

        fun setFlags(flag: Int): SpannableBuilder {
            flags = flag
            return this
        }

        fun applyColorfulNumber(@ColorInt colorInt: Int): SpannableBuilder {
            output.apply {
                this.mapIndexed { i, c ->
                    if (c in '0'..'9') {
                        setSpan(ForegroundColorSpan(colorInt), i, i + 1, flags)
                    }
                }
            }
            return this
        }

        fun applyColorfulText(
            @ColorInt colorInt: Int,
            start: Int,
            end: Int,
        ): SpannableBuilder {
            output.apply {
                setSpan(ForegroundColorSpan(colorInt), start, end, flags)
            }
            return this
        }

        fun applyColorfulText(
            @ColorInt colorInt: Int,
            matchText: String? = null,
        ): SpannableBuilder {
            val spannableString = matchText ?: output.toString()
            output.apply {
                var index = this.indexOf(spannableString)
                while (index != -1) {
                    setSpan(
                        ForegroundColorSpan(colorInt),
                        index,
                        index + spannableString.length,
                        flags
                    )
                    index = this.indexOf(spannableString, index + 1)
                }
            }
            return this
        }

        fun applyUnderlineText(matchText: String): SpannableBuilder {
            output.apply {
                var index = this.indexOf(matchText)
                while (index != -1) {
                    setSpan(UnderlineSpan(), index, index + matchText.length, flags)
                    index = this.indexOf(matchText, index + 1)
                }
            }
            return this
        }

        fun applyClickableUnderlineText(
            matchText: String? = null, clickPeriod: Int = 600, onClick: ((widget: View) -> Unit),
        ): SpannableBuilder {
            val spannableString = matchText ?: output.toString()
            output.apply {
                var index = this.indexOf(spannableString)
                while (index != -1) {
                    setSpan(
                        object : ClickableSpan() {

                            private var lastClickStamp = 0L

                            override fun onClick(widget: View) {
                                val stamp = TimeUtils.getTimeNow()
                                if (stamp - lastClickStamp > clickPeriod) {
                                    lastClickStamp = stamp
                                    onClick(widget)
                                }
                            }

                            override fun updateDrawState(ds: TextPaint) {
                                ds.linkColor = ds.color
                                ds.isUnderlineText = true
                            }
                        }, index, index + spannableString.length, flags
                    )
                    index = this.indexOf(spannableString, index + 1)
                }
            }
            return this
        }

        fun applyClickableText(
            matchText: String? = null, clickPeriod: Int = 400, onClick: ((widget: View) -> Unit),
        ): SpannableBuilder {
            val spannableString = matchText ?: output.toString()
            output.apply {
                var index = this.indexOf(spannableString)
                while (index != -1) {
                    setSpan(
                        object : ClickableSpan() {

                            private var lastClickStamp = 0L

                            override fun onClick(widget: View) {
                                val stamp = TimeUtils.getTimeNow()
                                if (stamp - lastClickStamp > clickPeriod) {
                                    lastClickStamp = stamp
                                    onClick(widget)
                                }
                            }

                            override fun updateDrawState(ds: TextPaint) {
                                ds.linkColor = ds.color
                                ds.isUnderlineText = false
                            }
                        }, index, index + spannableString.length, flags
                    )
                    index = this.indexOf(spannableString, index + 1)
                }
            }
            return this
        }

        fun applyAbsoluteSizeText(size: Int, matchText: String? = null): SpannableBuilder {
            return apply(matchText) {
                AbsoluteSizeSpan(size)
            }
        }

        fun applyFakeBoldText(matchText: String? = null): SpannableBuilder {
            val target = matchText ?: output
            output.apply {
                var index = this.indexOf(target.toString())
                while (index != -1) {
                    setSpan(FakeBoldSpan(), index, index + target.length, flags)
                    index = this.indexOf(target.toString(), index + 1)
                }
            }
            return this
        }

        /**
         * @see Typeface
         **/
        fun applyStyleText(style: Int, matchText: String? = null): SpannableBuilder {
            val target = matchText ?: output
            output.apply {
                var index = this.indexOf(target.toString())
                while (index != -1) {
                    setSpan(StyleSpan(style), index, index + target.length, flags)
                    index = this.indexOf(target.toString(), index + 1)
                }
            }
            return this
        }

        /**
         * 用文字替换的方法添加段落符号
         **/
        fun applyBullet(
            @ColorInt colorInt: Int,
            @androidx.annotation.IntRange(from = 0) bulletRadius: Int,
            gapWidth: Int = 0,
            matchText: String,
        ): SpannableBuilder {
            return apply(matchText) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    QuoteSpan(gapWidth, colorInt, bulletRadius)
                } else {
                }
            }
        }

        fun applyDrawable(
            drawable: Drawable,
            matchText: String,
            alignment: Int = DynamicDrawableSpan.ALIGN_CENTER,
        ): SpannableBuilder {
            if (drawable.bounds.width() == 0 || drawable.bounds.height() == 0) {
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            }
            return apply(matchText) {
                ImageSpan(drawable, alignment)
            }
        }

        fun applyDrawable(
            drawable: Drawable,
            index: Int,
            alignment: Int = DynamicDrawableSpan.ALIGN_CENTER,
        ): SpannableBuilder {
            if (drawable.bounds.width() == 0 || drawable.bounds.height() == 0) {
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            }
            output.apply {
                setSpan(ImageSpan(drawable, alignment), index, index + 1, flags)
            }
            return this
        }

        private fun apply(matchText: String?, block: () -> Any): SpannableBuilder {
            val target = matchText ?: output
            output.apply {
                var index = this.indexOf(target.toString())
                while (index != -1) {
                    setSpan(block.invoke(), index, index + target.length, flags)
                    index = this.indexOf(target.toString(), index + 1)
                }
            }
            return this
        }
    }
}