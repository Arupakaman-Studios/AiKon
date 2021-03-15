package com.arupakaman.aikon.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.arupakaman.aikon.R
import com.arupakaman.aikon.utils.AikonRes
import com.arupakaman.aikon.utils.AppIconUtil
import com.arupakaman.aikon.utils.toast

@RequiresApi(Build.VERSION_CODES.O)
class ForegroundService : Service() {

    companion object {

        private val TAG by lazy { "ForegroundService" }
        private const val ACTION_STOP = "stop_action"
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_APP_ICON_URI = "app_icon_uri"
        const val NOTIFICATION_ID = 101

        var isRunning = false

        @RequiresApi(Build.VERSION_CODES.O)
        fun startOrStopService(mContext: Context, start: Boolean, appName: String? = null, appIconUri: String? = null) {
            Log.d(TAG, "startOrStop start -> $start")
            Intent(mContext, ForegroundService::class.java).let { intent->
                if (start) {
                    if (isRunning) mContext.stopService(intent)
                    intent.putExtra(EXTRA_APP_NAME, appName)
                    intent.putExtra(EXTRA_APP_ICON_URI, appIconUri)
                    ContextCompat.startForegroundService(mContext, intent)
                } else {
                    mContext.stopService(intent)
                }
                isRunning = start
            }
        }

    }

    private lateinit var appName: String
    private lateinit var appIcon: Bitmap

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
    }


    @Suppress("DEPRECATION")
    private fun getNotificationBuilder(): Notification.Builder{
        return Notification.Builder(
            this,
            AikonRes.getString(R.string.notification_channel_name)
        )
            .setContentTitle(appName)
            .setTicker(AikonRes.getString(R.string.app_name))
            .setSmallIcon(Icon.createWithBitmap(appIcon))
            .setOngoing(true)
            .setShowWhen(true)
            .setVibrate(longArrayOf(0, 500, 1000))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setWhen(System.currentTimeMillis())
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorBlackLightX))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setStyle(
                Notification.BigTextStyle()
                    .bigText(AikonRes.getString(R.string.msg_notification_preview))
            )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "Received ACTION_STOP")
            startOrStopService(this, false)
        }else{
            intent?.runCatching {
                appName = getStringExtra(EXTRA_APP_NAME)?:AikonRes.getString(R.string.app_name)
                val appIconUri = getStringExtra(EXTRA_APP_ICON_URI)

                if (!appIconUri.isNullOrBlank()){
                    val bitmap = AppIconUtil.uriToBitmap(applicationContext, Uri.parse(appIconUri), 512)
                    if (bitmap != null)
                        appIcon = bitmap
                }
                Log.d(TAG, "getIntentN appName -> $appName appIconUri $appIconUri")
                if (!::appIcon.isInitialized)
                    appIcon = AppIconUtil.vectorDrawableToBitmap(applicationContext, R.drawable.ic_aikon_baseline_logo, 512)

            }?.onFailure {
                Log.e(TAG, "Extra get Exc : $it")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notifMan = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                with(
                    NotificationChannel(
                        AikonRes.getString(R.string.notification_channel_name),
                        AikonRes.getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_MIN
                    )
                ) {
                    setShowBadge(true)
                    enableVibration(true)
                    enableLights(false)
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                    notifMan.createNotificationChannel(this)
                }
            }

            val stopIntent = Intent(this, ForegroundService::class.java)
            stopIntent.action = ACTION_STOP
            val pendingStopIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(this, 0, stopIntent, 0)
            } else {
                PendingIntent.getService(this, 0, stopIntent, 0)
            }

            val notification = getNotificationBuilder()
                .setContentText(AikonRes.getString(R.string.msg_notification_preview))
                .setContentIntent(pendingStopIntent)
                .setPublicVersion(getNotificationBuilder().build())
                .build()

            startForeground(NOTIFICATION_ID, notification)

            toast(AikonRes.getString(R.string.msg_notification_created))
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
        isRunning = false
    }

}