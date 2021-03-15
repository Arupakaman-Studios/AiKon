package com.arupakaman.aikon.utils

import android.app.Activity
import android.view.View

class AdsUtil(private val mActivity: Activity) {

    fun loadBannerAd(adView: View){}

    fun showInterstitialAd(adUnitId: String, onAdLoaded : () -> Unit = {}){ }

}