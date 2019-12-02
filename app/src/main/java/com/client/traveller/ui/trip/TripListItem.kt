package com.client.traveller.ui.trip

import android.content.Context
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.ui.util.Coroutines.main
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_trip_list.*


class TripListItem(
    val trip: Trip,
    val currentTrip: Trip?,
    val context: Context?,
    val viewModel: TripViewModel,
    val currentUser: User
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            updateStatus()
            updateTripName()
            updateTripCity()
            updateTripAuthor()
            updateTripPeople()
            updateTripDistance()
            updateTripWaypoints()
        }
    }

    override fun getLayout() = R.layout.item_trip_list

    private fun GroupieViewHolder.updateStatus() {

        if (currentTrip != null && currentTrip.name == trip.name)
            status.background = context?.getDrawable(R.drawable.status_green)
        else {
            trip.persons?.forEach {
                if (it == currentUser.email) {
                    status.background = context?.getDrawable(R.drawable.status_yellow)
                    return@forEach
                }
            }
        }
    }

    private fun GroupieViewHolder.updateTripName() {
        trip_name.text = trip.name
    }

    private fun GroupieViewHolder.updateTripCity() {
        if (context != null) {
            trip_city.text = "${context.getString(R.string.trip_city)} ${trip.startAddress}"
        } else {
            trip_city.text = "${trip.startAddress}"
        }
    }

    private fun GroupieViewHolder.updateTripAuthor() {
        if (context != null) {
            trip_author.text =
                "${context.getString(R.string.trip_author)} ${trip.author?.displayName}"
        } else {
            trip_author.text = "${trip.author?.displayName}"
        }
    }

    private fun GroupieViewHolder.updateTripPeople() {
        if (context != null) {
            trip_people.text =
                "${context.getString(R.string.trip_people)} ${trip.persons?.size ?: "0"}"
        } else {
            trip_people.text = "${trip.persons?.size ?: "0"}"
        }
    }

    private fun GroupieViewHolder.updateTripDistance() = main {
        val distance =
            viewModel.tripDistance(trip.startAddress!!, trip.endAddress!!, trip.waypoints)
        if (context != null) {
            trip_distance.text =
                "${context.getString(R.string.trip_distance)} ${distance?.text ?: " - "}"
        } else {
            trip_distance.text =
                "${viewModel.tripDistance(trip.startAddress!!, trip.endAddress!!, trip.waypoints)}"
        }
    }

    private fun GroupieViewHolder.updateTripWaypoints() {
        if (context != null) {
            trip_waypoints.text =
                "${context.getString(R.string.trip_waypoints)} ${trip.waypoints?.size}"
        } else {
            trip_waypoints.text = "${trip.waypoints?.size}"
        }
    }
}