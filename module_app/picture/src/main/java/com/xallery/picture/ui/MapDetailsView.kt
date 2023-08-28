package com.xallery.picture.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.xallery.common.ui.view.XMapView
import com.xallery.picture.databinding.ViewMapDetailsBinding
import com.xihh.base.android.appContext
import com.xihh.base.util.ReuseReferenceProvider
import com.xihh.base.util.drawableRes

class MapDetailsView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val vb = ViewMapDetailsBinding.inflate(LayoutInflater.from(context), this, true)

    private val mapView get() = vb.mapContainer.getChildAt(0) as? XMapView

    fun loadMapView(mapViewProvider: ReuseReferenceProvider<XMapView>) {
        if (mapView != null) {
            return
        }

        val mapView = mapViewProvider.acquire()
        vb.mapContainer.addView(mapView)
        mapView.onResume()
    }

    fun unloadMapView(mapViewProvider: ReuseReferenceProvider<XMapView>) {
        val mapView = mapView ?: return

        mapView.onPause()
        vb.mapContainer.removeView(mapView)
        mapViewProvider.release(mapView)
    }

    fun setLatLng(lat: Double?, lng: Double?, mapViewProvider: ReuseReferenceProvider<XMapView>) {
        if (lat != null && lng != null) {
            loadMapView(mapViewProvider)
            vb.tvCoordinate.text = "$lat , $lng"

            mapView?.let { mapView ->
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

            vb.root.visibility = VISIBLE
        } else {
            vb.root.visibility = GONE
        }

    }
}