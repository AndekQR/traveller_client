package com.client.traveller.data.network.map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.client.traveller.data.services.MyLocationService
import com.client.traveller.ui.util.KodeinBroadcastReceiver

class LocationBroadcastReceiver : KodeinBroadcastReceiver() {

    override fun onBroadcastReceived(context: Context, intent: Intent) {
        val location: Location = intent!!.getParcelableExtra(MyLocationService.EXTRA_LOCATION)
        Log.e(javaClass.simpleName, "BroadcastReceiver: ${location.latitude}")
    }

}