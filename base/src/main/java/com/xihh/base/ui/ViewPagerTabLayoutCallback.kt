package com.xihh.base.ui

import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import java.lang.ref.WeakReference

class ViewPagerTabLayoutCallback(tabLayout: TabLayout) : ViewPager2.OnPageChangeCallback() {

    private val tabWeak = WeakReference(tabLayout)

    private var previousScrollState = ViewPager2.SCROLL_STATE_IDLE
    private var scrollState = ViewPager2.SCROLL_STATE_IDLE

    override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int,
    ) {
        val tabLayout = tabWeak.get() ?: return

        val updateSelectedTabView =
            scrollState != ViewPager2.SCROLL_STATE_SETTLING || previousScrollState == ViewPager2.SCROLL_STATE_DRAGGING
        val updateIndicator =
            !(scrollState == ViewPager2.SCROLL_STATE_SETTLING && previousScrollState == ViewPager2.SCROLL_STATE_IDLE)

        tabLayout.setScrollPosition(
            position,
            positionOffset,
            updateSelectedTabView,
            updateIndicator
        )
    }

    override fun onPageSelected(position: Int) {
        val tabLayout = tabWeak.get() ?: return

        if (tabLayout.selectedTabPosition != position && position < tabLayout.tabCount) {
            val updateIndicator = (scrollState == ViewPager2.SCROLL_STATE_IDLE
                    || (scrollState == ViewPager2.SCROLL_STATE_SETTLING
                    && previousScrollState == ViewPager2.SCROLL_STATE_IDLE))

            tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        previousScrollState = scrollState
        scrollState = state
    }
}