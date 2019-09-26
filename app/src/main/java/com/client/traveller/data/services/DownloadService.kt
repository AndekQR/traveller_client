package com.client.traveller.data.services

import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.client.traveller.R
import com.client.traveller.ui.home.HomeActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class DownloadService : BaseTaskService() {

    companion object {

        private const val TAG = "Storage#DownloadService"

        const val ACTION_DOWNLOAD = "action_download"
        const val DOWNLOAD_COMPLETED = "download_completed"
        const val DOWNLOAD_ERROR = "download_error"

        const val EXTRA_DOWNLOAD_PATH = "extra_download_path"
        const val EXTRA_BYTES_DOWNLOADED = "extra_bytes_downloaded"

        val intentFilter: IntentFilter
            get() {
                val filter = IntentFilter()
                filter.addAction(DOWNLOAD_COMPLETED)
                filter.addAction(DOWNLOAD_ERROR)

                return filter
            }
    }

    private lateinit var storageRef: StorageReference

    override fun onCreate() {
        super.onCreate()

        storageRef = FirebaseStorage.getInstance().reference
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (ACTION_DOWNLOAD == intent.action) {
            val downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH)
            downloadFromPath(downloadPath)
        }

        return START_REDELIVER_INTENT
    }

    private fun downloadFromPath(downloadPath: String) {

        taskStarted()
        showProgressNotification(getString(R.string.progress_downloading), 0, 0)

        // Download and get total bytes
        storageRef.child(downloadPath).getStream { taskSnapshot, inputStream ->
            val totalBytes = taskSnapshot.totalByteCount
            var bytesDownloaded: Long = 0

            val buffer = ByteArray(1024)
            var size: Int = inputStream.read(buffer)

            while (size != -1) {
                bytesDownloaded += size.toLong()
                showProgressNotification(
                    getString(R.string.progress_downloading),
                    bytesDownloaded, totalBytes
                )

                size = inputStream.read(buffer)
            }

            inputStream.close()
        }.addOnSuccessListener { taskSnapshot ->

            // Send success broadcast with number of bytes downloaded
            broadcastDownloadFinished(downloadPath, taskSnapshot.totalByteCount)
            showDownloadFinishedNotification(downloadPath, taskSnapshot.totalByteCount.toInt())

            taskCompleted()
        }.addOnFailureListener { exception ->
            // Send failure broadcast
            broadcastDownloadFinished(downloadPath, -1)
            showDownloadFinishedNotification(downloadPath, -1)

            taskCompleted()
        }
    }

    private fun broadcastDownloadFinished(downloadPath: String, bytesDownloaded: Long): Boolean {
        val success = bytesDownloaded != -1L
        val action = if (success) DOWNLOAD_COMPLETED else DOWNLOAD_ERROR

        val broadcast = Intent(action)
            .putExtra(EXTRA_DOWNLOAD_PATH, downloadPath)
            .putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded)
        return LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(broadcast)
    }

    private fun showDownloadFinishedNotification(downloadPath: String, bytesDownloaded: Int) {
        // Hide the progress notification
        dismissProgressNotification()

        // Make Intent to MainActivity
        val intent = Intent(this, HomeActivity::class.java)
            .putExtra(EXTRA_DOWNLOAD_PATH, downloadPath)
            .putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val success = bytesDownloaded != -1
        val caption = if (success) {
            getString(R.string.download_success)
        } else {
            getString(R.string.download_failure)
        }

        showFinishedNotification(caption, intent, true)
    }
}