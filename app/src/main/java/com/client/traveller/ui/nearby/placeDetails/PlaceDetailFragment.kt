package com.client.traveller.ui.nearby.placeDetails

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.client.traveller.R
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Photo
import com.client.traveller.data.network.api.places.response.placeDetailResponse.Location
import com.client.traveller.data.network.api.places.response.placeDetailResponse.PlaceDetailResponse
import com.client.traveller.ui.home.HomeActivity
import com.client.traveller.ui.nearby.NearbyPlacesViewModel
import com.client.traveller.ui.nearby.NearbyPlacesViewModelFactory
import com.client.traveller.ui.util.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_place_detail.*
import kotlinx.android.synthetic.main.progress_bar.view.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class PlaceDetailFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: NearbyPlacesViewModelFactory by instance()
    private lateinit var viewModel: NearbyPlacesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_place_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, factory).get(NearbyPlacesViewModel::class.java)
        } ?: throw Exception("Invalid activity")

        arguments?.let {
            val placeId = PlaceDetailFragmentArgs.fromBundle(it).placeId
            lifecycleScope.launch {
                val details = viewModel.getPlaceDetails(placeId)
                this@PlaceDetailFragment.bindUI(details)
            }
        }
    }

    private fun bindUI(placeDetails: PlaceDetailResponse) {
        val result = placeDetails.result

        this.updateIcon(result.icon)
        this.updateName(result.name)
        this.updateSearchResults(result.name)
        this.updateMap(result.geometry.location)
        this.initFab(placeDetails.result.geometry.location)
        this.updatePhotos(result.photos)
        this.updateRating(result.rating)
        this.updateContactData(result.formattedAddress, result.website, result.formattedPhoneNumber)
    }

    private fun updateContactData(
        formattedAddressValue: String,
        websiteValue: String,
        formattedPhoneNumberValue: String
    ) {
        address.text = formattedAddressValue
        website.text = websiteValue
        phone.text = formattedPhoneNumberValue

        address.setOnClickListener {
            val clipboardManager =
                context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(address.text, address.text)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(context, getString(R.string.text_copied), Toast.LENGTH_SHORT).show()
        }
        website.setOnClickListener {
            val clipboardManager =
                context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(website.text, website.text)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(context, getString(R.string.text_copied), Toast.LENGTH_SHORT).show()

        }
        phone.setOnClickListener {
            val clipboardManager =
                context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(phone.text, phone.text)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(context, getString(R.string.text_copied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateRating(ratingValue: Double) {
        rating.text = getString(R.string.google_users_rating) + " $ratingValue"
    }

    private fun getScreenSize(): DisplayMetrics {
        return Resources.getSystem().displayMetrics
    }

    private fun updatePhotos(photos: List<Photo>) {
        if (photos == null) return
        when (photos.size) {
            0 -> return
            1 -> {
                val photo =
                    this.viewModel.getPhoto(photos.first().photoReference, photos.first().width)
                photo_1.visibility = View.VISIBLE
                view?.let {
                    Glide.with(it).load(photo)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .centerCrop()
                        .into(photo_1)
                }
                return
            }
            2 -> {
                val photoOne = this.viewModel.getPhoto(
                    photos.first().photoReference,
                    this.getScreenSize().widthPixels
                )
                val photoTwo = this.viewModel.getPhoto(
                    photos[1].photoReference,
                    this.getScreenSize().widthPixels
                )
                photo_1.visibility = View.VISIBLE
                photo_2.visibility = View.VISIBLE
                view?.let {
                    Glide.with(it).load(photoOne)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .centerCrop()
                        .into(photo_1)
                    Glide.with(it).load(photoTwo)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .centerCrop()
                        .into(photo_2)
                }
                return
            }
            else -> {
                val photoOne =
                    this.viewModel.getPhoto(photos.first().photoReference, photos[0].width)
                val photoTwo = this.viewModel.getPhoto(photos[1].photoReference, photos[1].width)
                val photoThree = this.viewModel.getPhoto(photos[2].photoReference, photos[2].width)
                photo_1.visibility = View.VISIBLE
                photo_2.visibility = View.VISIBLE
                photo_3.visibility = View.VISIBLE
                view?.let {
                    Glide.with(it)
                        .load(photoOne)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(photo_1)
                    Glide.with(it)
                        .load(photoTwo)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(photo_2)
                    Glide.with(it)
                        .load(photoThree)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(photo_3)
                }
                return
            }
        }
    }

    private fun initFab(location: Location) {
        fab.setOnClickListener {
            Intent(context, HomeActivity::class.java).also {
                val bundle = Bundle()
                bundle.putString(ActivitiesAction.HOME_ACTIVITY_DRAW_ROAD.name, location.toLatLng().formatToApi())
                it.putExtras(bundle)
                context?.startActivity(it)
            }
        }
    }

    private fun showDialog(title: String) {
        val fullScreenDialog = PlaceDetailsDialog()
        val bundle = Bundle()
        bundle.putString("wikiTitle", title)
        fullScreenDialog.arguments = bundle
        val transaction = parentFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        fullScreenDialog.show(transaction, PlaceDetailsDialog.TAG)
    }


    private fun updateMap(location: Location) {
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.let {
            activity?.let { activity ->
                viewModel.initMap(it, activity, null)
            }
        }
        val latlng = com.google.android.gms.maps.model.LatLng(location.lat, location.lng)
        this.viewModel.centerOnLocation(latlng, true)
        this.viewModel.disableMapDragging()
    }

    private fun updateSearchResults(name: String) = lifecycleScope.launch {
        search_result_layout.progress_bar.showProgressBar()
        link_buttons.visibility = View.GONE
        val prefixes =
            this@PlaceDetailFragment.viewModel.getWikipediaPrefixes(name).query.prefixsearch
        if (prefixes.isNotEmpty()) {
            search_result_layout.visibility = View.VISIBLE
            if (prefixes.size > 6) {
                for (i in 0..6) {
                    val button = this@PlaceDetailFragment.createButton(prefixes[i].title)
                    link_buttons.addView(button)
                    button.setOnClickListener {
                        this@PlaceDetailFragment.showDialog(
                            prefixes[i].title.replace(
                                " ",
                                "_"
                            )
                        )
                    }
                }
            } else {
                prefixes.forEachIndexed { _, prefixsearch ->
                    val button = this@PlaceDetailFragment.createButton(prefixsearch.title)
                    link_buttons.addView(button)
                    button.setOnClickListener {
                        this@PlaceDetailFragment.showDialog(
                            prefixsearch.title.replace(
                                " ",
                                "_"
                            )
                        )
                    }
                }
            }
            search_result_layout.progress_bar.hideProgressBar()
            link_buttons.visibility = View.VISIBLE
        }
    }


    private fun createButton(desc: String): MaterialButton {
        val theme = ContextThemeWrapper(context, R.style.outlinedButton)
        val button = MaterialButton(theme)
        button.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        button.cornerRadius = 10
//        button.setStrokeColorResource(R.color.colorPrimaryDark)
        button.text = desc
        return button
    }

    private fun updateName(nameValue: String) {
        name.text = nameValue
    }

    private fun updateIcon(iconUrl: String) {
        view?.let {
            Glide.with(it).load(iconUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .centerCrop()
                .into(icon)
        }
    }

}


