package com.arupakaman.aikon.uiModules.home

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.arupakaman.aikon.BuildConfig
import com.arupakaman.aikon.R
import com.arupakaman.aikon.databinding.ActivityHomeBinding
import com.arupakaman.aikon.services.ForegroundService
import com.arupakaman.aikon.uiModules.base.BaseAppCompatActivity
import com.arupakaman.aikon.uiModules.common.ActivityCommon
import com.arupakaman.aikon.uiModules.gotoAboutScreen
import com.arupakaman.aikon.uiModules.gotoSettingsScreen
import com.arupakaman.aikon.uiModules.openArupakamanPlayStore
import com.arupakaman.aikon.uiModules.openContactMail
import com.arupakaman.aikon.uiModules.openDonationVersion
import com.arupakaman.aikon.uiModules.openShareAppIntent
import com.arupakaman.aikon.utils.AikonRes
import com.arupakaman.aikon.utils.AppReviewUtil
import com.arupakaman.aikon.utils.htmlText
import com.arupakaman.aikon.utils.invoke
import com.arupakaman.aikon.utils.relativeSizeSpan
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ActivityHome : BaseAppCompatActivity() {

    companion object {

        private val TAG by lazy { "ActivityHome" }

    }

    private val mBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var mAdapterFragmentState: AdapterFragmentState
    private var mDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding{

            setSupportActionBar(appBarInclude.toolbar)
            appBarInclude.tvToolbarTitle.relativeSizeSpan(getString(R.string.app_name) + " Beta", 6, 10)  //getString(R.string.app_name) + " <sup><small>Beta</small></sup>"

            initNavDrawer()

            mAdapterFragmentState = AdapterFragmentState(this@ActivityHome)
            mAdapterFragmentState.addFragment(FragmentAppIcon.newInstance(), AikonRes.getString(R.string.action_preview_app_icon))
            mAdapterFragmentState.addFragment(FragmentNotificationIcon.newInstance(), AikonRes.getString(R.string.action_preview_app_notification_icon))

            viewPager.adapter = mAdapterFragmentState
        }
    }

   /* override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }*/

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        mBinding {
            when {
                drawerLayoutHome.isDrawerOpen(GravityCompat.START) -> {
                    drawerLayoutHome.closeDrawer(GravityCompat.START)
                }
                viewPager.currentItem == 1 -> {
                    viewPager.currentItem = 0
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ForegroundService.isRunning && mPrefs.stopNotificationPreviewOnExit)
                        ForegroundService.startOrStopService(this@ActivityHome, false)
                    super.onBackPressed()
                }
            }
        }
    }

    override fun onDestroy() {
        if (mDialog?.isShowing == true){
            mDialog?.dismiss()
        }
        super.onDestroy()
    }

    private fun ActivityHomeBinding.initNavDrawer() {
        drawerToggle = ActionBarDrawerToggle(
            this@ActivityHome,
            drawerLayoutHome,
            appBarInclude.toolbar,
            R.string.title_drawer_open,
            R.string.title_drawer_close
        )
        drawerLayoutHome.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.menu.findItem(R.id.navItemDonateApp).isVisible = BuildConfig.isAdsOn

        navigationView.setNavigationItemSelectedListener {
            drawerLayoutHome.closeDrawer(GravityCompat.START)
            when(it.itemId){
                R.id.navItemAbout -> gotoAboutScreen()
                R.id.navItemSettings -> gotoSettingsScreen()
                R.id.navItemRate -> AppReviewUtil.askForReview(this@ActivityHome)
                R.id.navItemShare -> openShareAppIntent()
                R.id.navItemContact -> openContactMail()
                R.id.navItemNotWorking -> showNotWorkingDialog()
                R.id.navItemDonateApp -> openDonationVersion()
                R.id.navItemMoreApps -> openArupakamanPlayStore()
            }
            return@setNavigationItemSelectedListener true
        }
    }

    fun changeViewPagerItem(pos: Int){
        mBinding.viewPager.currentItem = pos
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityCommon.REQUEST_CODE_SETTINGS && resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }

    private fun showNotWorkingDialog(){
        mDialog = MaterialAlertDialogBuilder(this)
            .setTitle(AikonRes.getString(R.string.title_not_working))
            .setMessage(AikonRes.getString(R.string.msg_not_working))
            .setPositiveButton(AikonRes.getString(R.string.action_write_device_info_in_mail)){ dialog, _ ->
                openContactMail(getDeviceInfo())
                dialog.dismiss()
            }
            .create()

        mDialog?.show()
    }

    private fun getDeviceInfo(): String{
        var deviceInfoMsg = AikonRes.getString(R.string.msg_device_info_start)

        deviceInfoMsg += listOf(
            "\n\nOS Version Release : ${Build.VERSION.RELEASE}\n",
            "Model : ${Build.MODEL}",
            "Brand : ${Build.BRAND}\n",
            "Manufacturer : ${Build.MANUFACTURER}\n",
            "Version SDK : ${Build.VERSION.SDK_INT}\n\n\n",
        ).joinToString("")

        deviceInfoMsg += AikonRes.getString(R.string.msg_enter_your_message)
        return deviceInfoMsg
    }

}