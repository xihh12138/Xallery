package com.xallery.picture.ui

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xallery.common.util.loadUri
import com.xallery.picture.databinding.FragmentPictureDetailsBinding
import com.xallery.picture.repo.PictureDetailsViewModel
import com.xihh.base.android.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PictureDetailsFragment :
    BaseFragment<FragmentPictureDetailsBinding, PictureDetailsViewModel>() {

    override fun getViewModel() =
        ViewModelProvider(requireActivity())[PictureDetailsViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?) {
        vb.image.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.curSourceFlow.collectLatest {
                    it ?: return@collectLatest
                    vb.image.loadUri(it.uri, it.key, false, false)
                }
            }
        }
    }
}