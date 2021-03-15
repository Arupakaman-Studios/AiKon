package com.arupakaman.aikon.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes

/**
 *   Resource Wrapper
 */

@SuppressLint("StaticFieldLeak")
object AikonRes {

    private lateinit var mContext: Context

    fun setContext(context: Context){
        mContext = context
    }

    fun getString(@StringRes resId: Int) = mContext.getString(resId)

}