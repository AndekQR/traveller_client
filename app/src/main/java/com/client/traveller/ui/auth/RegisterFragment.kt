package com.client.traveller.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.client.traveller.R
import com.client.traveller.ui.dialog.Dialog
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.showProgressBar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.progress_bar.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class RegisterFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // dzięki takiej inicializacji jeden viewmodel jest współdzielony przez wszystkie fragmenty
        viewModel = activity?.run {
            ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        } ?: throw Exception("Invalid activity")

        activity?.let {
            firebaseAnalytics = FirebaseAnalytics.getInstance(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        to_login_button.setOnClickListener {
            val options = NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build()

            it.findNavController().navigate(R.id.loginFragment, null, options)
        }
        register_button.setOnClickListener {
            this.onSignUpButtonClick()
        }
    }

    /**
     * metoda createUserWithEmailAndPassword wysyła na serwery google passy
     * następnie profil użytkownika na firebase jest aktualizowany o jego imię
     * a następnie dane są zapisywane w lokalnej bazie danych
     */
    private fun onSignUpButtonClick() {

        progress_bar.showProgressBar()

        val email = email.text.toString().trim()
        val password = password.text.toString()
        val displayName = displayName.text.toString().trim()
        val confirmPassword = re_password.text.toString()

        if (!viewModel.validate(email, password, displayName, confirmPassword)) {
            progress_bar.hideProgressBar()
            Toast.makeText(activity, "Niepoprawne dane", Toast.LENGTH_LONG).show()
            return
        }

        viewModel.createUserNormal(
            email,
            password,
            displayName
        ) { isSuccessful, exception, firebaseUser ->
            if (isSuccessful && firebaseUser != null) {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.METHOD, "Normal")
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()

                viewModel.updateProfile(
                    firebaseUser,
                    profileUpdates
                ) { isSuccessfulProfileUpdates, exceptionProfileUpdates ->
                    if (!isSuccessfulProfileUpdates) {
                        Dialog.Builder()
                            .addTitle(getString(R.string.something_went_wrong))
                            .addMessage(exceptionProfileUpdates?.message!!)
                            .addPositiveButton("ok") { dialog ->
                                dialog.dismiss()
                            }
                            .build(parentFragmentManager, javaClass.simpleName)
                    }
                }

                viewModel.sendEmailVerification(firebaseUser)
                progress_bar.hideProgressBar()

            } else {
                progress_bar.hideProgressBar()
                Log.e(javaClass.simpleName, exception?.message)
                Dialog.Builder()
                    .addTitle(getString(R.string.something_went_wrong))
                    .addMessage(exception?.message!!)
                    .addPositiveButton("ok") { dialog ->
                        dialog.dismiss()
                    }
                    .build(parentFragmentManager, javaClass.simpleName)
            }
        }
    }
}
