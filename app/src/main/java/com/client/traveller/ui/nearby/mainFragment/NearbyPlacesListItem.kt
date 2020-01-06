package com.client.traveller.ui.nearby.mainFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.client.traveller.R
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.ui.home.HomeActivity
import com.client.traveller.ui.util.ActivitiesAction
import com.client.traveller.ui.util.formatToApi
import com.client.traveller.ui.util.toLatLng
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_nearby_place.*

class NearbyPlacesListItem(
    private val nearbySearchResponseResult: Result,
    private val parentView: View?,
    private val photoUrl: String?,
    private val context: Context?
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            updateBackgroundImage()
            updateIcon()
            updateName()
            updateAddress()
            updateRatingStars()
            updateTypes()
            updateStatus()
        }
        viewHolder.navigate_to_button.setOnClickListener {
            Intent(context, HomeActivity::class.java).also {
                val bundle = Bundle()
                val location = nearbySearchResponseResult.geometry.location.toLatLng().formatToApi()
                bundle.putString(ActivitiesAction.HOME_ACTIVITY_DRAW_ROAD.name, location)
                it.putExtras(bundle)
                context?.startActivity(it)
            }
        }
    }

    override fun getLayout() = R.layout.item_nearby_place

    private fun GroupieViewHolder.updateStatus() {
        if (nearbySearchResponseResult.openingHours != null) {
            if (nearbySearchResponseResult.openingHours.openNow) {
                this.status.background = context?.getDrawable(R.drawable.status_green)
            } else {
                this.status.background = context?.getDrawable(R.drawable.status_red)
            }
        } else {
            this.status.visibility = View.GONE
        }
    }

    fun getPlaceId(): String {
        return nearbySearchResponseResult.placeId
    }

    private fun GroupieViewHolder.updateTypes() {
        if (context != null) {
            val types = nearbySearchResponseResult.types.map {
                val id = context.resources.getIdentifier(it, "string", context.packageName)
                if (id != 0) return@map context.getString(id)
                else return@map ""
            }
            val typesString = types.filter { it.isNotEmpty() }.joinToString(", ")
            this.types.text = "${context.getString(R.string.type)}: $typesString"
        } else {
            this.types.text = nearbySearchResponseResult.types.joinToString(", ")
        }
    }

    private fun GroupieViewHolder.updateRatingStars() {
        rating.rating = nearbySearchResponseResult.rating.toFloat()
    }

    private fun GroupieViewHolder.updateAddress() {
        address.text = nearbySearchResponseResult.vicinity
    }

    private fun GroupieViewHolder.updateName() {
        name.text = nearbySearchResponseResult.name
    }

    private fun GroupieViewHolder.updateIcon() {
        parentView?.let {
            Glide.with(this.root.context).load(nearbySearchResponseResult.icon).into(this.icon)
        }
    }

    private fun GroupieViewHolder.updateBackgroundImage() {
        this@NearbyPlacesListItem.photoUrl?.let {
            Glide.with(this.root.context)
                .load(it)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .fitCenter()
                .into(this.photo)
        }
    }
}






