package com.client.traveller.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.client.traveller.R
import com.client.traveller.ui.dialog.Dialog
import com.client.traveller.ui.util.Constants.Companion.RC_SIGN_IN
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.showProgressBar
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.progress_bar.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class LoginFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManagerFacebook: CallbackManager
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //dane jakie google zwróci po zalogowaniu, tymi danymi inicjalizujemy klienta logowania
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(getString(R.string.google_client_id))
            .build()
        activity?.let {
            mGoogleSignInClient = GoogleSignIn.getClient(it, gso)
        }

        callbackManagerFacebook = CallbackManager.Factory.create()

        activity?.let {
            firebaseAnalytics = FirebaseAnalytics.getInstance(it)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        } ?: throw Exception("Invalid activity")

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

        new_account?.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.registerFragment))
        password_reset_button?.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.passwordResetFragment))
        login_button?.setOnClickListener {
            this.onLoginButtonClick()
        }
        google_sign_in_button?.setOnClickListener {
            progress_bar.showProgressBar()
            this.googleSignIn()
        }
        login_button_facebook?.fragment = this
        login_button_facebook?.setPermissions("email", "public_profile")
        login_button_facebook?.registerCallback(
            callbackManagerFacebook,
            object : FacebookCallback<LoginResult> {
                override fun onError(error: FacebookException?) {
                    progress_bar.hideProgressBar()
                    Log.e(javaClass.simpleName, error?.localizedMessage)
                    Dialog.Builder()
                        .addTitle(getString(R.string.something_went_wrong))
                        .addMessage(error?.message!!)
                        .addPositiveButton("Ok") { dialog ->
                            dialog.dismiss()
                        }
                        .build(fragmentManager, javaClass.simpleName)
                }

                override fun onCancel() {
                    progress_bar.hideProgressBar()
                    Log.e(javaClass.simpleName, "facebook cancel")
                }

                override fun onSuccess(result: LoginResult?) {
                    progress_bar.showProgressBar()
                    Log.e(javaClass.simpleName, "facebook success")
                    result?.let {
                        viewModel.loginUserByFacebook(it.accessToken) { isSuccessful, exception ->
                            progress_bar.hideProgressBar()
                            if (!isSuccessful) {
                                LoginManager.getInstance().logOut()
                                Log.e(javaClass.simpleName, exception?.localizedMessage)
                                Dialog.Builder()
                                    .addMessage(exception?.message!!)
                                    .addPositiveButton("Ok") { dialog ->
                                        dialog.dismiss()
                                    }
                                    .build(fragmentManager, javaClass.simpleName)
                            } else {
                                val bundle = Bundle()
                                bundle.putString(FirebaseAnalytics.Param.METHOD, "facebook")
                                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
                            }
                        }
                    }
                }
            })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(javaClass.simpleName, requestCode.toString())
        if (requestCode == RC_SIGN_IN) {
            this.handleGoogleLogin(data)
        }

        callbackManagerFacebook.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Uruchamia dialog do wyboru konta google
     */
    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun handleGoogleLogin(intent: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        viewModel.loginUserByGoogle(task) { isSuccessful, exception ->
            progress_bar.hideProgressBar()
            if (!isSuccessful) {
                Log.e(javaClass.simpleName, exception?.localizedMessage)
                Dialog.Builder()
                    .addTitle(getString(R.string.something_went_wrong))
                    .addMessage(exception?.message!!)
                    .addPositiveButton("Ok") { dialog ->
                        dialog.dismiss()
                    }
                    .build(fragmentManager, javaClass.simpleName)
            } else {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.METHOD, "Google")
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
            }


        }
    }

    /**
     * Logowanie emailem i hasłem
     * Akcja po naciśnięciu przycisku zaloguj
     *
     * walidacja polega tylko na sprawdzeniu czy pola nie są puste
     * reszta walidacji jest przeprowadzana po stronie serwera google
     */
    private fun onLoginButtonClick() {
        progress_bar.showProgressBar()

        val email = email.text.toString()
        val password = password.text.toString()

        /**
         * Jeżeli spełnia walidację to nie wchodzi w if
         */
        if (!viewModel.validate(email, password)) {
            progress_bar.hideProgressBar()
            Toast.makeText(activity, getString(R.string.Invalid_data), Toast.LENGTH_LONG).show()
            return
        }
        viewModel.loginUser(email, password) { isSuccessful, exception ->
            progress_bar.hideProgressBar()
            if (!isSuccessful) {
                Log.e(javaClass.simpleName, exception?.message)
                Dialog.Builder()
                    .addTitle(getString(R.string.something_went_wrong))
                    .addMessage(exception?.message!!)
                    .addPositiveButton("ok") { dialog ->
                        dialog.dismiss()
                    }
                    .build(fragmentManager, javaClass.simpleName)
            } else {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.METHOD, "Normal")
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
            }
        }
    }


}
