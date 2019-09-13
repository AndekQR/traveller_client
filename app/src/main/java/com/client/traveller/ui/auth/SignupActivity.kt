package com.client.traveller.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.R
import com.client.traveller.ui.home.HomeActivity
import com.client.traveller.ui.util.hide
import com.client.traveller.ui.util.show
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_signup.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SignupActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

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

    }

    /**
     * metoda kierująca użytkownika do panelu logowania
     */
    fun toLogin(v: View) {
        Intent(v.context, LoginActivity::class.java).also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            v.context.startActivity(it)
        }
    }

    /**
     * metoda createUserWithEmailAndPassword wysyła na serwery google passy
     * następnie profil użytkownika na firebase jest aktualizowany o jego imię
     * a następnie dane są zapisywane w lokalnej bazie danych
     */
    fun onSignUpButtonClick(view: View){

        progress_bar.show()

        val email = email.text.toString()
        val password = password.text.toString()
        val displayName = displayName.text.toString()
        val confirmPassword = re_password.text.toString()

        if (!viewModel.validate(email, password, displayName, confirmPassword)){
            progress_bar.hide()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    this.updateProfile(task.result?.user, profileUpdates)
                    viewModel.logInUser(task.result?.user)
                    viewModel.sendEmailVerification(task.result?.user)
                    progress_bar.hide()
                } else{
                    progress_bar.hide()
                    Toast.makeText(this,task.exception?.message , Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Aktualizacja usera w firebase
     *
     * @param user użytkownik do aktualizacji
     * @param profileUpdates zaktualizowane dane
     */
    private fun updateProfile(user: FirebaseUser?, profileUpdates: UserProfileChangeRequest?){
        if (user != null && profileUpdates != null){
            user.updateProfile(profileUpdates)
        }
    }

}
