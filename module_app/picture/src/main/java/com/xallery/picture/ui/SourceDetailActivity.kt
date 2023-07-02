package com.xallery.picture.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.xallery.common.reposity.db.model.Source
import com.xallery.common.util.loadUri
import com.xallery.picture.databinding.ActivitySourceDetailBinding
import com.xallery.picture.repo.PictureDetailsViewModel
import com.xihh.base.android.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SourceDetailActivity : BaseActivity<ActivitySourceDetailBinding>() {

    private val vm by lazy { ViewModelProvider(this)[PictureDetailsViewModel::class.java] }

    override fun onPrepareAnimation() {
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(com.xihh.base.R.transition.image_shared_element_transition)

        postponeEnterTransition()

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>,
            ) {
                sharedElements[names[0]] = vb.image
            }
        })
    }

    override fun initView(savedInstanceState: Bundle?) {
//        ImmerseUtil.setStatusBarVisible(window, false)
        vb.image.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        lifecycleScope.launch {
            vm.curSourceFlow.collectLatest {
                val source = it?.first ?: return@collectLatest
                vb.image.loadUri(source.uri, source.key, false, false) {
                    vb.image.transitionName = source.id.toString()
                    startPostponedEnterTransition()
                }
            }
        }
        intent.getParcelableExtra<Source>(EXTRA_SOURCE)?.let {
            vm.updateCurSource(it, 0)
        }
    }

    class TransitionLauncher : ActivityResultContract<Pair<Source, View>, Int>() {

        override fun createIntent(context: Context, input: Pair<Source, View>): Intent {
            return Intent(context, SourceDetailActivity::class.java)
                .putExtra(
                    ActivityResultContracts.StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        context as Activity, input.second, input.second.transitionName
                    ).toBundle()
                ).putExtra(EXTRA_SOURCE, input.first)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Int {
            return resultCode
        }
    }

    companion object {
        const val EXTRA_SOURCE = "EXTRA_SOURCE"
    }
}