package com.xallery.picture.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.xallery.common.repository.db.model.Source
import com.xallery.common.ui.view.XMapView
import com.xallery.common.util.loadUri
import com.xallery.picture.databinding.ItemSourceDetailBinding
import com.xihh.base.android.appContext
import com.xihh.base.ui.PictureView
import com.xihh.base.ui.VerticalTwoViewPager
import com.xihh.base.util.*
import java.util.*

class SourceDetailAdapter(
    private val pictureViewListener: PictureView.DragListener,
    private val pageListener: VerticalTwoViewPager.Listener,
    private val mapViewProvider: ReuseReferenceProvider<XMapView>,
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

        val lat = source.lat
        val lng = source.lng
        if (lat != null && lng != null) {
            holder.tvCoordinate.text = "$lat , $lng"

//            appContext.contentResolver.openInputStream(source.uri).use {
//                val mapView = (holder.mapContainer.getChildAt(0) as? XMapView).let { xmap ->
//                    if (xmap == null) {
//                        logx { "SourceDetailAdapter: onBindViewHolder[$position]   mapView doesn't load,load mapView" }
//                        val mapView = reuseReferenceProvider.acquire()
//                        mapView.parent?.let {
//                            logx { "SourceDetailAdapter: onBindViewHolder[$position]   mapView was using,remove from it's old parent" }
//                            reuseReferenceProvider.release(mapView)
//
//                            (it as ViewGroup).removeView(mapView)
//                        }
//                        holder.mapContainer.addView(mapView)
//                        mapView
//                    } else {
//                        logx { "SourceDetailAdapter: onBindViewHolder[$position]   mapView is exist" }
//                        xmap
//                    }
//                }
//                val drawable = /*ScaleDrawable(
//                    Drawable.createFromStream(it, source.path)
//                        ?: appContext.drawableRes(org.osmdroid.library.R.drawable.marker_default),
//                    Gravity.BOTTOM,
//                    0.9f,
//                    0.9f
//                )*/appContext.drawableRes(org.osmdroid.library.R.drawable.marker_default)!!
//                drawable.level = 1
//                mapView.setImageMark(lat, lng, drawable, true)
//                mapView.setTileSource(TileSourceFactory.MAPNIK)
//                mapView.controller.setZoom(16.0)
//            }
        } else {
            holder.tvCoordinate.text = ""
//            logx { "SourceDetailAdapter: onBindViewHolder[$position]   remove mapView" }
//            (holder.mapContainer.getChildAt(0) as? XMapView)?.let {
//                reuseReferenceProvider.release(it)
//                holder.mapContainer.removeView(it)
//            }
        }
    }

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        logx { "SourceDetailAdapter: onViewAttachedToWindow[${holder.adapterPosition}]" }
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
//        (holder.mapContainer.getChildAt(0) as? XMapView)?.onResume()

        val position = holder.adapterPosition
        val source = sourceList?.getOrNull(position) ?: return
        val lat = source.lat
        val lng = source.lng
        if (lat != null && lng != null) {
            appContext.contentResolver.openInputStream(source.uri).use {
                val mapView = mapViewProvider.acquire()
                holder.mapContainer.addView(mapView)
                mapView.onResume()
                val drawable = /*ScaleDrawable(
                    Drawable.createFromStream(it, source.path)
                        ?: appContext.drawableRes(org.osmdroid.library.R.drawable.marker_default),
                    Gravity.BOTTOM,
                    0.9f,
                    0.9f
                )*/appContext.drawableRes(org.osmdroid.library.R.drawable.marker_default)!!
                drawable.level = 1
                mapView.setImageMark(lat, lng, drawable, true)
                mapView.controller.setZoom(16.0)
            }
        } else {
            (holder.mapContainer.getChildAt(0) as? XMapView)?.let { mapView ->
                mapViewProvider.release(mapView)
                holder.mapContainer.removeView(mapView)
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        super.onViewDetachedFromWindow(holder)
        logx { "SourceDetailAdapter: onViewDetachedFromWindow[${holder.adapterPosition}]" }
        holder.source.removeDragListener(pictureViewListener)
        holder.pager.removeListener(mPagerListener)
//        (holder.mapContainer.getChildAt(0) as? XMapView)?.onPause()
        (holder.mapContainer.getChildAt(0) as? XMapView)?.let { mapView ->
            mapViewProvider.release(mapView)
            holder.mapContainer.removeView(mapView)
        }
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
        val mapContainer = vb.mapContainer
        val tvCoordinate = vb.tvCoordinate
        val tvAddress = vb.tvAddress
    }
}