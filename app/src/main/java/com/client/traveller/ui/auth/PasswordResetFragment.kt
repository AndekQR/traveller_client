package com.client.traveller.ui.auth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.client.traveller.R
import kotlinx.android.synthetic.main.fragment_password_reset.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PasswordResetFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PasswordResetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PasswordResetFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button = password_reset_send
        button.setOnClickListener {
            val email = email_password_reset.text.toString()
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                listener?.onButtonClick(email)
            else{
                Toast.makeText(activity, getString(R.string.password_reset_wrong_email), Toast.LENGTH_SHORT).show()
            }
        }

        val cancelButton = password_reset_cancel
        cancelButton.setOnClickListener {
            listener?.onButtonClick(null)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_reset, container, false)
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onButtonClick(email: String?)
    }

    companion object {
        @JvmStatic
        fun newInstance(): PasswordResetFragment {
            return PasswordResetFragment()
        }
    }
}
