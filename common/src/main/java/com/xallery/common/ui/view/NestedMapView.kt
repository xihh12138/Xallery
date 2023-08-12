package com.xallery.common.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.xihh.base.util.logx
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MinimapOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class NestedMapView(context: Context, attrs: AttributeSet?) : MapView(context, attrs) {

    private val disallowSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var startX = 0f
    private var startY = 0f

    constructor(context: Context) : this(context, null)

    override fun onFinishInflate() {
        super.onFinishInflate()
        setMultiTouchControls(true)
        overlays.add(MyLocationNewOverlay(this).apply { enableMyLocation() })
        overlays.add(CompassOverlay(context, this).apply { enableCompass() })
        overlays.add(RotationGestureOverlay(this))
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        logx { "NestedMapView: onInterceptTouchEvent ev=$ev" }
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x
                startY = ev.y

                parent.requestDisallowInterceptTouchEvent(true)
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}