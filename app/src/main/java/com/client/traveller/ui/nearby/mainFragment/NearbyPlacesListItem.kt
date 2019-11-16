package com.client.traveller.ui.nearby.mainFragment

import android.view.View
import com.bumptech.glide.Glide
import com.client.traveller.R
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_nearby_place.*

class NearbyPlacesListItem(
    private val nearbySearchResponseResult: Result,
    private val parentView: View?,
    private val photoUrl: String?
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            updateBackgroundImage()
            updateIcon()
            updateName()
            updateAddress()
            updateRatingStars()
            updateTypes()
        }
    }

    override fun getLayout() = R.layout.item_nearby_place

    private fun GroupieViewHolder.updateTypes() {
        val types = nearbySearchResponseResult.types.joinToString(", ")
        if (parentView != null) {
            this.types.text = types
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
                .into(this.photo)
        }
    }
}






