package com.client.traveller.data.services

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.client.traveller.data.network.firebase.firestore.Tokens
import com.client.traveller.data.network.firebase.firestore.model.Token
import com.client.traveller.ui.chat.messeages.MesseageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.Activity
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID_DEFAULT = "default"
        private const val NOTIFICATION_REQUEST_CODE = 978
    }

    private val manager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createDefaultChannel() {
        //Powyżej androida O jest potrzebny notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                "Default",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        this.saveToken(token)
    }

    private fun saveToken(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            FirebaseFirestore.getInstance().collection(Tokens.COLLECTION_NAME).document(it.uid)
                .set(Token(it.uid, token = token))
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // TODO trzeba sprawdzać czy jest w MesseageActivity
        //  i czy jest w tej samej wycieczce
        val sentTo = message.data["sentTo"]
        val currentUser = FirebaseAuth.getInstance().currentUser
        val am = applicationContext.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        val currentActivity = am.appTasks.first().taskInfo.topActivity
        if (currentUser != null && sentTo == currentUser.uid)
            sendNotification(message)
    }

    private fun sendNotification(message: RemoteMessage) {
        val sentFrom = message.data["sentFrom"]
        val icon = message.data["icon"]
        val title = message.data["title"]
        val body = message.data["body"]
        val chatUid = message.data["chatUid"]
        val tripUid = message.data["tripUid"]

        val intent = Intent(this, MesseageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("chatId", chatUid)
        bundle.putString("tripUid", tripUid)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        this.createDefaultChannel()
        val builder =  NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(Integer.parseInt(icon))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        manager.notify(0, builder.build())

    }
}