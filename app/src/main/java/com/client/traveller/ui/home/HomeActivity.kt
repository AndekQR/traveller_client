package com.client.traveller.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.BuildConfig
import com.client.traveller.R
import com.client.traveller.ui.dialog.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.android.synthetic.main.fragment_home.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class HomeActivity : AppCompatActivity(),
    KodeinAware {

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    private var doubleBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        if (intent != null)
            this.getDynamicLinks()

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
            return
        } else if (doubleBack) {
            super.onBackPressed()
            return
        }

        this.doubleBack = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBack = false }, 2000)
    }

    /**
     * Uprawnienia lokalizacji są także sprawdzane w [LocationProviderImpl] z kodem 121
     * tutaj jest sprawdzane czy te uprawnienia zostały przyznane
     * jeżeli tak to zostaje wywołana metoda startLocationUpdates
     * jeżeli nie zostają otworzone ustawienia gdzie możemy zmienić uprawnienia [openSettings]
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            121 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.startLocationUpdates()
                } else {
                    openSettings()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(
            "package",
            BuildConfig.APPLICATION_ID, null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    /**
     * wywoływana po ponownym stworzeniu aktywności
     */
    override fun onResume() {
        super.onResume()

        if (viewModel.sendingLocationData()) {
            viewModel.startLocationUpdates()
        }
    }

    /**
     * wywoływana gdy aktywność przejdzie na drugi plan
     */
    override fun onPause() {
        super.onPause()

        if (viewModel.sendingLocationData()) {
            viewModel.stopLocationUpdates()
        }
    }

    //TODO trzeba pprawić metodę
    private fun getDynamicLinks() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) {
                var deepLink: Uri? = null

                if (it != null) {
                    Log.e(
                        javaClass.simpleName,
                        "dynamic Link working!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                    )
//                    FirebaseAuth.getInstance().applyActionCode()
                    deepLink = it.link
                    viewModel.setEmailVerified()
                    Dialog.Builder()
                        .addMessage(getString(R.string.post_email_verification_success))
                        .addPositiveButton("Ok", View.OnClickListener {
                            val dialog =
                                supportFragmentManager.findFragmentByTag(javaClass.simpleName) as Dialog?
                            dialog?.dismiss()
                        })
                        .build(supportFragmentManager, javaClass.simpleName)
                } else {
                    if (intent.data != null) {
                        this.handleLink(intent.data)
                    }
                }
            }
            .addOnFailureListener {
                Log.e(javaClass.simpleName, it.localizedMessage)
            }
    }

    //https://travellersystems.page.link?
// link=https://traveller-249409.firebaseapp.com/__/auth/action?
// apiKey%3DAIzaSyCrwQqjOn5v4BDdkKCZHMmmav1YEzvaq5s%26mode%3D
// verifyEmail%26oobCode%3DL5SpczQtw2sKuIaCPOu8s9CFcziz3Cdmo1KB9JLWY5UAAAFtJizsbA%26
// continueUrl%3Dhttps://travellersystems.page.link/verify?email%253Ddaniellegawiec20@gmail.com%26lang%3Dpl&
// apn=com.client.traveller&amv
    private fun handleLink(uri: Uri?) {

        if (uri == null)
            return

        val link = Uri.parse(uri.getQueryParameter("link"))

        var mode = link.getQueryParameter("mode")
        var actionCode = link.getQueryParameter("oobCode")
        var apiKey = link.getQueryParameter("apiKey")
        var continueUrl = link.getQueryParameter("continueUrl")

        if (mode == "verifyEmail" && actionCode != null) {
            viewModel.setEmailVerified()
            FirebaseAuth.getInstance().applyActionCode(actionCode)
                .addOnSuccessListener {
                    Dialog.Builder()
                        .addMessage(getString(R.string.post_email_verification_success))
                        .addPositiveButton("Ok", View.OnClickListener {
                            val dialog =
                                supportFragmentManager.findFragmentByTag(javaClass.simpleName) as Dialog?
                            dialog?.dismiss()
                        })
                        .build(supportFragmentManager, javaClass.simpleName)
                }
                .addOnFailureListener {
                    Dialog.Builder()
                        .addTitle(getString(R.string.post_email_verification_fail))
                        .addMessage(it.localizedMessage)
                        .addPositiveButton("Ok", View.OnClickListener {
                            val dialog =
                                supportFragmentManager.findFragmentByTag(javaClass.simpleName) as Dialog?
                            dialog?.dismiss()
                        })
                        .build(supportFragmentManager, javaClass.simpleName)
                }

        }
    }

}
