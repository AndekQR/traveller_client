package com.client.traveller.ui.dialogs

import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

import com.client.traveller.R
import java.lang.IllegalStateException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Dialog.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Dialog.newInstance] factory method to
 * create an instance of this fragment.
 *
 *  // onCreate --> (onCreateDialog) --> onCreateView --> onActivityCreated
 */
class Dialog : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(message)
                .setPositiveButton("Ok") { dialog, id ->

                }
            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog, container, false)
    }

    override fun onDetach() {
        super.onDetach()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param message Parameter 1.
         * @return A new instance of fragment Dialog.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(message: String) =
            Dialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, message)
                }
            }
    }
}
