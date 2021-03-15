package com.arupakaman.aikon.uiModules.about

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.arupakaman.aikon.BuildConfig
import com.arupakaman.aikon.R
import com.arupakaman.aikon.databinding.FragmentAboutBinding
import com.arupakaman.aikon.uiModules.base.BaseFragment
import com.arupakaman.aikon.uiModules.openArupakamanPlayStore
import com.arupakaman.aikon.uiModules.openContactMail
import com.arupakaman.aikon.uiModules.openDonationVersion
import com.arupakaman.aikon.uiModules.openShareAppIntent
import com.arupakaman.aikon.utils.AikonRes
import com.arupakaman.aikon.utils.AppReviewUtil
import com.arupakaman.aikon.utils.invoke
import com.arupakaman.aikon.utils.setSafeOnClickListener

class FragmentAbout : BaseFragment() {

    companion object{

        private val TAG by lazy { "FragmentAbout" }

        fun newInstance() = FragmentAbout()

    }

    private val mBinding by lazy { FragmentAboutBinding.inflate(layoutInflater) }

    //private lateinit var mAppReviewUtil: AppReviewUtil

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

            btnDonationApp.isVisible = BuildConfig.isAdsOn
            tvMsgAboutUs.movementMethod = LinkMovementMethod.getInstance()

            btnDonationApp.setSafeOnClickListener {
                mActivity.openDonationVersion()
            }
            btnMoreApps.setSafeOnClickListener {
                mActivity.openArupakamanPlayStore()
            }
            btnRateApp.setSafeOnClickListener {
                AppReviewUtil.askForReview(mActivity)
            }
            btnShareApp.setSafeOnClickListener {
                mActivity.openShareAppIntent()
            }
            btnContact.setSafeOnClickListener {
                mActivity.openContactMail()
            }
        }
    }

}