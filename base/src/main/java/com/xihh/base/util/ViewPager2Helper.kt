package com.xihh.base.util

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.viewpager2.widget.ViewPager2
import java.lang.ref.WeakReference

class ViewPager2Helper(viewPager2: ViewPager2) {

    private val viewPager2Ref = WeakReference(viewPager2)

    private val getRecyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")

    init {
        getRecyclerViewField.isAccessible = true
    }

    val recyclerView: RecyclerView by lazy { getRecyclerViewField.get(viewPager2Ref.get()) as RecyclerView }

    val layoutManager: LayoutManager? get() = recyclerView.layoutManager
}