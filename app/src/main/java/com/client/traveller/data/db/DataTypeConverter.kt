package com.client.traveller.data.db

import android.net.Uri
import androidx.room.TypeConverter
import com.client.traveller.data.db.entities.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object DataTypeConverter {

    @TypeConverter
    @JvmStatic
    fun uriToString(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    @JvmStatic
    fun stringToUri(uri: String?): Uri? {
        if (uri == null)
            return null
        else
            return Uri.parse(uri)
    }

    @TypeConverter
    @JvmStatic
    fun listToJson(value: ArrayList<String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun jsonToList(value: String?): ArrayList<String>? {
        val listType = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun stringToDate(value: String?): LocalDateTime? {
        if (value == null)
            return null
        else
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    @JvmStatic
    fun dateToString(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    @JvmStatic
    fun jsonToUser(value: String?): User? {
        return Gson().fromJson(value, User::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun userToJson(value: User?): String? {
        return Gson().toJson(value)
    }
}