package com.xallery.main.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.xallery.common.reposity.RouteViewModel
import com.xallery.common.reposity.config
import com.xallery.common.ui.LoadingHost
import com.xallery.common.ui.LoadingHostImpl
import com.xallery.common.ui.LottieHost
import com.xallery.common.ui.LottieHostImpl
import com.xallery.common.ui.view.CommonDialogFragment
import com.xallery.common.util.toast
import com.xallery.picture.ui.PictureDetailsFragment
import com.xihh.base.android.BaseActivity
import com.xihh.base.android.SuspendActivityResultContract.Companion.registerForActivityResult
import com.xihh.base.delegate.RouteAction
import com.xihh.base.util.hasManageMediaPermission
import com.xihh.base.util.hasPermission
import com.xihh.base.util.isExternalStorageManager
import com.xihh.base.util.launchGrantAllFilesIntent
import com.xihh.xallery.R
import com.xihh.xallery.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class MainActivity : BaseActivity<ActivityMainBinding>(), LoadingHost by LoadingHostImpl(),
    LottieHost by LottieHostImpl() {

    private val permissionContract =
        registerForActivityResult(ActivityResultContracts.RequestPermission())

    @RequiresApi(Build.VERSION_CODES.S)
    private val requestManageMediaContract =
        registerForActivityResult(object : ActivityResultContract<Unit, Unit>() {
            override fun createIntent(context: Context, input: Unit): Intent {
                return Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA)
                    .setData(Uri.parse("package:$packageName"))
            }

            override fun parseResult(resultCode: Int, intent: Intent?) {
            }
        })

    private val vm by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    private val routeVm by lazy { ViewModelProvider(this)[RouteViewModel::class.java] }

    override fun initView(savedInstanceState: Bundle?) {
        initFragment(savedInstanceState)
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            routeVm.getRouteActionFlow().collect {
                // ---------- 添加fragment ----------
                when (it.flag) {
                    RouteViewModel.ROUTE_FLAG_MAIN -> {
                        routeVm.arrangeAction {
                            supportFragmentManager.beginTransaction()
                                .disallowAddToBackStack()
                                .setReorderingAllowed(true)
                                .add(
                                    vb.container.id,
                                    MainFragment::class.java,
                                    null,
                                    MainFragment::class.simpleName
                                )
                                .commit()
                        }
                    }

                    RouteViewModel.ROUTE_FLAG_PICTURE -> {
                        routeVm.arrangeAction {
                            val view = (it.extras?.get("view") as WeakReference<*>).get() as View
                            val t = supportFragmentManager.beginTransaction()
                                .setReorderingAllowed(true)
                                .addSharedElement(view, view.transitionName)
                                .replace(
                                    vb.container.id,
                                    PictureDetailsFragment::class.java,
                                    null,
                                    PictureDetailsFragment::class.simpleName
                                )
//                                .add(
//                                    vb.container.id,
//                                    PictureDetailsFragment(),
//                                    PictureDetailsFragment::class.simpleName
//                                )
//                                .hide(supportFragmentManager.fragments.find { it is MainFragment }!!)

                            t.addToBackStack(PictureDetailsFragment::class.simpleName)
                                .commit()
                        }
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            if (acquirePermission()) {
                if (savedInstanceState == null) {
                    routeVm.addActionNow(RouteAction(RouteViewModel.ROUTE_FLAG_MAIN))
                }
            }
        }
    }

    private suspend fun acquirePermission(): Boolean {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        if (!hasPermission(storagePermission) && !permissionContract.get(storagePermission)) {
            toast(R.string.no_permission)
            finish()

            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val mediaLocationPermission = Manifest.permission.ACCESS_MEDIA_LOCATION

            if (!hasPermission(mediaLocationPermission) &&
                !permissionContract.get(mediaLocationPermission)
            ) {
                toast(R.string.no_permission)
                finish()

                return false
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val videoPermission = Manifest.permission.READ_MEDIA_VIDEO
            if (!hasPermission(videoPermission) && !permissionContract.get(videoPermission)) {
                toast(R.string.no_permission)
                finish()

                return false
            }
        }

        if (!hasManageMediaPermission() && !isExternalStorageManager()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !config.hasAccessStorage) {
                if (!Environment.isExternalStorageManager()) {
                    var messagePrompt = getString(R.string.access_storage_prompt)
                    messagePrompt += if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        "\n\n${getString(R.string.media_management_alternative)}"
                    } else {
                        "\n\n${getString(R.string.alternative_media_access)}"
                    }
                    CommonDialogFragment().content(messagePrompt).onConfirm(R.string.all_files) {
                        launchGrantAllFilesIntent()
                    }.onCancel(R.string.media_only) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            lifecycleScope.launchWhenResumed {
                                requestManageMediaContract.get(Unit)
                            }
                        } else {
                            config.hasAccessStorage = true
                        }
                    }
                }
            }
        }

        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val pageFlag = intent?.getIntExtra(EXTRA_PAGE_FLAG, -1) ?: -1
        if (pageFlag != -1) {
            vm.goMainPage(pageFlag)
        }
    }

    override fun onStart() {
        super.onStart()
        routeVm.execAllPendingAction { it.invoke() }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            routeVm.arrangeAction {
                super.onBackPressed()
            }
        }
    }

    companion object {

        private const val EXTRA_PAGE_FLAG = "page_flags"

        fun goPage(context: Context, @MainPageFlags page: Int) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(EXTRA_PAGE_FLAG, page)
            context.startActivity(intent)
        }
    }
}