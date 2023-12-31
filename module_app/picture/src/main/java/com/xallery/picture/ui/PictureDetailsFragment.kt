package com.xallery.picture.ui

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.xallery.common.util.loadUri
import com.xallery.picture.databinding.FragmentPictureDetailsBinding
import com.xallery.picture.repo.SourceDetailsViewModel
import com.xihh.base.android.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PictureDetailsFragment :
    BaseFragment<FragmentPictureDetailsBinding, SourceDetailsViewModel>() {

    override fun getViewModel() =
        ViewModelProvider(requireActivity())[SourceDetailsViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?) {
        vb.image.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        lifecycleScope.launch {
            vm.sourcePageInfoFlow.collectLatest {
                val source = it?.source ?: return@collectLatest
//                vb.image.loadUri(source.uri, source.key, false, false) {
//                }
                vb.image.transitionName = source.id.toString()
//                startPostponedEnterTransition()
            }
        }

//        prepareTransition()
    }

    private fun prepareTransition() {
        sharedElementEnterTransition = TransitionInflater.from(
            requireContext()
        ).inflateTransition(com.xihh.base.R.transition.image_shared_element_transition)

        postponeEnterTransition()

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>, sharedElements: MutableMap<String, View>,
            ) {
                sharedElements[names[0]] = vb.image
            }
        })
    }

//    override fun onHiddenChanged(hidden: Boolean) {
//        super.onHiddenChanged(hidden)
//        if (!hidden) {
//            lifecycleScope.launch {
//                vm.curSourceFlow.collectLatest {
//                    val source = it?.first ?: return@collectLatest
//                    vb.image.loadUri(source.uri, source.key, false, false)
//                    vb.image.transitionName = source.id.toString()
//                    startPostponedEnterTransition()
//                }
//            }
//            ImmerseUtil.setStatusBarVisible(requireActivity().window, false)
//        } else {
//            vb.image.setImageDrawable(null)
//            ImmerseUtil.setStatusBarVisible(requireActivity().window, true)
//        }
//    }
}