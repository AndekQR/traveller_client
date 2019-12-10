package com.client.traveller.data.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.preference.PreferenceManager
import com.client.traveller.R
import java.text.DateFormat
import java.util.*

internal object Utils {
    const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates"
    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
//    fun requestingLocationUpdates(context: Context?): Boolean {
//        return PreferenceManager.getDefaultSharedPreferences(context)
//            .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
//    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun setRequestingLocationUpdates(
        context: Context?,
        requestingLocationUpdates: Boolean
    ) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
            .apply()
    }

    /**
     * Returns the `location` object as a human readable string.
     * @param location  The [Location].
     */
    fun getLocationText(location: Location?): String {
        return if (location == null) "Unknown location" else "(" + location.latitude + ", " + location.longitude + ")"
    }

    @SuppressLint("StringFormatInvalid")
    fun getLocationTitle(context: Context): String {
        return context.getString(
            R.string.location_updated,
            DateFormat.getDateTimeInstance().format(Date())
        )
    }
}