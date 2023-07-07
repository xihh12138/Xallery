package com.xallery.main.ui

import android.graphics.Rect
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.annotation.DrawableRes
import androidx.core.view.marginBottom
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.xallery.album.repo.PictureFlowViewModel
import com.xallery.album.ui.PictureFlowFragment
import com.xihh.base.android.BaseFragment
import com.xihh.base.delegate.NavAction
import com.xihh.base.ui.FadedPageTransformer
import com.xihh.xallery.databinding.FragmentMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainFragment : BaseFragment<FragmentMainBinding, MainViewModel>() {

    private val menuInflater = MainMenuInflater {

    }

    private val pagerAdapter by lazy {
        object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = pageList.size

            override fun createFragment(position: Int): Fragment {
                return pageList[position].fragmentClass.newInstance().apply {
                    arguments = Bundle().apply {
                        putInt(PictureFlowFragment.ARGUMENT_PAGE, position)
                    }
                }
            }
        }
    }

    private val pictureFlowVM by lazy { ViewModelProvider(this)[PictureFlowViewModel::class.java] }

    override fun adaptWindowInsets(insets: Rect) {
        vb.appBar.updatePadding(top = insets.top)
        vb.root.updatePadding(bottom = insets.bottom)
        val new = Rect(insets)
        new.bottom = (vb.indicator.height + vb.indicator.marginBottom)
        super.adaptWindowInsets(new)
    }

    override fun getViewModel() = ViewModelProvider(requireActivity())[MainViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?) {
        // ------------ inflateAppBarMenu ------------
        menuInflater.inflate(vb.appBar)
        // ------------ inflate ViewPager ------------
        vb.viewpager.adapter = pagerAdapter
        vb.viewpager.setPageTransformer(FadedPageTransformer())
        TabLayoutMediator(vb.indicator, vb.viewpager) { tab, pos ->
            tab.setIcon(pageList[pos].iconStringRes)
        }.attach()
        vb.indicator.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                pictureFlowVM.postAction(
                    NavAction(
                        PictureFlowViewModel.USER_ACTION_JUMP_POS,
                        PictureFlowViewModel.USER_ACTION_KEY_REQUEST_CODE to tab.position,
                        PictureFlowViewModel.USER_ACTION_KEY_JUMP_POS to 0
                    )
                )
            }
        })

        lifecycleScope.apply {
            launch {
                vm.mainPageFlow.collectLatest {
                    when (it) {
                        MainViewModel.MAIN_PAGE_ALL -> goPage(0)
                        MainViewModel.MAIN_PAGE_GIF -> goPage(1)
                        MainViewModel.MAIN_PAGE_MOVIE -> goPage(2)
                        MainViewModel.MAIN_PAGE_ELSE -> goPage(3)
                    }
                }
            }
        }

        prepareTransitions()
    }

    private fun goPage(pageIndex: Int) {
        vb.indicator.getTabAt(pageIndex)?.let {
            vb.indicator.selectTab(it)
        }
    }

    /**
     * Prepares the shared element transition to the pager fragment, as well as the other transitions
     * that affect the flow.
     */
    private fun prepareTransitions() {
        exitTransition = TransitionInflater.from(requireContext())
            .inflateTransition(com.xihh.xallery.R.transition.grid_exit_transition_main)

        postponeEnterTransition()

        vb.root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                vb.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                startPostponedEnterTransition()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        pictureFlowVM.imageRecyclerViewPool.clear()
    }

    private data class PageBean(
        val fragmentClass: Class<out Fragment>,
        @DrawableRes val iconStringRes: Int,
    )

    companion object {
        private val pageList = arrayListOf(
            PageBean(
                PictureFlowFragment::class.java,
                com.xallery.common.R.drawable.selector_ic_image_menu,
            ),
            PageBean(
                PictureFlowFragment::class.java,
                com.xallery.common.R.drawable.selector_ic_video_menu,
            ),
            PageBean(
                PictureFlowFragment::class.java,
                com.xallery.common.R.drawable.selector_ic_gif_menu,
            ),
            PageBean(
                PictureFlowFragment::class.java,
                android.R.drawable.ic_menu_gallery,
            ),
        )
    }
}