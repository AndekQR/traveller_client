package com.client.traveller.ui.auth

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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.progress_bar.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class LoginFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
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

        new_account.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.registerFragment))
        password_reset_button.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.passwordResetFragment))
        login_button.setOnClickListener {
            this.onLoginButtonClick()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }

        val TAG = "LOGIN_FRAGMENT"
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
                    viewModel.logInUser(task.result?.user)
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
