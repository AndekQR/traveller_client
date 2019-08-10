package com.client.traveller.data.network

import android.util.Log
import com.client.traveller.ui.util.ApiException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

abstract class SafeApiRequest {

    suspend fun <T: Any> apiRequest(call: suspend () -> Response<T>): T{
        val response = call.invoke()
        if (response.isSuccessful){
            try {
                return response.body()!!
            }catch (e: Exception){
                throw ApiException("Exception in the success body of the response")
            }
        }
        else{
            val error = response.errorBody()?.string()
            val errorMessage = StringBuilder()
            error.let {
                try {
                    errorMessage.append(JSONObject(it).getString("message"))
                    errorMessage.append("\n")
                    errorMessage.append(JSONObject(it).getJSONArray("details").toString())
                    errorMessage.append("\n")
                }
                catch (e: JSONException){
                    Log.d("SafeRequest", e.toString())
                }
            }
            errorMessage.append("Error code: ${response.code()}")
            throw ApiException(errorMessage.toString())
        }
    }
}