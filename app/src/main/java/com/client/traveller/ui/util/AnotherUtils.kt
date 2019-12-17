package com.client.traveller.ui.util

import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId


fun Location.format(): String {
    return "${this.latitude},${this.longitude}"
}

fun LatLng.format(): String {
    return "${this.latitude},${this.longitude}"
}

fun LocalDateTime.toLong(): Long {
    val zdt = this.atZone(ZoneId.of("Europe/Warsaw"))
    return zdt.toInstant().toEpochMilli()
}

fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.of("Europe/Warsaw"))
}

fun randomUid(): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz"
    return (1..35)
        .map { allowedChars.random() }
        .joinToString("")
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

@ExperimentalCoroutinesApi
fun CollectionReference.toFlow() = callbackFlow<QuerySnapshot> {
    val listener = this@toFlow.addSnapshotListener { snapshot, e ->
        if (e != null) {
            cancel("ERROR", e)
            return@addSnapshotListener
        }
        if (snapshot != null) {
            offer(snapshot)
        }
    }
    awaitClose {
        //        cancel()
        listener.remove()
    }
}

@ExperimentalCoroutinesApi
fun Query.toFlow() = callbackFlow<QuerySnapshot> {
    val registration = this@toFlow.addSnapshotListener { snapshot, e ->
        if (e != null) {
            cancel("ERROR", e)
            return@addSnapshotListener
        }
        if (snapshot != null) {
            offer(snapshot)
        }
    }
    awaitClose {
        //        cancel()
        registration.remove()
    }
}

fun <T, K, V> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    combineFun: (T?, K?) -> V
): LiveData<V> {
    val result = MediatorLiveData<V>()
    result.addSource(this) {
        result.value = combineFun.invoke(this.value, liveData.value)
    }
    result.addSource(liveData) {
        result.value = combineFun.invoke(this.value, liveData.value)
    }
    return result
}

fun LatLng.formatToApi(): String {
    return "${this.latitude},${this.longitude}"
}

fun com.client.traveller.data.network.api.places.response.nearbySearchResponse.Location.toLatLng(): LatLng {
    return LatLng(this.lat, this.lng)
}

fun com.client.traveller.data.network.api.geocoding.response.geocodingResponse.Location.toLatLng(): LatLng {
    return LatLng(this.lat, this.lng)
}

fun com.client.traveller.data.network.api.places.response.placeDetailResponse.Location.toLatLng(): LatLng  {
    return LatLng(this.lat, this.lng)
}

fun Location.toLatLng() : LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun String.toLatLng(): LatLng? {
    val splitResult = this.split(",")
    if (splitResult.size == 2) {
        return LatLng(splitResult[0].toDouble(), splitResult[1].toDouble())
    }
    return null
}


/**
 * sprawdza czy jakikolwiek element z pierwszej listy jest taki sam jak jakikolwiek element z drugiej listy
 */
fun <T> List<T>.contains(list: List<T>): Boolean {
    this.forEach { first ->
        list.forEach { second ->
            if (first == second)
                return true
        }
    }
    return false
}


