package com.arupakaman.aikon.uiModules.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.arupakaman.aikon.BuildConfig
import com.arupakaman.aikon.R
import com.arupakaman.aikon.databinding.FragmentNotificationIconBinding
import com.arupakaman.aikon.services.ForegroundService
import com.arupakaman.aikon.uiModules.base.BaseFragment
import com.arupakaman.aikon.uiModules.openDonationVersion
import com.arupakaman.aikon.uiModules.openPickImageIntent
import com.arupakaman.aikon.utils.AdsUtil
import com.arupakaman.aikon.utils.AikonRes
import com.arupakaman.aikon.utils.AppIconUtil
import com.arupakaman.aikon.utils.invoke
import com.arupakaman.aikon.utils.setSafeOnClickListener
import com.arupakaman.aikon.utils.toast

class FragmentNotificationIcon: BaseFragment() {

    companion object{

        private val TAG by lazy { "FragmentNotifIcon" }

        private const val REQUEST_CODE_PICK_ICON = 1002

        fun newInstance() = FragmentNotificationIcon()

    }

    private val mBinding by lazy { FragmentNotificationIconBinding.inflate(layoutInflater) }
    private lateinit var adsUtil: AdsUtil

    private var selectedIcon: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding {
            adsUtil = AdsUtil(mActivity)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                btnPreviewAppNotification {
                    text = AikonRes.getString(R.string.err_msg_notification_icon_device_support)
                    isEnabled = false
                    isClickable = false
                    alpha = 0.4f
                }
            }

            iplAppName.hint = AikonRes.getString(R.string.hint_enter_app_name)
            btnDonationApp.isVisible = BuildConfig.isAdsOn

            switchRemoveNotification.isChecked = mPrefs.stopNotificationPreviewOnExit

            adsUtil.loadBannerAd(includeAdView.adView)

            setClickListeners()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE_PICK_ICON -> {
                kotlin.runCatching {
                    selectedIcon = data?.data
                    if (resultCode == Activity.RESULT_OK && selectedIcon != null){
                        mBinding.ivPickAppIcon.setImageURI(selectedIcon)
                    }else{
                        mActivity.toast(AikonRes.getString(R.string.err_msg_not_icon_selected))
                    }
                }.onFailure {
                    Log.e(TAG, "onActivityResult Exc -> $it")
                    mActivity.toast(AikonRes.getString(R.string.err_msg_general))
                }
            }
        }
    }

    private fun FragmentNotificationIconBinding.setClickListeners(){

        ivPickAppIcon.setSafeOnClickListener {
            openPickImageIntent(REQUEST_CODE_PICK_ICON)
        }

        btnDonationApp.setSafeOnClickListener {
            mActivity.openDonationVersion()
        }

        btnPreviewAppNotification.setSafeOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appName = etAppName.text?.toString()?.trim()
                if (appName.isNullOrBlank()) {
                    mActivity.toast(AikonRes.getString(R.string.err_msg_enter_app_name))
                    return@setSafeOnClickListener
                }
                if (BuildConfig.isAdsOn && mPrefs.previewCount == 5){
                    adsUtil.showInterstitialAd(getString(R.string.key_ad_mob_interstitial_home_unit_id)) {
                        mPrefs.previewCount = 0
                    }
                }else {
                    if (BuildConfig.isAdsOn) mPrefs.previewCount = mPrefs.previewCount + 1
                    ForegroundService.startOrStopService(mActivity, true, appName, selectedIcon?.toString()?:"")
                }
            }
        }

        btnPreviewAppIcon.setSafeOnClickListener {
            (mActivity as ActivityHome).changeViewPagerItem(0)
        }

        switchRemoveNotification.setOnCheckedChangeListener { _, b ->
            mPrefs.stopNotificationPreviewOnExit = b
        }

    }

}