package com.xallery.album.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xallery.album.databinding.FragmentPictureFlowBinding
import com.xallery.album.databinding.ItemPictureFlowBinding
import com.xallery.album.repo.PictureFlowViewModel
import com.xallery.common.reposity.db.model.Source
import com.xallery.common.util.loadUri
import com.xallery.common.util.toast
import com.xihh.base.android.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PictureFlowFragment : BaseFragment<FragmentPictureFlowBinding, PictureFlowViewModel>() {

    private val adapter = Adapter {
        toast(it.toString())
    }

    override fun adaptWindowInsets(insets: Rect) {
        vb.rv.updatePadding(top = insets.top, bottom = insets.bottom)
        super.adaptWindowInsets(insets)
    }

    override fun getViewModel() = ViewModelProvider(this)[PictureFlowViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?) {
        initRV()
    }

    private fun initRV() {
        val count =
            requireContext().resources.getInteger(com.xallery.common.R.integer.album_column_count)
        vb.rv.layoutManager = GridLayoutManager(requireContext(), count)
        vb.rv.adapter = adapter
        vb.rv.setItemViewCacheSize(count * 2)
        vb.rv.setHasFixedSize(true)

        lifecycleScope.launch {
            vm.fetchPicture(true)
            vm.dataFlow.collectLatest {
                if (it != null) {
                    adapter.updateData(it)
                }
            }
        }
    }

    private class Adapter(private val onClick: (Source) -> Unit) :
        RecyclerView.Adapter<Adapter.VH>() {

        private val differ = AsyncListDiffer(this,
            object : DiffUtil.ItemCallback<Source>() {
                override fun areItemsTheSame(oldItem: Source, newItem: Source): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: Source, newItem: Source): Boolean {
                    return oldItem == newItem
                }
            })

        fun updateData(data: List<Source>) {
            differ.submitList(data)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            ItemPictureFlowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val position = holder.adapterPosition
            val bean = differ.currentList[position]

            holder.iv.loadUri(bean.uri, bean.key)
            holder.iv.setOnClickListener {
                onClick(bean)
            }
        }

        override fun getItemCount() = differ.currentList.size

        class VH(vb: ItemPictureFlowBinding) : RecyclerView.ViewHolder(vb.root) {
            val iv = vb.root
        }
    }
}