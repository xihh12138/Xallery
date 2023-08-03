package com.xallery.album.ui

import android.os.Bundle
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.xallery.album.databinding.FragmentPictureFlowBinding
import com.xallery.album.repo.PictureFlowViewModel
import com.xallery.common.repository.db.model.Source
import com.xallery.common.util.SourceDBReadyBroadcaster
import com.xallery.picture.ui.SourceDetailActivity
import com.xihh.base.android.BaseFragment
import com.xihh.base.ui.DampEdgeEffectFactory
import com.xihh.base.util.isPositionFullVisible
import com.xihh.base.util.logx
import com.xihh.base.util.scrollToFullVisible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class PictureFlowFragment : BaseFragment<FragmentPictureFlowBinding, PictureFlowViewModel>() {

    private val sourceDetailLauncher =
        registerForActivityResult(SourceDetailActivity.TransitionLauncher()) {

        }

    private var page: Int = 0

    private val adapter = PictureFlowAdapter(onTitleClick = {

    }, onSourceClick = { view, pos, bean ->
        showPictureDetailWithTransition(view, pos, bean.source)
    })

    private lateinit var sourceDBReadyBroadcaster: SourceDBReadyBroadcaster

    override fun getViewModel() =
        ViewModelProvider(requireParentFragment())[PictureFlowViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?) {
        page = arguments?.getInt(ARGUMENT_PAGE, page) ?: 0
        sourceDBReadyBroadcaster = object : SourceDBReadyBroadcaster(requireContext()) {
            override fun onSourceDBReady(oldCount: Int, newCount: Int) {
                if (oldCount != newCount) {
                    vm.refreshSourceList(page, true)
                }
            }
        }
        sourceDBReadyBroadcaster.register(lifecycle)
        initRV()

        lifecycleScope.apply {
            launch {
                vm.refreshSourceList(page, true)
                vm.dataFlow.collectLatest {
                    if (it?.first == page) {
                        it.second?.let { it1 ->
                            adapter.updateData(it1)
                        }
                    }
                }
            }
            launch {
                vm.userActionFlow.collectLatest {
                    val extra = it.extras ?: return@collectLatest
                    if (extra[PictureFlowViewModel.USER_ACTION_KEY_REQUEST_CODE] != page) {
                        return@collectLatest
                    }
                    when (it.flag) {
                        PictureFlowViewModel.USER_ACTION_JUMP_POS -> {
                            vb.rv.scrollToFullVisible(
                                extra[PictureFlowViewModel.USER_ACTION_KEY_JUMP_POS] as Int, true
                            )
                        }

                        PictureFlowViewModel.USER_ACTION_REFRESH -> {
                            vm.refreshSourceList(page, false)
                        }
                    }
                }
            }
        }
    }

    private fun initRV() {
        val count =
            requireContext().resources.getInteger(com.xallery.common.R.integer.album_column_count)
        vb.rv.layoutManager = GridLayoutManager(requireContext(), count).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int) = when (adapter.getItemViewType(position)) {
                    PictureFlowAdapter.VIEW_TYPE_GROUP -> count
                    else -> 1
                }
            }
        }
        vb.rv.adapter = adapter
        vb.rv.setItemViewCacheSize(count * 2)
        vb.rv.setHasFixedSize(true)
        vb.rv.setRecycledViewPool(vm.imageRecyclerViewPool)
        vb.rv.edgeEffectFactory = DampEdgeEffectFactory()
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
    }

    private fun showPictureDetailWithTransition(view: View, position: Int, source: Source) {
//        (exitTransition as TransitionSet).excludeTarget(view, true)
        view.transitionName = source.id.toString()
        vm.curOriginPosition = position
//        parentFragmentManager.beginTransaction()
//            .setReorderingAllowed(true)
//            .addSharedElement(view, view.transitionName)
//            .replace(
//                com.xallery.common.R.id.root_container,
//                PictureDetailsFragment::class.java,
//                null,
//                PictureDetailsFragment::class.simpleName
//            )
//            .addToBackStack(PictureDetailsFragment::class.simpleName)
//            .commit()
//        lifecycleScope.launch {
//            (sourceDetailLauncher.get(
//                mapOf(
//                    "view" to WeakReference(view),
//                    "position" to position,
//                    "filterType" to PictureFlowViewModel.filterTypeMap[page]
//                )
//            )?.get("originPosition") as? Int)?.let {
//                vm.curPosition = adapter.convertToCurPosition(it)
//            }
//        }
        sourceDetailLauncher.launch(
            mapOf(
                "view" to WeakReference(view),
                "position" to position,
                "filterType" to PictureFlowViewModel.filterTypeMap[page]
            )
        )
//        getRouter(requireActivity()).addActionNow(
//            NavAction(
//                RouteViewModel.ROUTE_FLAG_PICTURE,
//                "view" to WeakReference(view),
//                "position" to position,
//                "filterType" to PictureFlowViewModel.filterTypeMap[page]
//            ) /*{
//                (it?.get("originPosition") as? Int)?.let {
//                    vm.curPosition = adapter.convertToCurPosition(it)
//                }
//            }*/
//        )
    }

    /**
     * Prepares the shared element transition to the pager fragment, as well as the other transitions
     * that affect the flow.
     */
    private fun prepareTransitions() {
        logx { "PictureFlowFragment:prepareTransitions  postponeEnterTransition" }
        requireActivity().postponeEnterTransition()

        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>, sharedElements: MutableMap<String, View>,
            ) {
                // Locate the ViewHolder for the clicked position.
                val pos = adapter.convertToCurPosition(vm.curOriginPosition)

                /**
                 * 这里会为空是因为父activity返回时需要获取动画共享元素，然后调用onMapSharedElements()建立共享元素之间的联系。
                 * 但是因为获取的元素在这时候还没有显示在界面上(RecyclerView没有滚动到指定位置)，所以当然获取不到对应的view啦。
                 * 解决方法就是在Fragment.onStart()或者Activity.onReEnter()或Activity.onStart()里调用postponeEnterTransition()，
                 * 这个方法能够使调用者(Fragment或者Activity)延迟启动进入和共享元素转换(也就是延迟回调onMapSharedElements())，直到调用者再次调用
                 * startPostponedEnterTransition()，在这期间，窗口会保持透明。
                 * 注意下面调用的是activity的postponeEnterTransition()和startPostponedEnterTransition()，因为是activity之间跳转动画
                 **/
                val selectedViewHolder = vb.rv.findViewHolderForAdapterPosition(pos)
                logx { "PictureFlowFragment: onMapSharedElements pos=$pos selectedViewHolder=$selectedViewHolder names[0]=${names[0]}" }

                selectedViewHolder ?: return
                // Map the first shared element name to the child ImageView.
                sharedElements[names[0]] = selectedViewHolder.itemView
            }
        })

        // ---------- scroll rv if shareElement is not fully visible----------
        val pos = adapter.convertToCurPosition(vm.curOriginPosition)
        if (vb.rv.isPositionFullVisible(pos)) {
            logx { "PictureFlowFragment:  startPostponedEnterTransition" }
            requireActivity().startPostponedEnterTransition()
        } else {
            vb.rv.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int,
                ) {
                    vb.rv.removeOnLayoutChangeListener(this)
                    logx { "PictureFlowFragment: onLayoutChange   startPostponedEnterTransition" }
                    requireActivity().startPostponedEnterTransition()
                }
            })

            vb.rv.scrollToPosition(pos)
        }
    }

    override fun onStart() {
        super.onStart()

        prepareTransitions()
    }

    companion object {

        const val ARGUMENT_PAGE = "ARGUMENT_PAGE"
    }

}