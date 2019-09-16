package com.client.traveller.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.client.traveller.R
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.showProgressBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.progress_bar.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import android.util.Log
import com.client.traveller.ui.dialog.Dialog
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider


class LoginFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: AuthViewModel
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 140
    private lateinit var callbackManagerFacebook: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = activity?.run {
            ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        } ?: throw Exception("Invalid activity")

        //dane jakie google zwróci po zalogowaniu, tymidanymi inicjalizujemy klienta logowania
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(activity!!, gso)

        callbackManagerFacebook = CallbackManager.Factory.create()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        new_account.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.registerFragment))
        password_reset_button.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.passwordResetFragment))
        login_button.setOnClickListener {
            this.onLoginButtonClick()
        }
        google_sign_in_button.setOnClickListener {
            this.googleSignIn()
        }
        login_button_facebook.fragment = this
        login_button_facebook.setReadPermissions("email", "public_profile")
        login_button_facebook.registerCallback(callbackManagerFacebook, object : FacebookCallback<LoginResult>{
            override fun onError(error: FacebookException?) {
                Log.e(javaClass.simpleName, error?.localizedMessage)
                Dialog.Builder()
                    .addMessage("Logowanie nieudane. Spróbuj ponowanie za kilka minut")
                    .addPositiveButton("Ok", View.OnClickListener {
                        val dialog =
                            fragmentManager?.findFragmentByTag(javaClass.simpleName) as Dialog?
                        dialog?.dismiss()
                    })
                    .build(fragmentManager, javaClass.simpleName)
            }

            override fun onCancel() {

            }

            override fun onSuccess(result: LoginResult?) {
                if (result != null)
                    handleFacebookAccessToken(result.accessToken)
            }
        })
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken){
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener{
                if (it.isSuccessful){
                    viewModel.logInEmailUser(auth.currentUser)
                }
                else{
                    Log.e(javaClass.simpleName, it.exception?.localizedMessage)
                    Dialog.Builder()
                        .addMessage("Logowanie nieudane")
                        .addPositiveButton("Ok", View.OnClickListener {
                            val dialog =
                                fragmentManager?.findFragmentByTag(javaClass.simpleName) as Dialog?
                            dialog?.dismiss()
                        })
                        .build(fragmentManager, javaClass.simpleName)
                }
            }
    }

    private fun googleSignIn(){
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            this.handleSignInResult(task)
        }

        callbackManagerFacebook.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null)
                viewModel.logInGoogleUser(account)
            else
                Log.e(javaClass.simpleName, "google account == null")
        }
        catch (ex: ApiException){
            Log.e(javaClass.simpleName, "signInResult:failed code=" + ex.statusCode)
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

    private fun onLoginButtonClick() {
        progress_bar_background.showProgressBar()

        val email = email.text.toString()
        val password = password.text.toString()

        /**
         * Jeżeli spełnia walidację to nie wchodzi w if
         */
        if (!viewModel.validate(email, password)) {
            progress_bar_background.hideProgressBar()
            Toast.makeText(activity, "Niepoprawne dane", Toast.LENGTH_LONG).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModel.logInEmailUser(task.result?.user)
                    progress_bar_background.hideProgressBar()

                } else {
                    progress_bar_background.hideProgressBar()
                    Toast.makeText(activity, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }


}
