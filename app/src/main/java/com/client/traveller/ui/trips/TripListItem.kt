package com.client.traveller.ui.trips

import android.location.Address
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_trip_list.*
import java.util.*


class TripListItem(val trip: Trip) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            updateTripName()
            updateTripCity()
            updateTripAuthor()
            updateTripPeople()
            updateTripDistance()
            updateTripWaypoints()
        }
    }

    override fun getLayout() = R.layout.item_trip_list

    private fun GroupieViewHolder.updateTripName(){
        trip_name.text = trip.name
    }

    private fun GroupieViewHolder.updateTripCity(){

    }

    private fun GroupieViewHolder.updateTripAuthor(){

    }

    private fun GroupieViewHolder.updateTripPeople(){

    }

    private fun GroupieViewHolder.updateTripDistance(){

    }

    private fun GroupieViewHolder.updateTripWaypoints(){

    }
}