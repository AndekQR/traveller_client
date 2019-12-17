package com.client.traveller.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.R
import com.client.traveller.ui.dialog.Dialog
import com.client.traveller.ui.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.analytics.FirebaseAnalytics
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class AuthActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    companion object {
        private val REQUIRED_PERMISSIONS = listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = this.run {
            ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        }


        viewModel.currentUser.observe(this, Observer { user ->
            if (user != null) {
                // gdy zalogujemy się jedną z trzech metod to tu nas przekieruje do HomeActivity
                Intent(this, HomeActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                    this.finish()
                }
            }

            //sprawdza czy użytkownik google już jest zalogowany w aplikacji
            // po wylogowaniu ta wartość staje się nullem
            val signInAccountGoogle = GoogleSignIn.getLastSignedInAccount(this)
            if (signInAccountGoogle != null && user == null) {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.METHOD, "Google")
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)

                viewModel.loginJustLocalGoogle(signInAccountGoogle)
            }
        })

        this.requestPermissions()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    /**
     * Metoda sprawdza czy potrzebne uprawnienia są przyznane, jeżeli nie to prosi użytkownika o nie.
     * Potrzebne uprawnienia są zawarte w [REQUIRED_PERMISSIONS]
     */
    private fun requestPermissions() {
        val permissionsNotGranted = mutableListOf<String>()

        REQUIRED_PERMISSIONS.forEach { permission ->
            val result = ActivityCompat.checkSelfPermission(this, permission)
            if (result == PackageManager.PERMISSION_DENIED)
                permissionsNotGranted.add(permission)
        }

        if (permissionsNotGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNotGranted.toTypedArray(), 100)
        }

    }

    /**
     * Metoda sprawdza czy użytkownik przyznał wszystkie uprawnienia, jeżeli nie to zostaje wyświetlony odpowiedni komunikat
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            100 -> {
                for (i in permissions.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Dialog.Builder()
                            .addTitle(getString(R.string.warning))
                            .addMessage(getString(R.string.not_all_permissions_granted))
                            .addPositiveButton("Ok") { dialog ->
                                dialog.dismiss()
                            }
                            .build(supportFragmentManager, javaClass.simpleName)
                    }
                }
            }
        }
    }


}
