package com.client.traveller.data.provider

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.client.traveller.ui.util.Constants

class PreferenceProvider(context: Context) {

    companion object {
        private const val NEARBY_PLACES_DISTANCE_SEARCH = "NEARBY_PLACES_DISTANCE_SEARCH"
        private const val SEND_LOCATION = "SEND_LOCATION"
        private const val TRAVEL_MODE = "TRAVEL_MODE"
        private const val CAMERA_TRACKING = "CAMERA_TRACKING"
        private const val SENDING_DATA_INTERVAL = "SENDING_DATA_INTERVAL"
    }

    private val appContext = context.applicationContext
    private val preferences: SharedPreferences

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    fun getSendLocation(): Boolean {
        return preferences.getBoolean(SEND_LOCATION, true)
    }

    fun getSendingDataInterval(): Int? {
        val interval = preferences.getString(SENDING_DATA_INTERVAL, "2")
        interval?.let {
            return Integer.parseInt(interval)
        }
        return null
    }

    fun getCameraTracking(): Boolean {
        return preferences.getBoolean(CAMERA_TRACKING, true)
    }

    fun getTravelMode(): String? {
        return preferences.getString(TRAVEL_MODE, "driving")
    }

    fun getNearbyPlacesSearchDistance(): Int? {
        val distance = preferences.getString(NEARBY_PLACES_DISTANCE_SEARCH, "3000")
        if (distance != null) {
            val distanceInt = distance.toInt()
            return if (distanceInt > 10000) {
                val editor = preferences.edit()
                editor.putString(NEARBY_PLACES_DISTANCE_SEARCH, "10000")
                editor.apply()
                10000
            } else {
                distanceInt
            }
        }
        return null
    }

    fun putCurrentTravelUid(uid: String) {
        preferences.edit().putString(Constants.CURRENT_TRIP_UID_PREFERENCES, uid).apply()
    }

    fun getCurrentTravelUid(): String? {
        return preferences.getString(Constants.CURRENT_TRIP_UID_PREFERENCES, "")
    }

}