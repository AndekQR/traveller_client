package com.client.traveller.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.client.traveller.R
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.showProgressBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.progress_bar.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class RegisterFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        // dzięki takiej inicializacji jeden viewmodel jest współdzielony przez wszystkie fragmenty
        viewModel = activity?.run {
            ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        } ?: throw Exception("Invalid activity")
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

    companion object {
        @JvmStatic
        fun newInstance(): RegisterFragment {
            return RegisterFragment()
        }

        val TAG = "REGISTER_FRAGMENT"
    }

    /**
     * metoda createUserWithEmailAndPassword wysyła na serwery google passy
     * następnie profil użytkownika na firebase jest aktualizowany o jego imię
     * a następnie dane są zapisywane w lokalnej bazie danych
     */
    private fun onSignUpButtonClick() {

        progress_bar_background.showProgressBar()

        val email = email.text.toString()
        val password = password.text.toString()
        val displayName = displayName.text.toString()
        val confirmPassword = re_password.text.toString()

        if (!viewModel.validate(email, password, displayName, confirmPassword)) {
            progress_bar_background.hideProgressBar()
            Toast.makeText(activity, "Niepoprawne dane", Toast.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    this.updateProfile(task.result?.user, profileUpdates)
                    viewModel.logInUser(task.result?.user)
                    viewModel.sendEmailVerification(task.result?.user)
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

    /**
     * Aktualizacja usera w firebase
     *
     * @param user użytkownik do aktualizacji
     * @param profileUpdates zaktualizowane dane
     */
    private fun updateProfile(user: FirebaseUser?, profileUpdates: UserProfileChangeRequest?) {
        if (user != null && profileUpdates != null) {
            user.updateProfile(profileUpdates)
        }
    }
}
