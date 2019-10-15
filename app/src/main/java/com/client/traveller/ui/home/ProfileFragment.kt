package com.client.traveller.ui.home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.client.traveller.R
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.services.UploadService
import com.client.traveller.ui.dialog.Dialog
import com.client.traveller.ui.util.Constants.Companion.KEY_FILE_URI
import com.client.traveller.ui.util.Constants.Companion.READ_REQUEST_CODE
import com.client.traveller.ui.util.ScopedFragment
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.showProgressBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ProfileFragment : ScopedFragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    private var fileUri: Uri? = null
    private var downloadUrl: Uri? = null
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            fileUri = it.getParcelable(KEY_FILE_URI)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apply_button.setOnClickListener {
            progress_bar.showProgressBar()
            this.updateProfile()
        }

        cancel.setOnClickListener {
            downloadUrl = null
            fileUri = null
            Navigation.findNavController(view).navigate(R.id.homeFragment)
        }

        avatar.setOnClickListener {
            this.performFileSearch()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //wynik działania UploadService
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    UploadService.UPLOAD_COMPLETED, UploadService.UPLOAD_ERROR -> onUploadResultIntent(
                        intent
                    )
                }
            }
        }

        viewModel = activity?.run {
            ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        } ?: throw Exception("Invalid activity")

        viewModel.getLoggedInUser().observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                this.currentUser = user
                this.updateProfileViewData()
            }
        })
    }

    private fun updateProfileViewData() = launch {
        if (fileUri != null)
            Glide.with(this@ProfileFragment).load(fileUri).into(avatar)
        else
            Glide.with(this@ProfileFragment).load(currentUser.image).into(avatar)

        displayName.setText(currentUser.displayName)
        email.setText(currentUser.email)
    }

    private fun updateProfile() {

        var userDataToUpdate = currentUser
        userDataToUpdate.displayName = displayName.text.toString()
        userDataToUpdate = this.checkEmail(userDataToUpdate, email.text.toString())

        fileUri?.let {
            // image/png
            val type =
                DocumentFile.fromSingleUri(context!!, fileUri!!)?.type?.substringAfterLast("/")
            if (type != null && (type.equals("png") || type.equals("jpg") || type.equals("jpeg"))) {
                val fileName = currentUser.idUserFirebase + "." + type
                this.uploadFromUri(it, fileName)
            }
        }

        launch {
            viewModel.updateProfile(userDataToUpdate)
        }
        progress_bar.hideProgressBar()
        Dialog.Builder()
            .addMessage(getString(R.string.changes_in_minutes))
            .addPositiveButton("ok"){
                it.dismiss()
            }
            .build(fragmentManager, javaClass.simpleName)

    }

    private fun updateUserAvatar() = launch {
        if (downloadUrl != null) {
            viewModel.updateAvatar(currentUser, downloadUrl.toString())
        }
    }

    private fun checkEmail(userDataToUpdate: User, email: String): User {
        if (userDataToUpdate.email.equals(email))
            return userDataToUpdate

        userDataToUpdate.email = email
        userDataToUpdate.verified = false

        launch {
            viewModel.sendEmailVerification(FirebaseAuth.getInstance().currentUser)
        }

        return userDataToUpdate
    }

    /**
     * Wywoływana gdy aktywność wraca na pierwszy plan, lecz tylko wtedy gdy nie została usunięta z pamięci
     * oraz przy uruchamianiu aktywności
     */
    override fun onStart() {
        super.onStart()

        context?.let {
            val manager = LocalBroadcastManager.getInstance(it)
            manager.registerReceiver(broadcastReceiver, UploadService.intentFilter)
        }
    }

    /**
     * Wywoływana gdy aktywność nie jest widoczna
     */
    override fun onStop() {
        super.onStop()

        context?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(KEY_FILE_URI, fileUri)
    }

    private fun onUploadResultIntent(intent: Intent) {
        fileUri = intent.getParcelableExtra(UploadService.EXTRA_FILE_URI)
        downloadUrl = intent.getParcelableExtra(UploadService.EXTRA_DOWNLOAD_URL)
        // aktualizcja avatara musi być po wgraniu go na storage
        this.updateUserAvatar()
        this.updateProfileViewData()
        progress_bar.hideProgressBar()
    }

    /**
     * uruchamia serwis [UploadService] który w tyle wtgrywa plik do storage
     */
    private fun uploadFromUri(uploadUri: Uri, fileName: String) {

        fileUri = uploadUri

        activity?.let {
            it.startService(
                Intent(it, UploadService::class.java)
                    .putExtra(UploadService.EXTRA_FILE_URI, uploadUri)
                    .putExtra(UploadService.EXTRA_FILE_NAME, fileName)
                    .setAction(UploadService.ACTION_UPLOAD)
            )
        }
    }

    private fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            //pliki które mogą być otwarte
            addCategory(Intent.CATEGORY_OPENABLE)
            //tylko zdjęcia
            type = "image/*"
        }
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //po wybraniu obrazu zostaje on wgrany do storage
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data.also { uri ->
                this.fileUri = uri
                this.updateProfileViewData()
            }
        }
    }

}
