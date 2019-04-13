package com.hsuanparty.unbox_parity.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.NotificationManager
import android.media.RingtoneManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.view.ui.MainActivity





class MyFirebaseMessagingService: FirebaseMessagingService() {

    companion object {
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }

    private var videoUrl: String? = null
    private var messageTitle: String? = null
    private var messageBody: String? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        val keys = remoteMessage?.data?.keys

        if (keys != null) {
            for (s in keys) {
                if (s == "video") {
                    videoUrl = remoteMessage.data!![s]
                    LogMessage.D(TAG, "video url: $videoUrl")
                }
            }
        }

        messageBody = remoteMessage?.notification?.body
//        messageTitle = remoteMessage?.notification?.title
        LogMessage.D(TAG, "onMessageReceived(), title: $messageTitle, body: $messageBody")

        sendNotification()
    }

    private fun sendNotification() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_notification)
            .setLargeIcon(
                BitmapFactory.decodeResource(applicationContext.resources,
                R.mipmap.ic_notification))
//            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}