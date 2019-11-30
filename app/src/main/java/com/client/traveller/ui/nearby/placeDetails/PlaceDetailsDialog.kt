package com.client.traveller.ui.nearby.placeDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.client.traveller.R
import com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse.Originalimage
import com.client.traveller.ui.nearby.NearbyPlacesViewModel
import com.client.traveller.ui.nearby.NearbyPlacesViewModelFactory
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.showProgressBar
import kotlinx.android.synthetic.main.place_detail_dialog.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class PlaceDetailsDialog: DialogFragment(), KodeinAware {

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
        this@PlaceDetailsDialog.updateTitle(data.titles.normalized)
        this@PlaceDetailsDialog.updateDesc(data.extract)
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
        link.text = page
    }

    private fun updatePhoto(originalimage: Originalimage) {
        Glide.with(view!!).load(originalimage.source).into(image)
    }

    private fun updateDesc(extract: String) {
        text.text = extract
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