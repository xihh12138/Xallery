package com.xallery.common.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import com.xallery.common.R
import com.xihh.base.util.logx
import org.osmdroid.tileprovider.tilesource.MapQuestTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class XMapView(context: Context, attrs: AttributeSet?) : MapView(context, attrs) {

    private var downX = 0f
    private var downY = 0f

    constructor(context: Context) : this(context, null)

    init {
        setDestroyMode(false)
    }

    fun setImageMark(lat: Double, lng: Double, drawable: Drawable, moveToCenter: Boolean = false) {
        val tag = getTag(R.id.map_unique_marker)
        if (tag is Marker) {
            tag.icon = drawable
            tag.position = GeoPoint(lat, lng)
        } else {
            val marker = Marker(this).apply {
                icon = drawable
                position = GeoPoint(lat, lng)
            }
            setTag(R.id.map_unique_marker, marker)
            overlays.add(marker)
        }

        if (moveToCenter) {
            controller.setCenter(GeoPoint(lat, lng))
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setTileSource(MapQuestTileSource(context))
        overlays.add(MyLocationNewOverlay(this).apply { enableMyLocation() })
//        overlays.add(CompassOverlay(context, this).apply { enableCompass() })
        overlays.add(RotationGestureOverlay(this))

        setMultiTouchControls(true)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        logx { "NestedMapView: onInterceptTouchEvent ev=$ev" }
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y

                parent.requestDisallowInterceptTouchEvent(true)
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    class FastScaleGestureOverlay(private val mapView: MapView) : Overlay() {

    }
}