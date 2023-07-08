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
import com.xallery.common.util.MediaStoreFetcher
import com.xallery.picture.databinding.ActivitySourceDetailBinding
import com.xallery.picture.repo.PictureDetailsViewModel
import com.xihh.base.android.BaseActivity
import com.xihh.base.ui.FadedPageTransformer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SourceDetailActivity : BaseActivity<ActivitySourceDetailBinding>() {

    private val vm by lazy {
        ViewModelProvider(
            this,
            PictureDetailsViewModel.Factory(
                intent.getIntExtra(EXTRA_FILTER_TYPE, MediaStoreFetcher.FilterType.FILTER_ALL),
                intent.getIntExtra(EXTRA_POSITION, 0)
            )
        )[PictureDetailsViewModel::class.java]
    }

    private val pagerAdapter = SourceDetailAdapter()

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
//
                if (vb.viewpager.childCount > 0) {
                    sharedElements[names[0]] = vb.viewpager.getChildAt(0)
                }
            }
        })
    }

    override fun initView(savedInstanceState: Bundle?) {
        vb.viewpager.adapter = pagerAdapter
        vb.viewpager.setPageTransformer(FadedPageTransformer())

        lifecycleScope.launch {
//            vm.curSourceFlow.collectLatest {
//                val source = it?.first ?: return@collectLatest
//                vb.image.loadUri(source.uri, source.key, false, false) {
//                    vb.image.transitionName = source.id.toString()
//                    startPostponedEnterTransition()
//                }
//            }
            vm.sourceListFlow.collectLatest {
                it ?: return@collectLatest
                pagerAdapter.updateData(it)
                vm.curSourceFlow.value?.second?.let {
                    vb.viewpager.setCurrentItem(it, false)
                    startPostponedEnterTransition()
                }
            }
        }
    }

    class TransitionLauncher : ActivityResultContract<Pair<Pair<Int, Int>, View>, Int>() {

        override fun createIntent(context: Context, input: Pair<Pair<Int, Int>, View>): Intent {
            return Intent(context, SourceDetailActivity::class.java)
                .putExtra(
                    ActivityResultContracts.StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        context as Activity, input.second, input.second.transitionName
                    ).toBundle()
                )
                .putExtra(EXTRA_FILTER_TYPE, input.first.first)
                .putExtra(EXTRA_POSITION, input.first.second)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Int {
            return resultCode
        }
    }

    companion object {
        const val EXTRA_FILTER_TYPE = "EXTRA_FILTER_TYPE"
        const val EXTRA_POSITION = "EXTRA_POSITION"
    }
}