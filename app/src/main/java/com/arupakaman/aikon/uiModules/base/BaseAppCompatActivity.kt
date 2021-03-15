package com.arupakaman.aikon.uiModules.base

import android.content.Context
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.arupakaman.aikon.data.AikonSharedPrefs
import com.arupakaman.aikon.utils.LocaleHelper

abstract class BaseAppCompatActivity : AppCompatActivity() {

    protected val mPrefs by lazy { AikonSharedPrefs.getInstance(applicationContext) }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }

}