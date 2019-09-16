package com.client.traveller.ui.auth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.R
import com.client.traveller.ui.dialog.Dialog
import com.client.traveller.ui.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.security.MessageDigest


class AuthActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel

    private val REQUIRED_PERMISSIONS = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = this.run {
            ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        }

        //sprawdza czy użytkownik google już jest zalogowany w aplikacji
        // po wylogowaniu ta wartość staje się nullem
        val signInAccountGoogle = GoogleSignIn.getLastSignedInAccount(this)

        viewModel.getLoggedInUser().observe(this, Observer { user ->
            if (user != null) {
                Intent(this, HomeActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }

            if (signInAccountGoogle != null && user == null){
                viewModel.logInGoogleUser(signInAccountGoogle)
            }
        })

        this.requestPermissions()
        generateSSHKey(this)
    }

    @SuppressLint("NewApi")
    fun generateSSHKey(context: Context){
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(java.util.Base64.getEncoder().encode(md.digest()))
                Log.i("AppLog", "key:$hashKey=")
            }
        } catch (e: Exception) {
            Log.e("AppLog", "error:", e)
        }

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
                            .addTitle("Ostrzeżenie!")
                            .addMessage("Wymagane uprawnienia nie zostały przyznane. Aplikacja może działać w niewłaściwy sposób.")
                            .addPositiveButton("Ok", View.OnClickListener {
                                val dialog = supportFragmentManager.findFragmentByTag(javaClass.simpleName) as Dialog
                                dialog.dismiss()
                            })
                            .build(supportFragmentManager, javaClass.simpleName)
                    }
                }
            }
        }
    }


}
