package com.xallery.album.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.xallery.album.databinding.ItemPictureFlowBinding
import com.xallery.album.databinding.ItemPictureFlowGroupBinding
import com.xallery.album.repo.PictureFlowViewModel
import com.xihh.base.ui.AntiDitherClicker

class PictureFlowAdapter(
    private val onTitleClick: (PictureFlowViewModel.GroupBean) -> Unit,
    private val onSourceClick: (View, Int, PictureFlowViewModel.SourceBean) -> Unit,
) : RecyclerView.Adapter<PictureFlowAdapter.IViewHolder>() {

    private val differ = AsyncListDiffer(this,
        object : DiffUtil.ItemCallback<PictureFlowViewModel.IItemBean>() {
            override fun areItemsTheSame(
                oldItem: PictureFlowViewModel.IItemBean, newItem: PictureFlowViewModel.IItemBean,
            ): Boolean = oldItem.isItemsTheSame(newItem)

            override fun areContentsTheSame(
                oldItem: PictureFlowViewModel.IItemBean, newItem: PictureFlowViewModel.IItemBean,
            ): Boolean = oldItem.isContentTheSame(newItem)

        })

    fun updateData(data: List<PictureFlowViewModel.IItemBean>) {
        differ.submitList(data)
    }

    override fun getItemViewType(position: Int): Int =
        if (differ.currentList[position] is PictureFlowViewModel.GroupBean) {
            VIEW_TYPE_GROUP
        } else {
            VIEW_TYPE_SOURCE
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == VIEW_TYPE_GROUP) {
            GroupVH(
                ItemPictureFlowGroupBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            SourceVH(
                ItemPictureFlowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

    override fun onBindViewHolder(holder: IViewHolder, position: Int) {
        val position = holder.adapterPosition
        val bean = differ.currentList[position]

        if (bean is PictureFlowViewModel.GroupBean) {
            holder as GroupVH

            holder.tv.text = bean.name
        } else if (bean is PictureFlowViewModel.SourceBean) {
            holder as SourceVH

            holder.iv.loadSource(bean.source)
            holder.iv.setOnClickListener(AntiDitherClicker {
                onSourceClick(holder.iv, position, bean)
            })
        }
    }

    override fun getItemCount() = differ.currentList.size

    class GroupVH(vb: ItemPictureFlowGroupBinding) : IViewHolder(vb.root) {
        val tv = vb.root
    }

    class SourceVH(vb: ItemPictureFlowBinding) : IViewHolder(vb.root) {
        val iv = vb.root
    }

    abstract class IViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val VIEW_TYPE_SOURCE = 0
        const val VIEW_TYPE_GROUP = 1
    }
}