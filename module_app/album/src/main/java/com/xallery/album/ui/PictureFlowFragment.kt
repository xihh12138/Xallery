package com.xallery.album.ui

import android.os.Bundle
import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.xallery.album.R
import com.xallery.album.databinding.FragmentPictureFlowBinding
import com.xallery.album.repo.PictureFlowViewModel
import com.xallery.common.repository.RouteViewModel
import com.xallery.common.repository.db.model.Source
import com.xallery.common.repository.getRouter
import com.xallery.picture.repo.PictureDetailsViewModel
import com.xihh.base.android.BaseFragment
import com.xihh.base.delegate.RouteAction
import com.xihh.base.util.logx
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class PictureFlowFragment : BaseFragment<FragmentPictureFlowBinding, PictureFlowViewModel>() {

    private var page: Int = 0

    private val adapter = PictureFlowAdapter(onTitleClick = {

    }, onSourceClick = { view, pos, bean ->
        showPictureDetailWithTransition(view, pos, bean.source)
    })

    override fun getViewModel() = ViewModelProvider(this)[PictureFlowViewModel::class.java]

    private val detailsVm by lazy { ViewModelProvider(requireActivity())[PictureDetailsViewModel::class.java] }

    override fun initView(savedInstanceState: Bundle?) {
        initRV()

        lifecycleScope.launch {
            vm.getSourceList(page, true)
            vm.dataFlow.collectLatest {
                if (it?.first == page) {
                    it.second?.let { it1 ->
                        adapter.updateData(it1)
                    }
                }
            }
        }

        prepareTransitions()
    }

    private fun initRV() {
        page = arguments?.getInt(ARGUMENT_PAGE, page) ?: 0

        val count =
            requireContext().resources.getInteger(com.xallery.common.R.integer.album_column_count)
        vb.rv.layoutManager = GridLayoutManager(requireContext(), count).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int =
                    when (adapter.getItemViewType(position)) {
                        PictureFlowAdapter.VIEW_TYPE_GROUP -> count
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

        scrollToPositionIfNeed()
    }

    private fun showPictureDetailWithTransition(view: View, position: Int, source: Source) {
        (exitTransition as TransitionSet).excludeTarget(view, true)
        view.transitionName = source.id.toString()
        detailsVm.updateCurSource(source, position)
        getRouter(requireActivity()).addActionNow(
            RouteAction(
                RouteViewModel.ROUTE_FLAG_PICTURE,
                mapOf("view" to WeakReference(view), "source" to source)
            )
        )
    }

    /**
     * Prepares the shared element transition to the pager fragment, as well as the other transitions
     * that affect the flow.
     */
    private fun prepareTransitions() {
        exitTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.grid_exit_transition)

        postponeEnterTransition()

        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>, sharedElements: MutableMap<String, View>
            ) {
                // Locate the ViewHolder for the clicked position.
                val pos = detailsVm.curSourceFlow.value?.second ?: return
                val selectedViewHolder = vb.rv.findViewHolderForAdapterPosition(pos) ?: return

                // Map the first shared element name to the child ImageView.
                sharedElements[names[0]] = selectedViewHolder.itemView
            }
        })
    }

    private fun scrollToPositionIfNeed() {
        vb.rv.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                vb.rv.removeOnLayoutChangeListener(this)
                startPostponedEnterTransition()
                val layoutManager: RecyclerView.LayoutManager = vb.rv.layoutManager ?: return
                val pos = detailsVm.curSourceFlow.value?.second ?: return
                val view = vb.rv.findViewHolderForAdapterPosition(pos)?.itemView
                // Scroll to position if the view for the current position is null (not currently part of
                // layout manager children), or it's not completely visible.
                if (view == null || layoutManager.isViewPartiallyVisible(view, false, true)) {
                    vb.rv.post {
                        vb.rv.scrollToPosition(pos)
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        logx { "onDestroy: PictureFlowFragment" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logx { "onDestroyView: PictureFlowFragment" }
    }

    override fun onDetach() {
        super.onDetach()
        logx { "onDetach: PictureFlowFragment" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logx { "onCreateView: PictureFlowFragment" }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logx { "onViewCreated: PictureFlowFragment" }
    }

    companion object {

        const val ARGUMENT_PAGE = "ARGUMENT_PAGE"
    }

}