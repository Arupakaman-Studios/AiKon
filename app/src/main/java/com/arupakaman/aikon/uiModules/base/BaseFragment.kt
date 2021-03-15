package com.arupakaman.aikon.uiModules.base

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.arupakaman.aikon.data.AikonSharedPrefs

abstract class BaseFragment : Fragment() {

    protected lateinit var mActivity: Activity

    protected val mPrefs by lazy { AikonSharedPrefs.getInstance(mActivity) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
    }

}