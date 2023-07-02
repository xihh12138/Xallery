package com.xallery.picture.ui

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.xallery.common.util.loadUri
import com.xallery.picture.databinding.FragmentPictureDetailsBinding
import com.xallery.picture.repo.PictureDetailsViewModel
import com.xihh.base.android.BaseFragment
import com.xihh.base.util.ImmerseUtil
import com.xihh.base.util.logx
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PictureDetailsFragment :
    BaseFragment<FragmentPictureDetailsBinding, PictureDetailsViewModel>() {

    override fun getViewModel() =
        ViewModelProvider(requireActivity())[PictureDetailsViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?) {
        ImmerseUtil.setStatusBarVisible(requireActivity().window, false)
        vb.image.setOnClickListener {
            parentFragmentManager.popBackStack()
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

        prepareTransition()
    }

    private fun prepareTransition() {
        sharedElementEnterTransition = TransitionInflater.from(
            requireContext()
        ).inflateTransition(com.xihh.base.R.transition.image_shared_element_transition)

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

    override fun onDestroy() {
        super.onDestroy()
        logx { "onDestroy: PictureDetailsFragment" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ImmerseUtil.setStatusBarVisible(requireActivity().window, true)
        logx { "onDestroyView: PictureDetailsFragment" }
    }

    override fun onDetach() {
        super.onDetach()
        logx { "onDetach: PictureDetailsFragment" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logx { "onCreateView: PictureDetailsFragment" }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logx { "onViewCreated: PictureDetailsFragment" }
    }
}