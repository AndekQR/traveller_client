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
    private var positivieButtonListener: View.OnClickListener? = null
    private var negativeButtonListener: View.OnClickListener? = null
    private var negativeButtonText: String? = null

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
            if (positivieButtonListener != null)
                view.positive_button.setOnClickListener(positivieButtonListener)
        } ?: run {
            view.positive_button.visibility = View.GONE
        }

        negativeButtonText?.let {
            view.negative_button.text = negativeButtonText
            if (negativeButtonListener != null)
                view.negative_button.setOnClickListener(negativeButtonListener)
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
            positivieButtonListener: View.OnClickListener?,
            negativeButtonText: String?,
            negativeButtonListener: View.OnClickListener?
        ): Dialog {

            val dialog = Dialog()
            dialog.title = title
            dialog.message = message
            dialog.positivieButtonText = positivieButtonText
            dialog.positivieButtonListener = positivieButtonListener
            dialog.negativeButtonText = negativeButtonText
            dialog.negativeButtonListener = negativeButtonListener
            return dialog

        }
    }

    class Builder {

        private var title: String? = null
        private var message: String? = null
        private var positivieButtonText: String? = null
        private var positivieButtonListener: View.OnClickListener? = null
        private var negativeButtonListener: View.OnClickListener? = null
        private var negativeButtonText: String? = null

        fun addTitle(title: String) = apply {
            this.title = title
        }

        fun addMessage(message: String) = apply {
            this.message = message
        }

        fun addPositiveButton(title: String, listener: View.OnClickListener) = apply {
            this.positivieButtonText = title
            this.positivieButtonListener = listener
        }

        fun addNegativeButton(title: String, listener: View.OnClickListener) = apply {
            this.negativeButtonText = title
            this.negativeButtonListener = listener
        }

        fun build(manager: FragmentManager?, tag: String) {
            val dialog = newInstance(
                title,
                message,
                positivieButtonText,
                positivieButtonListener,
                negativeButtonText,
                negativeButtonListener
            )

            if (manager != null)
                dialog.show(manager, tag)
            else
                throw Exception("Fragment manager must not be null")
        }
    }
}
