package com.xihh.base.ui

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class FadedPageTransformer(private val minFactor: Float = 0.3f) : ViewPager2.PageTransformer {

    private val minScale = 0.8f
    private val minAlpha = 0.2f

    override fun transformPage(page: View, offset: Float) {
        with(page) {
            when {
                offset < -1 -> {//[-Infinity,-1)
//                    alpha = 0f
                }
                offset <= 1 -> {//[-1,1]
                    val factor = Math.max(minFactor, 1 - Math.abs(offset))

                    val scale =
                        minScale + Math.pow(factor.toDouble(), 2.0).toFloat() * (1 - minScale)

                    scaleX = scale
                    scaleY = scale

                    alpha = minAlpha + factor * (1 - minAlpha)
                }

                else -> {//(1,+Infinity]
//                    alpha = 0f
                }
            }
        }
    }

}