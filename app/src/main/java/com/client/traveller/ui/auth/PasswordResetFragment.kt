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
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.client.traveller.R
import com.client.traveller.ui.dialog.Dialog
import com.client.traveller.ui.util.hideProgressBar
import kotlinx.android.synthetic.main.fragment_password_reset.*
import kotlinx.android.synthetic.main.progress_bar.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


/**
 * Fragment zarządza resetowaniem hasła
 */
class PasswordResetFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        } ?: throw Exception("Invalid activity")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        password_reset_send.setOnClickListener {
            val email = email_password_reset.text.toString()
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                this.onSendButtonClick(email)
            else {
                Toast.makeText(
                    activity,
                    getString(R.string.password_reset_wrong_email),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        password_reset_cancel.setOnClickListener {
            // powoduje że z fragmentu podanego jako pierwszy argment wychodzimy z aplikacji. Usuwany wszystkie fragmentu ze stosu
            val options = NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build()

            it.findNavController().navigate(R.id.loginFragment, null, options)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_password_reset, container, false)
    }

    /**
     * Statyczna metoda do tworzenia nowych instancji tego fragmentu
     * [TAG] unikalny tag tego fragmentu ( po nim możemy go wyszukać)
     */
    companion object {
        @JvmStatic
        fun newInstance(): PasswordResetFragment {
            return PasswordResetFragment()
        }

        val TAG = "PASSWORD_RESET_FRAGMENT"
    }

    /**
     * Listener, kiedy w [PasswordResetFragment] zostanie naciśnięty przycik wyślij to zostanie uruchomiona ta metoda
     * Zostaje tu usunięty [PasswordResetFragment] i przywrócony [AuthActivity]
     * Jeżeli został podany błędny email to zostanie wyświetlony odpowiedni komunikat
     * Walidacja jest przeprowadzana w [PasswordResetFragment]
     */
    private fun onSendButtonClick(email: String?) {

        if (email != null) {
            viewModel.resetPassword(email) { isSuccessful, exception ->
                progress_bar.hideProgressBar()
                if (!isSuccessful) {
                    Dialog.Builder()
                        .addTitle("Wiadomość z linkiem resetującym hasło nie została wysłana")
                        .addMessage(" ${exception?.localizedMessage}")
                        .addPositiveButton("Ok") { dialog ->
                            dialog.dismiss()
                            val options = NavOptions.Builder()
                                .setPopUpTo(R.id.loginFragment, true)
                                .build()

                            val view = view
                            if (view != null)
                                Navigation.findNavController(view).navigate(
                                    R.id.loginFragment,
                                    null,
                                    options
                                )
                            else Log.e(javaClass.simpleName, "View is null")
                        }
                        .build(fragmentManager, javaClass.simpleName)
                } else {
                    Dialog.Builder()
                        .addMessage("Wiadomość z linkiem resetującym hasło została wysłana na email")
                        .addPositiveButton("Ok") { dialog ->
                            dialog.dismiss()
                            val options = NavOptions.Builder()
                                .setPopUpTo(R.id.loginFragment, true)
                                .build()
                            val view = view
                            if (view != null)
                                Navigation.findNavController(view).navigate(
                                    R.id.loginFragment,
                                    null,
                                    options
                                )
                            else Log.e(javaClass.simpleName, "View is null")
                        }
                        .build(fragmentManager, javaClass.simpleName)
                }
            }
        } else {
            Log.i(javaClass.simpleName, "PasswordResetFragment cancel click")
        }
    }
}
