package com.client.traveller.data.services

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.client.traveller.R
import com.client.traveller.data.network.firebase.storage.Avatars
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UploadService : BaseTaskService() {

    companion object {
        private const val TAG = "action_upload"

        const val ACTION_UPLOAD = "action_upload"
        const val UPLOAD_COMPLETED = "upload_completed"
        const val UPLOAD_ERROR = "upload_error"

        const val EXTRA_FILE_URI = "extra_file_uri"
        const val EXTRA_DOWNLOAD_URL = "extra_download_url"

        //z formacie: title.extension
        const val EXTRA_FILE_NAME = "extra_file_name"

        val intentFilter: IntentFilter
            get() {
                val filter = IntentFilter()
                filter.addAction(UPLOAD_COMPLETED)
                filter.addAction(UPLOAD_ERROR)

                return filter
            }
    }

    private lateinit var storageReference: StorageReference

    /**
     * Wywoływana tylko raz, kiedy ten serwis jest tworzony
     */
    override fun onCreate() {
        super.onCreate()

        storageReference = FirebaseStorage.getInstance().reference
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Wywoływana za każdym razem kiedy jest wywoływany ten serwis
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ACTION_UPLOAD == intent.action) {
            val fileUri = intent.getParcelableExtra<Uri>(EXTRA_FILE_URI)
            val fileName = intent.getStringExtra(EXTRA_FILE_NAME)

            this.uploadFromUri(fileUri, fileName)
        }
        return START_REDELIVER_INTENT
    }

    private fun uploadFromUri(fileUri: Uri, fileName: String) {

        taskStarted()
        showProgressNotification(getString(R.string.progress_upload), 0, 0)

        val avatarRef = storageReference.child(Avatars.AVATARS)
            .child(fileName)

        avatarRef.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                showProgressNotification(
                    getString(R.string.progress_upload),
                    taskSnapshot.bytesTransferred,
                    taskSnapshot.totalByteCount
                )
            }.continueWithTask { task ->
                if (!task.isSuccessful)
                    throw task.exception!!

                avatarRef.downloadUrl
            }.addOnSuccessListener { downloadUri ->
                this.broadcastUploadFinished(downloadUri, fileUri)
                dismissProgressNotification()
                taskCompleted()
            }
    }

    private fun getFileType(uri: Uri): String {
        val fileName = uri.lastPathSegment
        return fileName?.substringAfterLast('.') ?: ".png"
    }

    private fun broadcastUploadFinished(downloadUri: Uri?, fileUri: Uri?): Boolean {
        val success = downloadUri != null

        val action = if (success) UPLOAD_COMPLETED else UPLOAD_ERROR

        val broadcast = Intent(action)
            .putExtra(EXTRA_DOWNLOAD_URL, downloadUri)
            .putExtra(EXTRA_FILE_URI, fileUri)
        return LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(broadcast)
    }

}