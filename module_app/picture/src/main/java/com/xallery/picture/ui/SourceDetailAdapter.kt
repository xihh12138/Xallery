package com.xallery.picture.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xallery.common.repository.db.model.Source
import com.xallery.common.util.loadUri
import com.xallery.picture.databinding.ItemSourceDetailBinding
import com.xihh.base.util.get12HourHMString
import com.xihh.base.util.getYMDString
import com.xihh.base.util.logx
import com.xihh.base.util.setTimeMills
import java.util.*

class SourceDetailAdapter(
    private val pictureViewListener: PictureView.DragListener,
    private val pageListener: VerticalTwoViewPager.Listener,
) : RecyclerView.Adapter<SourceDetailAdapter.VH>() {

    private val calendar = Calendar.getInstance()

    private var sourceList: List<Source>? = null

    private val mPagerListener = object : VerticalTwoViewPager.Listener {
        override fun onPageScroll(totalDistanceY: Float, distanceRatio: Float) {
            pageListener.onPageScroll(totalDistanceY, distanceRatio)
            logx { "SourceDetailAdapter: onPageScroll totalDistanceY=$totalDistanceY  distanceRatio=$distanceRatio" }
        }

        override fun onPageChange(page: Int) {
            curVerticalPage = page
            pageListener.onPageChange(page)
        }
    }

    private var curVerticalPage = 0

    fun updateData(data: List<Source>) {
        sourceList = data

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemSourceDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ).apply {
        ivBack.setOnClickListener {
            pager.scrollToFirstPage()
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val position = holder.adapterPosition
        val source = sourceList?.getOrNull(position) ?: return

//        holder.source.setImageURI(source.uri)
        holder.source.loadUri(source.uri, source.key, false, false)

        holder.tvTitle.text = source.name
        calendar.setTimeMills(source.addTimestamp)
        holder.tvDate.text =
            "${calendar.getYMDString(Locale.getDefault())} · ${calendar.get12HourHMString(Locale.getDefault())}"
        holder.tvResolution.text = "${source.width} × ${source.height}"
    }

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        holder.source.addDragListener(pictureViewListener)
        holder.pager.addListener(mPagerListener)

        if (curVerticalPage == 0) {
            holder.pager.scrollToFirstPage(true, false)
        } else {
            holder.pager.scrollToSecondPage(true, false)
        }
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        super.onViewDetachedFromWindow(holder)
        holder.source.removeDragListener(pictureViewListener)
        holder.pager.removeListener(mPagerListener)
    }

    override fun getItemCount() = sourceList?.size ?: 0

    class VH(vb: ItemSourceDetailBinding) : RecyclerView.ViewHolder(vb.root) {
        val pager = vb.pager
        val source = vb.source
        val ivBack = vb.ivBack
        val tvTitle = vb.tvTitle
        val tvDate = vb.tvDate
        val tvResolution = vb.tvResolution
    }
}