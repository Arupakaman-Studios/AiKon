package com.arupakaman.aikon.uiModules.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.isVisible
import com.arupakaman.aikon.BuildConfig
import com.arupakaman.aikon.R
import com.arupakaman.aikon.databinding.FragmentAppIconBinding
import com.arupakaman.aikon.uiModules.base.BaseFragment
import com.arupakaman.aikon.uiModules.openDonationVersion
import com.arupakaman.aikon.uiModules.openPickImageIntent
import com.arupakaman.aikon.utils.AdsUtil
import com.arupakaman.aikon.utils.AikonRes
import com.arupakaman.aikon.utils.AppIconUtil
import com.arupakaman.aikon.utils.invoke
import com.arupakaman.aikon.utils.setSafeOnClickListener
import com.arupakaman.aikon.utils.toast
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class FragmentAppIcon: BaseFragment() {

    companion object{

        private val TAG by lazy { "FragmentAppIcon" }

        private const val REQUEST_CODE_PICK_ICON = 1001
        private const val REQUEST_CODE_PICK_BG = 1002
        private const val ACTION_PREVIEW_ICON_CLICK = "Open_Aikon"

        fun newInstance() = FragmentAppIcon()

    }

    private val mBinding by lazy { FragmentAppIconBinding.inflate(layoutInflater) }
    private var mDialogBgPick: AlertDialog? = null
    private lateinit var adsUtil: AdsUtil

    private var selectedIcon: Bitmap? = null
    private var selectedBgColor = Color.WHITE
    private var selectedBgUri: Uri? = null
    private var selectedBgType = 0
    private var iconPadding = 500

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

            btnPreviewAppIcon{
                if (!ShortcutManagerCompat.isRequestPinShortcutSupported(mActivity)){
                    text = AikonRes.getString(R.string.err_msg_app_icon_device_support)
                    isEnabled = false
                    isClickable = false
                    alpha = 0.4f
                }
            }

            ivPickAppIcon.post {
                clAppIconContainer.layoutParams{
                    width = iconPadding + 100
                    height = iconPadding + 100
                }
                clAppIconContainer.requestLayout()
                scaleAppIcon(iconPadding)
            }

            iplAppName.hint = AikonRes.getString(R.string.hint_enter_app_name)
            btnDonationApp.isVisible = BuildConfig.isAdsOn

            switchAdaptiveIcon.isChecked = mPrefs.isAdaptiveIcon
            TooltipCompat.setTooltipText(ivAppIconTooltip, ivAppIconTooltip.contentDescription)

            adsUtil.loadBannerAd(includeAdView.adView)

            setViewListeners()
        }
    }

    override fun onDestroy() {
        if (mDialogBgPick?.isShowing == true){
            mDialogBgPick?.dismiss()
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE_PICK_ICON -> {
                kotlin.runCatching {
                    selectedIcon = AppIconUtil.uriToBitmap(mActivity, data?.data)
                    if (resultCode == Activity.RESULT_OK && selectedIcon != null) {
                        mBinding.ivPickAppIcon.setImageBitmap(selectedIcon)
                    } else {
                        mActivity.toast(AikonRes.getString(R.string.err_msg_not_icon_selected))
                    }
                }.onFailure {
                    Log.e(TAG, "onActivityResult Exc -> $it")
                    mActivity.toast(AikonRes.getString(R.string.err_msg_general))
                }
            }
            REQUEST_CODE_PICK_BG -> {
                if (resultCode == Activity.RESULT_OK && data?.data != null) {
                    selectedBgUri = data.data
                    mBinding.ivAppIconBg{
                        setBackgroundResource(R.color.colorTransparent)
                        setImageBitmap(AppIconUtil.uriToBitmap(mActivity, selectedBgUri))
                    }
                }else{
                    selectedBgType = 0
                    mActivity.toast(AikonRes.getString(R.string.err_msg_not_icon_bg_selected))
                }
            }
        }
    }

    private fun FragmentAppIconBinding.setViewListeners(){

        ivPickAppIcon.setSafeOnClickListener {
            openPickImageIntent(REQUEST_CODE_PICK_ICON)
        }

        //https://stackoverflow.com/questions/47611448/how-do-launchers-change-the-shape-of-an-adaptive-icon-including-removal-of-back
        //https://github.com/fennifith/AdaptiveIconView

        btnPreviewAppIcon.setSafeOnClickListener {
            if (BuildConfig.isAdsOn && mPrefs.previewCount == 5){
                adsUtil.showInterstitialAd(getString(R.string.key_ad_mob_interstitial_home_unit_id)) {
                    mPrefs.previewCount = 0
                }
            }else {
                if (BuildConfig.isAdsOn) mPrefs.previewCount = mPrefs.previewCount + 1
                onPreviewClick()
            }
        }

        btnDonationApp.setSafeOnClickListener {
            mActivity.openDonationVersion()
        }

        btnPreviewAppNotification.setSafeOnClickListener {
            (mActivity as ActivityHome).changeViewPagerItem(1)
        }

        btnPickIconBg.setSafeOnClickListener {
            showPickBgDialog()
        }

        switchAdaptiveIcon.setOnCheckedChangeListener { _, b ->
            mPrefs.isAdaptiveIcon = b
        }

        sliderAppIconPadding.addOnChangeListener { _, value, _ ->
            iconPadding = value.toInt()
            scaleAppIcon(1000 - iconPadding)
        }

    }

    private fun FragmentAppIconBinding.scaleAppIcon(padding: Int){
        ivPickAppIcon.layoutParams{
            width = padding
            height = padding
        }
        ivPickAppIcon.requestLayout()
    }

    private fun FragmentAppIconBinding.onPreviewClick(){
        kotlin.runCatching {
            val shortcutName = etAppName.text?.toString()
            if (shortcutName.isNullOrBlank()) {
                mActivity.toast(AikonRes.getString(R.string.err_msg_enter_app_name))
                return
            }
            val selectedBg = when(selectedBgType){
                2 -> AppIconUtil.getColorToBitmap(selectedBgColor, 512 + iconPadding)
                1 -> AppIconUtil.uriToBitmap(mActivity, selectedBgUri, 512 + iconPadding)
                else -> AppIconUtil.vectorDrawableToBitmap(mActivity, R.drawable.ic_app_icon_background, 512 + iconPadding)
            }
            if (selectedIcon == null) {
                selectedIcon = AppIconUtil.vectorDrawableToBitmap(mActivity, R.drawable.ic_aikon_baseline_logo, 512)
            }
            if (selectedBg == null || selectedIcon == null) {
                mActivity.toast(AikonRes.getString(R.string.err_msg_general))
                return
            }
            val appIcon = AppIconUtil.getAdaptiveIcon(
                selectedIcon!!,
                selectedBg,
                (iconPadding - 150)
            )
            val shortcutId = "${mPrefs.lastPreviewId}${System.currentTimeMillis()}"
            mPrefs.lastPreviewName = shortcutName

            //val scaledBitmap = Bitmap.createScaledBitmap(if (switchAdaptiveIcon.isChecked) appIcon else selectedIcon!!, 100, 100, false)
            ShortcutInfoCompat.Builder(mActivity, shortcutId)
                .setIntent(
                    Intent(mActivity, ActivityHome::class.java)
                        .setAction(ACTION_PREVIEW_ICON_CLICK)
                )
                .setShortLabel(shortcutName)
                .setLongLabel(shortcutName)
                .setIcon(
                    if (switchAdaptiveIcon.isChecked)
                        IconCompat.createWithAdaptiveBitmap(appIcon)
                    else IconCompat.createWithBitmap(selectedIcon)
                )
                .setAlwaysBadged()
                .build().let { shortcut->
                    ShortcutManagerCompat.requestPinShortcut(mActivity, shortcut, null)
                }
        }.onFailure {
            Log.e(TAG, "btnPreviewAppIcon Click Exc -> $it")
            mActivity.toast(AikonRes.getString(R.string.err_msg_general))
        }
    }

    private fun showPickBgDialog() {
        MaterialAlertDialogBuilder(mActivity).let { pictureDialog ->
            pictureDialog.setTitle(AikonRes.getString(R.string.msg_select_app_icon_background_from))
            val pictureDialogItems = arrayOf(
                AikonRes.getString(R.string.title_default),
                AikonRes.getString(R.string.title_gallery),
                AikonRes.getString(R.string.title_color)
            )
            pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                selectedBgType = which
                dialog.dismiss()
                when (which) {
                    0 -> {
                        mBinding.ivAppIconBg {
                            setBackgroundResource(R.color.colorTransparent)
                            setImageResource(R.drawable.ic_app_icon_background)
                        }
                    }
                    1 -> {
                        openPickImageIntent(REQUEST_CODE_PICK_BG)
                    }
                    2 -> {
                        ColorPickerDialog
                            .Builder(mActivity)
                            .setTitle(AikonRes.getString(R.string.title_select_icon_background_color))
                            .setPositiveButton(AikonRes.getString(R.string.action_select))
                            .setNegativeButton(AikonRes.getString(R.string.action_cancel))
                            .setColorShape(ColorShape.SQAURE)
                            .setDefaultColor(R.color.colorWhite)
                            .setColorListener { color, _ ->   //colorHex
                                selectedBgColor = color
                                mBinding.ivAppIconBg{
                                    setImageDrawable(null)
                                    setBackgroundColor(color)
                                }
                            }
                            .show()
                    }
                }
            }
            mDialogBgPick = pictureDialog.create()
            mDialogBgPick?.show()
        }
    }

}