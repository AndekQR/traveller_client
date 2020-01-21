package com.client.traveller.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

import com.client.traveller.R
import kotlinx.android.synthetic.main.fragment_dialog.view.*

/**
 * Klasa do tworzenia custom dialogów z zdefiniowanym wyglądem
 * Dialog tworzy się za pomocą buildera
 */
class Dialog : DialogFragment() {

    private var title: String? = null
    private var message: String? = null
    private var positivieButtonText: String? = null
    private var negativeButtonText: String? = null
    private var negativeButtonAction: ((Dialog) -> Unit)? = null
    private var positiveButtonAction: ((Dialog) -> Unit)? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog, container, false)
        this.initializeView(view)
        return view
    }

    private fun initializeView(view: View) {


        title?.let {
            view.dialog_title.text = title
        } ?: run {
            view.dialog_title.visibility = View.GONE
        }

        message?.let {
            view.dialog_message.text = message
        } ?: run {
            view.dialog_message.visibility = View.GONE
        }

        positivieButtonText?.let {
            view.positive_button.text = positivieButtonText
            view.positive_button.setOnClickListener {
                positiveButtonAction?.invoke(this)
            }
        } ?: run {
            view.positive_button.visibility = View.GONE
        }

        negativeButtonText?.let {
            view.negative_button.text = negativeButtonText
            view.negative_button.setOnClickListener {
                negativeButtonAction?.invoke(this)
            }
        } ?: run {
            view.negative_button.visibility = View.GONE
        }

        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        Log.e(javaClass.simpleName, view.buttons.childCount.toString())

    }


    companion object {
        @JvmStatic
        fun newInstance(
            title: String?,
            message: String?,
            positivieButtonText: String?,
            negativeButtonText: String?,
            negativeButtonAction: ((Dialog) -> Unit)?,
            positiveButtonAction: ((Dialog) -> Unit)?
        ): Dialog {

            val dialog = Dialog()
            dialog.title = title
            dialog.message = message
            dialog.positivieButtonText = positivieButtonText
            dialog.negativeButtonText = negativeButtonText
            dialog.negativeButtonAction = negativeButtonAction
            dialog.positiveButtonAction = positiveButtonAction
            return dialog

        }
    }

    class Builder {

        private var title: String? = null
        private var message: String? = null
        private var positivieButtonText: String? = null
        private var negativeButtonText: String? = null
        private var negativeButtonAction: ((Dialog) -> Unit)? = null
        private var positiveButtonAction: ((Dialog) -> Unit)? = null


        fun addTitle(title: String) = apply {
            this.title = title
        }

        fun addMessage(message: String) = apply {
            this.message = message
        }

        fun addPositiveButton(title: String, func: (Dialog) -> Unit) = apply {
            this.positivieButtonText = title
            this.positiveButtonAction = func
        }

        fun addNegativeButton(title: String, func: (Dialog) -> Unit) = apply {
            this.negativeButtonText = title
            this.negativeButtonAction = func
        }

        fun build(manager: FragmentManager?, tag: String) {
            val dialog = newInstance(
                title,
                message,
                positivieButtonText,
                negativeButtonText,
                negativeButtonAction,
                positiveButtonAction
            )

            if (manager != null)
                dialog.show(manager, tag)
            else
                throw Exception("Fragment manager must not be null")
        }
    }
}