package com.xallery.main.ui

import android.graphics.Rect
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.xallery.album.ui.PictureFlowFragment
import com.xihh.base.android.BaseFragment
import com.xihh.xallery.R
import com.xihh.xallery.databinding.FragmentMainBinding
import kotlinx.coroutines.flow.collectLatest

class MainFragment : BaseFragment<FragmentMainBinding, MainViewModel>() {

    override fun adaptWindowInsets(insets: Rect) {
        val new = Rect(insets)
        new.bottom += vb.indicator.height
        super.adaptWindowInsets(new)
    }

    private val pagerAdapter by lazy {
        object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = pageList.size

            override fun createFragment(position: Int): Fragment {
                return pageList[position].fragmentClass.newInstance()
            }
        }
    }

    override fun getViewModel() = ViewModelProvider(requireActivity())[MainViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?) {
        // ---------- 禁用滑动手势 ----------
        vb.viewpager.adapter = pagerAdapter
        TabLayoutMediator(vb.indicator, vb.viewpager) { tab, pos ->
            tab.setIcon(pageList[pos].iconStringRes)
        }.attach()

        lifecycleScope.launchWhenResumed {
            vm.mainPageFlow.collectLatest {
                when (it) {
                    MainViewModel.MAIN_PAGE_VOICEOVER -> goPage(0)
                    MainViewModel.MAIN_PAGE_MY_VOICE, MainViewModel.MAIN_PAGE_VOICE_CLONING -> {
                        goPage(1)
                    }

                    MainViewModel.MAIN_PAGE_MINE -> goPage(2)
                }
            }
        }
    }

    private fun goPage(pageIndex: Int) {
        vb.indicator.getTabAt(pageIndex)?.let {
            vb.indicator.selectTab(it)
        }
    }

    private data class PageBean(
        val fragmentClass: Class<out Fragment>,
        @DrawableRes val iconStringRes: Int,
    )

    companion object {
        private val pageList = arrayListOf(
            PageBean(
                PictureFlowFragment::class.java,
                android.R.drawable.ic_menu_gallery,
            ),
            PageBean(
                Fragment::class.java,
                com.xallery.common.R.drawable.ic_play,
            ),
            PageBean(
                Fragment::class.java,
                com.xallery.common.R.drawable.ic_star,
            ),
            PageBean(
                Fragment::class.java,
                com.xallery.common.R.drawable.ic_write_hover,
            ),
        )
    }
}