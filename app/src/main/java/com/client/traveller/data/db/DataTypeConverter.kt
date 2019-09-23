package com.client.traveller.data.db

import android.net.Uri
import androidx.room.TypeConverter

class DataTypeConverter {

    @TypeConverter
    fun uriToString(uri: Uri?): String? {
        if (uri == null)
            return null
        else
            return uri.toString()
    }

    @TypeConverter
    fun stringToUri(uri: String?): Uri? {
        if (uri == null)
            return null
        else
            return Uri.parse(uri)
    }
}