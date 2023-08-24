package com.xallery.picture.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.transition.Transition
import android.transition.Transition.TransitionListener
import android.transition.TransitionInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.xallery.common.ui.view.XMapView
import com.xallery.common.util.MediaStoreFetcher
import com.xallery.picture.SourceBroadcaster
import com.xallery.picture.databinding.ActivitySourceDetailBinding
import com.xallery.picture.repo.SourceDetailsViewModel
import com.xihh.base.android.BaseActivity
import com.xihh.base.android.appContext
import com.xihh.base.ui.FadedPageTransformer
import com.xihh.base.ui.PictureView
import com.xihh.base.ui.VerticalTwoViewPager
import com.xihh.base.util.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*

class SourceDetailActivity : BaseActivity<ActivitySourceDetailBinding>() {

    private val viewPager2Helper by lazy { ViewPager2Helper(vb.viewpager) }

    private val vm by lazy {
        ViewModelProvider(
            this,
            SourceDetailsViewModel.Factory(
                intent.getIntExtra(
                    EXTRA_FILTER_TYPE, MediaStoreFetcher.FilterType.FILTER_VISUAL_MEDIA
                ),
                intent.getIntExtra(EXTRA_POSITION, 0)
            )
        )[SourceDetailsViewModel::class.java]
    }

    private val reuseReferenceProvider = ReuseReferenceProvider<XMapView>()

    private val pagerAdapter = SourceDetailAdapter(object : PictureView.DragListener {
        override fun onDrag(
            totalDistanceX: Float,
            totalDistanceY: Float,
            totalDistance: Float,
            distanceRatio: Float,
        ) {
            val ratio = Math.min(1f, distanceRatio)
            val alpha = (255 - ratio * 255).toInt()
            vb.root.background.alpha = alpha
            vb.llInfo.alpha = 1 - ratio
        }

        override fun onFinish(finalDistanceRatio: Float) {
            if (finalDistanceRatio > 0.2f) {
                onBackPressedDispatcher.onBackPressed()
            } else {
                vb.root.background.alpha = 255
                vb.llInfo.alpha = 1f
            }
        }

        override fun onFlingCancel() {
            onBackPressedDispatcher.onBackPressed()
        }
    }, object : VerticalTwoViewPager.Listener {
        override fun onPageScroll(totalDistanceY: Float, distanceRatio: Float) {
            val ratio = Math.min(1f, distanceRatio)

            vb.llInfo.alpha = 1 - ratio
        }

        override fun onPageChange(page: Int) {
        }
    }, reuseReferenceProvider)

    private val sourceBroadcaster = SourceBroadcaster(appContext)

    override fun adaptWindowInsets(insets: Rect) {
        vb.llInfo.updatePadding(top = insets.top)
        super.adaptWindowInsets(insets)
    }

    override fun onPrepareAnimation() {
        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(com.xihh.base.R.transition.image_shared_element_transition)
            .addListener(
                object : TransitionListener {
                    override fun onTransitionStart(transition: Transition?) {
                        logx { "SourceDetailActivity: onTransitionStart   " }
                        vb.llInfo.isVisible = false
                    }

                    override fun onTransitionEnd(transition: Transition?) {
                        logx { "SourceDetailActivity: onTransitionEnd   " }
                        vb.llInfo.isVisible = true
                    }

                    override fun onTransitionCancel(transition: Transition?) {
                    }

                    override fun onTransitionPause(transition: Transition?) {
                    }

                    override fun onTransitionResume(transition: Transition?) {
                    }
                })

        postponeEnterTransition()

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>, sharedElements: MutableMap<String, View>,
            ) {
                logx { "SourceDetailActivity: onMapSharedElements   " }
                vm.sourcePageInfoFlow.value?.let { info ->
                    sourceBroadcaster.updateSource(info.source, info.position)
                }

                if (vb.viewpager.childCount > 0) {
                    sharedElements[names[0]] = vb.viewpager.getChildAt(0)
                }
            }
        })
    }

    override fun initView(savedInstanceState: Bundle?) {
        // TODO: 这里有些东西没有搞懂，如果没有下面这行的话再次进入这个activity背景会是透明的，后面有空再研究
        reuseReferenceProvider.init(builder = {
            XMapView(this)
        }, onClear = {
            it.onDetach()
        })
        vb.root.background.alpha = 255
        vb.viewpager.adapter = pagerAdapter
//        viewPager2Helper.disablePrefetch()
        vb.viewpager.setPageTransformer(FadedPageTransformer())
//        vb.viewpager.let {
//            (it.javaClass.getDeclaredField("mRecyclerView").apply {
//                isAccessible = true
//            }.get(it) as RecyclerView).edgeEffectFactory = DampEdgeEffectFactory()
//        }
        vb.viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                vm.updateCurPosition(position).invokeOnCompletion {
                    updateInfoVIew(position)
                }
            }
        })

        lifecycleScope.launch {
            vm.sourceListFlow.collectLatest {
                it ?: return@collectLatest
                pagerAdapter.updateData(it)
                vm.sourcePageInfoFlow.value?.let {
                    vb.viewpager.setCurrentItem(it.position, false)
                    startPostponedEnterTransition()
                }
            }
        }

        sourceBroadcaster.register(lifecycle)
    }

    private fun updateInfoVIew(position: Int) {
        logx { "SourceDetailActivity: updateInfoVIew   position=$position" }
        val source = vm.sourcePageInfoFlow.value?.source ?: return
        val calendar = Calendar.getInstance().setTimeMills(source.addTimestamp)
        val locale = Locale.getDefault()

        vb.tvInfoName.text = source.name

        StringBuilder().apply {
            append(calendar.getYMDString(locale))
            append(" · ")
            append(calendar.get12HourHMString(locale))

            if (source.width != null && source.height != null) {
                append("\t\t\t ${source.width} × ${source.height}")
            }

            vb.tvInfoTimeAndSize.text = this
        }

        if (source.lat != null && source.lng != null) {
            vb.tvInfoLocation.text = "${source.lat} , ${source.lng}"
            vb.tvInfoLocation.isVisible = true
        } else {
            vb.tvInfoLocation.isVisible = false
        }
    }

    override fun onBackPressed() {
        if (pagerAdapter.curVerticalPage == 0) {
            onBackPressedDispatcher.onBackPressed()
        } else {
            pagerAdapter.getItemViewViewBinding(viewPager2Helper.recyclerView.getChildAt(0)).pager.scrollToFirstPage()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reuseReferenceProvider.clear()
    }

    class TransitionLauncher : ActivityResultContract<Map<String, Any>, Map<String, Any>?>() {

        override fun createIntent(context: Context, input: Map<String, Any>): Intent {
            val filterType = input["filterType"] as Int
            val position = input["position"] as Int
            val view = (input["view"] as WeakReference<*>).get() as View
            return Intent(context, SourceDetailActivity::class.java)
                .putExtra(
                    ActivityResultContracts.StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        context as Activity, view, view.transitionName
                    ).toBundle()
                )
                .putExtra(EXTRA_FILTER_TYPE, filterType)
                .putExtra(EXTRA_POSITION, position)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Map<String, Any>? {
            return intent.takeIf { resultCode == RESULT_OK }?.let {
                mapOf("originPosition" to it.getIntExtra(EXTRA_RESULT_POSITION, 0))
            }
        }
    }

    companion object {
        const val EXTRA_FILTER_TYPE = "EXTRA_FILTER_TYPE"
        const val EXTRA_POSITION = "EXTRA_POSITION"

        const val EXTRA_RESULT_POSITION = "EXTRA_RESULT_POSITION"
    }
}