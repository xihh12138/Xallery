package com.xallery.picture.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.xallery.common.repository.db.model.Source
import com.xallery.common.util.loadUri
import com.xallery.picture.databinding.ItemSourceDetailBinding
import com.xihh.base.ui.PictureView
import com.xihh.base.ui.VerticalTwoViewPager
import com.xihh.base.util.ScreenUtil
import com.xihh.base.util.get12HourHMString
import com.xihh.base.util.getYMDString
import com.xihh.base.util.setTimeMills
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
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
        }

        override fun onPageChange(page: Int) {
            curVerticalPage = page
            pageListener.onPageChange(page)
        }
    }

    var curVerticalPage = 0
        private set

    fun updateData(data: List<Source>) {
        sourceList = data

        notifyDataSetChanged()
    }

    fun getItemViewViewBinding(itemView: View): ItemSourceDetailBinding {
        return ItemSourceDetailBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemSourceDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ).apply {
        ivBack.setOnClickListener {
            pager.scrollToFirstPage()
        }
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(16.0)
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
        holder.tvSize.text = "${source.sizeBytes} B"
        holder.tvUri.text = source.uri.toString()
        if (source.path != null) {
            holder.tvPath.text = source.path
            holder.tvPath.isVisible = true
        } else {
            holder.tvPath.isVisible = false
        }

        source.lat?.let { lat ->
            source.lng?.let { lng ->
                holder.map.controller.setCenter(GeoPoint(lat, lng))

                holder.tvCoordinate.text = "$lat , $lng"
            }
        }
    }

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        holder.source.addDragListener(pictureViewListener)
        holder.pager.addListener(mPagerListener)

        // ---------- 这是为了优化用户左右滚动时的体验 ----------
        if (curVerticalPage == 0) {
            holder.pager.scrollToFirstPage(true, false)
        } else {
            holder.pager.scrollToSecondPage(true, false)
        }
        // ---------- 在这里适配状态栏 ----------
        holder.pager.getChildAt(1)
            .updatePadding(top = ScreenUtil.getAbsStatusBarHeight(holder.itemView.rootView))
        holder.map.onResume()
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        super.onViewDetachedFromWindow(holder)
        holder.source.removeDragListener(pictureViewListener)
        holder.pager.removeListener(mPagerListener)
        holder.map.onPause()
    }

    override fun getItemCount() = sourceList?.size ?: 0

    class VH(vb: ItemSourceDetailBinding) : RecyclerView.ViewHolder(vb.root) {
        val pager = vb.pager
        val source = vb.source
        val ivBack = vb.ivBack
        val tvTitle = vb.tvTitle
        val tvDate = vb.tvDate
        val tvResolution = vb.tvResolution
        val tvSize = vb.tvSize
        val tvUri = vb.tvUri
        val tvPath = vb.tvPath
        val map = vb.map
        val tvCoordinate = vb.tvCoordinate
        val tvAddress = vb.tvAddress
    }
}