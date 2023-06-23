package com.xallery.album.ui

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.xallery.album.databinding.FragmentPictureFlowBinding
import com.xallery.album.repo.PictureFlowViewModel
import com.xallery.common.util.toast
import com.xihh.base.android.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PictureFlowFragment : BaseFragment<FragmentPictureFlowBinding, PictureFlowViewModel>() {

    private var page: Int = 0

    private val adapter = PictureFlowAdapter {
        toast(it.toString())
    }

    override fun getViewModel() =
        ViewModelProvider(requireActivity())[PictureFlowViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?) {
        initRV()
    }

    private fun initRV() {
        page = arguments?.getInt(ARGUMENT_PAGE, page) ?: 0

        val count =
            requireContext().resources.getInteger(com.xallery.common.R.integer.album_column_count)
        vb.rv.layoutManager = GridLayoutManager(requireContext(), count).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int =
                    when (adapter.getItemViewType(position)) {
                        PictureFlowAdapter.VIEW_TYPE_GROUP -> 3
                        else -> 1
                    }
            }
        }
        vb.rv.adapter = adapter
        vb.rv.setItemViewCacheSize(count * 2)
        vb.rv.setHasFixedSize(true)
        vb.rv.setRecycledViewPool(vm.imageRecyclerViewPool)
        vb.rv.addOnScrollListener(object : OnScrollListener() {
            private var hasDisallow = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!hasDisallow && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hasDisallow = true
                    recyclerView.requestDisallowInterceptTouchEvent(true)
                }
                if (!hasDisallow && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hasDisallow = false
                }
            }
        })

        lifecycleScope.launch {
            vm.fetchSource(page, true)
            vm.dataFlow.collectLatest {
                if (it?.first == page) {
                    it.second?.let { it1 ->
                        adapter.updateData(it1)
                    }
                }
            }
        }
    }

    companion object {

        const val ARGUMENT_PAGE = "ARGUMENT_PAGE"
    }

}