package com.arupakaman.aikon.uiModules

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.arupakaman.aikon.BuildConfig
import com.arupakaman.aikon.R
import com.arupakaman.aikon.uiModules.common.ActivityCommon
import com.arupakaman.aikon.utils.AikonRes

fun AppCompatActivity.gotoSettingsScreen(){
    startActivityForResult(ActivityCommon.getActivityIntent(this, ActivityCommon.KEY_FRAG_NAME_SETTINGS), ActivityCommon.REQUEST_CODE_SETTINGS)
}

fun Context.gotoAboutScreen(){
    startActivity(ActivityCommon.getActivityIntent(this, ActivityCommon.KEY_FRAG_NAME_ABOUT))
}

fun Fragment.openPickImageIntent(reqCode: Int){
    Intent(Intent.ACTION_PICK).let { iconPickIntent->
        iconPickIntent.type = "image/*"
        startActivityForResult(iconPickIntent, reqCode)
    }
}


fun Context.openContactMail(msg: String? = null){
    kotlin.runCatching {
        Intent(Intent.ACTION_SENDTO).let { emailIntent ->
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_publisher)))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            emailIntent.putExtra(Intent.EXTRA_TEXT, msg ?: AikonRes.getString(R.string.msg_enter_your_message))
            val packageManager = packageManager

            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(emailIntent, AikonRes.getString(R.string.title_send_via)))
            }
        }
    }
}

fun Context.openShareAppIntent(){
    kotlin.runCatching {
        Intent(Intent.ACTION_SEND).let { shareIntent ->
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            var shareMessage = "\n${AikonRes.getString(R.string.msg_share_aikon_app)}"
            shareMessage = "$shareMessage https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, AikonRes.getString(R.string.title_share_via)))
        }
    }.onFailure {
        Log.e("openShareAppIntent", "shareApp Exc : $it")
    }
}

fun Context.openDonationVersion(){
    openAppInPlayStore(getString(R.string.donate_version_pkg_name))
}

fun Context.openAppInPlayStore(id: String){
    val optionalIntent =  Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$id")
    )
    kotlin.runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id"))
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
        else startActivity(optionalIntent)
    }.onFailure {
        startActivity(optionalIntent)
    }
}

fun Context.openArupakamanPlayStore(){
    val intent =  Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Arupakaman+Studios"))
    kotlin.runCatching {
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
    }
}