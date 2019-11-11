package com.client.traveller.ui.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng
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

