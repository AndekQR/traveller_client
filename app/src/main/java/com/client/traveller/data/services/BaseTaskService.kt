package com.client.traveller.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.client.traveller.R

//https://github.com/firebase/quickstart-android/blob/master/storage/app/src/main/java/com/google/firebase/quickstart/firebasestorage/kotlin/MyBaseTaskService.kt
abstract class BaseTaskService : Service() {

    companion object {
        private const val CHANNEL_ID_DEFAULT = "default"
        internal const val PROGRESS_NOTIFICATION_ID = 0
        internal const val FINISHED_NOTIFICATION_ID = 1
        private const val TAG = "BaseTaskService"
    }

    private var numberTasks = 0

    private val manager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun taskStarted() {
        this.changeNumberOfTasks(1)
    }

    fun taskCompleted() {
        this.changeNumberOfTasks(-1)
    }

    private fun changeNumberOfTasks(i: Int) {
        numberTasks += i

        //gdy nie ma zadań, zatrzymaj działanie serwisu
        if (numberTasks <= 0) {
            stopSelf()
        }
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

    /**
     * Metoda pokazuje notifikacje z paskiem postępu
     */
    protected fun showProgressNotification(
        caption: String,
        completedUnits: Long,
        totalUnits: Long
    ) {
        var percentComplete = 0
        if (totalUnits > 0)
            percentComplete = (100 * completedUnits / totalUnits).toInt() //procent

        this.createDefaultChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.drawable.ic_cloud_upload)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(caption)
            .setProgress(100, percentComplete, false)
            .setOngoing(true) //notifikacja jest powyżej innych i nie można jej zamknąć
            .setAutoCancel(false)

        manager.notify(PROGRESS_NOTIFICATION_ID, builder.build())
    }

    /**
     * Metoda pokazuje powiadomienie a zakończonym zadaniu
     */
    protected fun showFinishedNotification(caption: String, intent: Intent, success: Boolean) {
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val icon = if (success) R.drawable.ic_check else R.drawable.ic_error

        this.createDefaultChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(icon)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(caption)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        manager.notify(FINISHED_NOTIFICATION_ID, builder.build())
    }

    protected fun dismissProgressNotification() {
        manager.cancel(PROGRESS_NOTIFICATION_ID)
    }

}