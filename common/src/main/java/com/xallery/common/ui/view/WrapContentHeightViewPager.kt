package com.xallery.common.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.xihh.base.util.logx

/**
 * 版权：中国麦风科技有限公司
 * @author Hem
 * @version 1.0.0
 * @创建日期 2023/5/30,20:15
 * @修改日期 2023/5/30
 * @description ---
 */
class WrapContentHeightViewPager: ViewPager {

    private val itemViews: MutableList<View> = mutableListOf()

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

    init {
        super.setAdapter(Adapter())
    }

    private var canSwipe = true

    fun setItems(items: List<View>) {
        itemViews.clear()
        itemViews.addAll(items)
        adapter?.notifyDataSetChanged()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxHeight = 0
        itemViews.forEach { child ->
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val childHeight = child.measuredHeight
            if (childHeight > maxHeight) {
                maxHeight = childHeight
            }
        }
        logx { "item count: ${itemViews.size}, maxHeight: $maxHeight" }

        if (maxHeight > 0) {
            /*val heightMeasureSpec2 = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY)
            super.onMeasure(widthMeasureSpec, heightMeasureSpec2)*/
            setMeasuredDimension(measuredWidth, maxHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }


    }

    fun setCanSwipe(b: Boolean) {
        canSwipe = b
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return super.onTouchEvent(ev) && canSwipe
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev) && canSwipe
    }

    inner class Adapter(): PagerAdapter() {

        override fun getCount(): Int {
            return itemViews.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val child = itemViews.get(position)
            container.addView(child)
            return child
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(itemViews.get(position))
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

    }

    override fun setAdapter(adapter: PagerAdapter?) {
//        super.setAdapter(adapter)
    }
}