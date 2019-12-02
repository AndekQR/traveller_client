package com.client.traveller.ui.nearby.placeDetails

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.client.traveller.R
import com.client.traveller.data.network.api.wikipedia.model.Section
import com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse.Originalimage
import com.client.traveller.ui.nearby.NearbyPlacesViewModel
import com.client.traveller.ui.nearby.NearbyPlacesViewModelFactory
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.showProgressBar
import kotlinx.android.synthetic.main.fragment_place_detail.*
import kotlinx.android.synthetic.main.place_detail_dialog.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.net.URLDecoder


class PlaceDetailsDialog : DialogFragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: NearbyPlacesViewModelFactory by instance()
    private lateinit var viewModel: NearbyPlacesViewModel

    companion object {
        const val TAG = "PlaceDialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)

        this.viewModel = activity?.run {
            ViewModelProvider(this, factory).get(NearbyPlacesViewModel::class.java)
        } ?: throw Exception("Invalid activity")

        val title = arguments?.getString("wikiTitle")
        title?.let { this.updateData(it) }
    }

    private fun updateData(title: String) = lifecycleScope.launch {
        progress_bar.showProgressBar()
        val data = this@PlaceDetailsDialog.viewModel.getWikipediaPageSummary(title)
        val sections = this@PlaceDetailsDialog.viewModel.getWikipediaPageSectionsText(title)
        this@PlaceDetailsDialog.updateTitle(data.titles.normalized)
        this@PlaceDetailsDialog.updateDesc(sections, data.extract)
        this@PlaceDetailsDialog.updatePhoto(data.originalimage)
        this@PlaceDetailsDialog.updadeLink(data.contentUrls.mobile.page)
        this@PlaceDetailsDialog.updatePageDate(data.timestamp)
        button_close.setOnClickListener { this@PlaceDetailsDialog.dismiss() }
        progress_bar.hideProgressBar()
    }

    private fun updatePageDate(timestamp: String) {
        val dateString = timestamp.replace("T", " ").replace("Z", "")
        date.text = dateString
    }

    private fun updadeLink(page: String) {
        link.text = URLDecoder.decode(page, "UTF-8")
        link.setOnClickListener {
            val clipboardManager =
                context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(link.text, link.text)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(context, getString(R.string.text_copied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePhoto(originalimage: Originalimage) {
        originalimage.source?.let {
            Glide.with(view!!).load(originalimage.source).into(image)
        }
    }

    private fun updateDesc(
        sections: List<Section>,
        extract: String
    ) {
        val descLayout = description

        val extractTextView = TextView(context)
        extractTextView.setTypeface(null, Typeface.ITALIC)
        extractTextView.text = extract
        var params =
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        params.setMargins(0, 0, 0, 15)
        extractTextView.layoutParams = params
        descLayout.addView(extractTextView)

        sections.forEach {
            val title = TextView(context)
            title.setTypeface(null, Typeface.BOLD)
            title.textSize = 20F
            title.text = it.title
            params =
                LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            params.setMargins(10, 0, 0, 4)
            title.layoutParams = params
            descLayout.addView(title)

            val text = TextView(context)
            text.text = it.text
            params =
                LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            params.setMargins(0, 0, 0, 15)
            text.layoutParams = params
            descLayout.addView(text)
        }
    }

    private fun updateTitle(title: String) {
        dialog_title.text = title
    }

    override fun onStart() {
        super.onStart()

        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog!!.window?.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.place_detail_dialog, container, false)
    }


}