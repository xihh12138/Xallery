package com.xihh.base.android

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback

abstract class ActivityExitSharedElementCallback(private val activity: AppCompatActivity) :
    MaterialContainerTransformSharedElementCallback() {

    protected val method =
        Fragment::class.java.getDeclaredMethod("getExitTransitionCallback").apply {
            isAccessible = true
        }

    protected fun getExitTransitionCallback(fragment: Fragment): SharedElementCallback? =
        (method.invoke(fragment) as? SharedElementCallback)

    override fun onMapSharedElements(
        names: MutableList<String>, sharedElements: MutableMap<String, View>
    ) {
        super.onMapSharedElements(names, sharedElements)
        activity.supportFragmentManager.fragments.forEach { fragment ->
            getExitTransitionCallback(fragment)?.onMapSharedElements(names, sharedElements)
        }
    }
}

abstract class ActivityEnterSharedElementCallback(private val activity: AppCompatActivity) :
    MaterialContainerTransformSharedElementCallback() {

    protected val method =
        Fragment::class.java.getDeclaredMethod("getEnterTransitionCallback").apply {
            isAccessible = true
        }

    protected fun getEnterTransitionCallback(fragment: Fragment): SharedElementCallback? =
        (method.invoke(fragment) as? SharedElementCallback)

    override fun onMapSharedElements(
        names: MutableList<String>, sharedElements: MutableMap<String, View>,
    ) {
        super.onMapSharedElements(names, sharedElements)
        activity.supportFragmentManager.fragments.forEach { fragment ->
            getEnterTransitionCallback(fragment)?.onMapSharedElements(names, sharedElements)
        }
    }
}

abstract class FragmentExitSharedElementCallback(private val fragment: Fragment) :
    SharedElementCallback() {

    protected val method =
        Fragment::class.java.getDeclaredMethod("getExitTransitionCallback").apply {
            isAccessible = true
        }

    protected fun getExitTransitionCallback(fragment: Fragment): SharedElementCallback? =
        (method.invoke(fragment) as? SharedElementCallback)

    override fun onMapSharedElements(
        names: MutableList<String>?, sharedElements: MutableMap<String, View>?,
    ) {
        super.onMapSharedElements(names, sharedElements)
        fragment.childFragmentManager.fragments.forEach { fragment ->
            getExitTransitionCallback(fragment)?.onMapSharedElements(names, sharedElements)
        }
    }

}

abstract class FragmentEnterSharedElementCallback(private val fragment: Fragment) :
    SharedElementCallback() {

    protected val method =
        Fragment::class.java.getDeclaredMethod("getEnterTransitionCallback").apply {
            isAccessible = true
        }

    protected fun getEnterTransitionCallback(fragment: Fragment): SharedElementCallback? =
        (method.invoke(fragment) as? SharedElementCallback)

    override fun onMapSharedElements(
        names: MutableList<String>?, sharedElements: MutableMap<String, View>?,
    ) {
        super.onMapSharedElements(names, sharedElements)
        fragment.childFragmentManager.fragments.forEach { fragment ->
            getEnterTransitionCallback(fragment)?.onMapSharedElements(names, sharedElements)
        }
    }

}