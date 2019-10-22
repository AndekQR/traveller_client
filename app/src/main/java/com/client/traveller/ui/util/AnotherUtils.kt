package com.client.traveller.ui.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng


fun Location.format(): String {
    return "${this.latitude},${this.longitude}"
}

fun LatLng.format(): String {
    return "${this.latitude},${this.longitude}"
}
