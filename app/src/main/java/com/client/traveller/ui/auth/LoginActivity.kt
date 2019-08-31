package com.client.traveller.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.R
import com.client.traveller.ui.home.HomeActivity
import com.client.traveller.ui.util.hide
import com.client.traveller.ui.util.show
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class LoginActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private lateinit var auth: FirebaseAuth

    private val REQUIRED_PERMISSIONS = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        viewModel.getLoggedInUser().observe(this, Observer { user ->
            if (user != null) {
                Intent(this, HomeActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        })

        auth = FirebaseAuth.getInstance()

        this.requestPermissions()
    }


    private fun requestPermissions() {
        var permissionsNotGranted = mutableListOf<String>()

        REQUIRED_PERMISSIONS.forEach { permission ->
            var result = ActivityCompat.checkSelfPermission(this, permission)
            if (result == PackageManager.PERMISSION_DENIED)
                permissionsNotGranted.add(permission)
        }

        if (permissionsNotGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNotGranted.toTypedArray(), 100)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            100 -> {
                for (i in permissions.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(
                            this, "Required permission: " + permissions[i] +
                                    " not granted.", Toast.LENGTH_LONG
                        ).show()
                        this.finish()
                    }
                }
            }
        }
    }

    fun toRegister(v: View) {
        Intent(v.context, SignupActivity::class.java).also {
            v.context.startActivity(it)
        }
    }

    /**
     * Akcja po naciśnięciu przycisku zaloguj
     *
     * walidacja polega tylko na sprawdzeniu czy pola nie są puste
     * reszta walidacji jest przeprowadzana po stronie serwera google
     *
     * @param view: musi być aby metoda można była przypisać w xml
     */

    fun onLoginButtonClick(view: View) {
        progress_bar.show()

        val email = email.text.toString()
        val password = password.text.toString()

        /**
         * Jeżeli spełnia walidację to nie wchodzi w if
         */
        if (!viewModel.validate(email, password)) {
            progress_bar.hide()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    viewModel.logInUser(task.result?.user)
                    progress_bar.hide()
                } else {
                    progress_bar.hide()
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

}
