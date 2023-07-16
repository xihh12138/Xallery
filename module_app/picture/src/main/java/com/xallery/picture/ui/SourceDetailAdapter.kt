package com.xallery.picture.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xallery.common.repository.db.model.Source
import com.xallery.common.util.loadUri
import com.xallery.picture.databinding.ItemSourceDetailBinding

class SourceDetailAdapter : RecyclerView.Adapter<SourceDetailAdapter.VH>() {

    private var sourceList: List<Source>? = null

    fun updateData(data: List<Source>) {
        sourceList = data

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemSourceDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val position = holder.adapterPosition
        val source = sourceList?.getOrNull(position) ?: return

        holder.source.loadUri(source.uri, source.key, false, false)
    }

    override fun getItemCount() = sourceList?.size ?: 0

    class VH(vb: ItemSourceDetailBinding) : RecyclerView.ViewHolder(vb.root) {
        val root = vb.root
        val source = vb.source
    }
}