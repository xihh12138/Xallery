package com.xallery.picture.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.xallery.common.reposity.db.model.Source
import com.xallery.common.util.loadUri
import com.xallery.picture.databinding.ActivitySourceDetailBinding
import com.xallery.picture.repo.PictureDetailsViewModel
import com.xihh.base.android.BaseActivity
import com.xihh.base.util.ImmerseUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SourceDetailActivity : BaseActivity<ActivitySourceDetailBinding>() {

    private val vm by lazy { ViewModelProvider(this)[PictureDetailsViewModel::class.java] }

    override fun initView(savedInstanceState: Bundle?) {
        ImmerseUtil.setStatusBarVisible(window, false)
        vb.image.setOnClickListener {
            finish()
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
        intent.getParcelableExtra<Source>("source")?.let {
            vm.updateCurSource(it, 0)
        }

        prepareTransition()
    }

    private fun prepareTransition() {
        overridePendingTransition(0, 0)
//        sharedElementEnterTransition = TransitionInflater.from(
//            requireContext()
//        ).inflateTransition(com.xihh.base.R.transition.image_shared_element_transition)

        postponeEnterTransition()

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                sharedElements[names[0]] = vb.image
            }
        })
    }

    class Launcher : ActivityResultContract<Unit, Int>() {

        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(context, SourceDetailActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Int {
            return resultCode
        }
    }
}