package com.arupakaman.aikon

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.arupakaman.aikon.data.AikonSharedPrefs
import com.arupakaman.aikon.utils.AikonRes
import com.arupakaman.aikon.utils.DefaultExceptionHandler
import com.arupakaman.aikon.utils.LocaleHelper

class AikonApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        AikonRes.setContext(applicationContext)

        if (BuildConfig.DEBUG || BuildConfig.isFDroid)
            Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler(this))

        //Init
        val mPrefs = AikonSharedPrefs.getInstance(this)

        AppCompatDelegate.setDefaultNightMode(mPrefs.selectedThemeMode)

    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(this)
    }

}