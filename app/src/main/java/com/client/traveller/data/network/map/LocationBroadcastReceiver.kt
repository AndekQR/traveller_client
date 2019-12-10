package com.client.traveller.data.network.map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.client.traveller.data.services.MyLocationService


class LocationBroadcastReceiver : BroadcastReceiver() {

    var value: Double = 0.0

    override fun onReceive(context: Context?, intent: Intent?) {
        val location: Location = intent!!.getParcelableExtra(MyLocationService.EXTRA_LOCATION)
        Log.e(javaClass.simpleName, "${location.latitude}")

    }

}