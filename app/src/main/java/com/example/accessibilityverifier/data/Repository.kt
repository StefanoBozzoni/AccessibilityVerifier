package com.example.accessibilityverifier.data

import android.util.Log
import com.example.accessibilityverifier.Constants
import com.example.accessibilityverifier.models.Results
import com.example.accessibilityverifier.models.SaveTestRequest
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL


object Repository {

    fun sendAxeResultTest(stringToSend: String) {
        val project_id="b62780c0-2e92-4272-9544-8a58475d8997"
        val session_id="bb7ab08b-4f4e-4516-b6d7-af76d49e8f7f"
        val section_id="8b843c6d-fcec-4d28-804c-51a5b20456c3"
        val route_id="81af4161-9838-4a53-8325-0272124df2d3"
        val url = "${Constants.BASE_URL}$project_id/$session_id/$section_id/$route_id"
        val request = SaveTestRequest(
            Results(
                "", stringToSend, listOf()
            ),
            "",
            true
        )

        retrofitCall(request)
    }

    fun retrofitCall(request: SaveTestRequest) {

        val apiService = RestClientFactory.apiServiceClient(RetrofitFactory.getRetrofitClient())

        val response = apiService.saveTest(request)
        response.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                Log.d("XDEBUG","Request successful: ${response.code() == 200}")
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.d("XDEBUG","error: ${t.message}")
            }
        })

    }

}