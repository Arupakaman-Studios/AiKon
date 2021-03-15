package com.arupakaman.aikon.uiModules.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.arupakaman.aikon.R
import com.arupakaman.aikon.databinding.FragmentSettingsBinding
import com.arupakaman.aikon.uiModules.base.BaseFragment
import com.arupakaman.aikon.uiModules.common.ActivityCommon
import com.arupakaman.aikon.utils.AikonRes
import com.arupakaman.aikon.utils.LocaleHelper
import com.arupakaman.aikon.utils.invoke
import com.arupakaman.aikon.utils.setSafeOnClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FragmentSettings : BaseFragment() {

    companion object{

        private val TAG by lazy { "FragmentSettings" }

        fun newInstance() = FragmentSettings()

    }

    private val mBinding by lazy { FragmentSettingsBinding.inflate(layoutInflater) }

    private var selLangPos = 0

    private var mDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding{
            setViewListeners()
            setSelectedTheme()
        }
    }

    override fun onDestroy() {
        if (mDialog?.isShowing == true){
            mDialog?.dismiss()
        }
        mDialog = null
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    private fun FragmentSettingsBinding.setViewListeners(){
        llSystemTheme.setSafeOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        llLightTheme.setSafeOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_NO)
        }
        llDarkTheme.setSafeOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_YES)
        }

        selLangPos = LocaleHelper.getSelectedLanguageCodePosition(mActivity)

        tvLanguageDesc.text = AikonRes.getString(R.string.desc_current_language_colon) + " " + LocaleHelper.getLanguageByPosition(mActivity, selLangPos).second

        clLanguage.setSafeOnClickListener {
            mDialog = MaterialAlertDialogBuilder(mActivity)
                .setTitle(AikonRes.getString(R.string.title_select_language_colon))
                .setSingleChoiceItems(R.array.arr_languages, LocaleHelper.getSelectedLanguageCodePosition(mActivity)){ _, which ->
                    selLangPos = which
                }
                .setPositiveButton(R.string.action_select){ dialog, _ ->
                    val selLang = LocaleHelper.getLanguageByPosition(mActivity, selLangPos)
                    LocaleHelper.setLocale(mActivity, selLang.first, selLang.second)
                    dialog.dismiss()
                    mDialog = null
                    (mActivity as ActivityCommon).languageChanged = true
                    mActivity.onBackPressed()
                }
                .setNegativeButton(R.string.action_cancel){ dialog, _ ->
                    dialog.dismiss()
                    mDialog = null
                }.create()

            mDialog?.show()

        }
    }

    private fun FragmentSettingsBinding.setSelectedTheme(){
        when(mPrefs.selectedThemeMode){
            AppCompatDelegate.MODE_NIGHT_NO -> {
                llSystemTheme.setBackgroundResource(R.color.colorTransparent)
                llDarkTheme.setBackgroundResource(R.color.colorTransparent)
                llLightTheme.setBackgroundResource(R.drawable.bg_rounded_rectangle_primary)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                llSystemTheme.setBackgroundResource(R.color.colorTransparent)
                llLightTheme.setBackgroundResource(R.color.colorTransparent)
                llDarkTheme.setBackgroundResource(R.drawable.bg_rounded_rectangle_primary)
            }
            else -> {
                llLightTheme.setBackgroundResource(R.color.colorTransparent)
                llDarkTheme.setBackgroundResource(R.color.colorTransparent)
                llSystemTheme.setBackgroundResource(R.drawable.bg_rounded_rectangle_primary)
            }
        }
    }

    private fun updateTheme(themeMode: Int){
        mPrefs.selectedThemeMode = themeMode
        AppCompatDelegate.setDefaultNightMode(themeMode)
        (mActivity as AppCompatActivity).delegate.applyDayNight()
        mBinding.setSelectedTheme()
    }


}