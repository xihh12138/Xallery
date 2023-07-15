package com.xallery.picture.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.Fade
import android.transition.TransitionInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.xallery.common.util.MediaStoreFetcher
import com.xallery.picture.SourceBroadcaster
import com.xallery.picture.databinding.ActivitySourceDetailBinding
import com.xallery.picture.repo.SourceDetailsViewModel
import com.xihh.base.android.BaseActivity
import com.xihh.base.android.appContext
import com.xihh.base.ui.FadedPageTransformer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class SourceDetailActivity : BaseActivity<ActivitySourceDetailBinding>() {

    private val vm by lazy {
        ViewModelProvider(
            this,
            SourceDetailsViewModel.Factory(
                intent.getIntExtra(EXTRA_FILTER_TYPE, MediaStoreFetcher.FilterType.FILTER_ALL),
                intent.getIntExtra(EXTRA_POSITION, 0)
            )
        )[SourceDetailsViewModel::class.java]
    }

    private val pagerAdapter = SourceDetailAdapter()

    private val sourceBroadcaster = SourceBroadcaster(appContext)

    override fun onPrepareAnimation() {
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(com.xihh.base.R.transition.image_shared_element_transition)
        window.enterTransition = Fade().addTarget(vb.root)

        postponeEnterTransition()

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>,
            ) {
                vm.curSourceFlow.value?.let { pair ->
                    sourceBroadcaster.updateSource(pair.first, pair.second)
                }

                if (vb.viewpager.childCount > 0) {
                    sharedElements[names[0]] = vb.viewpager.getChildAt(0)
                }
            }
        })
    }

    override fun initView(savedInstanceState: Bundle?) {
        vb.viewpager.adapter = pagerAdapter
        vb.viewpager.setPageTransformer(FadedPageTransformer())
        vb.viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                vm.updateCurPosition(position)
            }
        })

        lifecycleScope.launch {
            vm.sourceListFlow.collectLatest {
                it ?: return@collectLatest
                pagerAdapter.updateData(it)
                vm.curSourceFlow.value?.second?.let {
                    vb.viewpager.setCurrentItem(it, false)
                    startPostponedEnterTransition()
                }
            }
        }
        sourceBroadcaster.register(lifecycle)
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