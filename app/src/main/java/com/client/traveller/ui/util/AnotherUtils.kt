package com.client.traveller.ui.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine
import com.google.firebase.firestore.DocumentSnapshot as DocumentSnapshot1


fun Location.format(): String {
    return "${this.latitude},${this.longitude}"
}

fun LatLng.format(): String {
    return "${this.latitude},${this.longitude}"
}

