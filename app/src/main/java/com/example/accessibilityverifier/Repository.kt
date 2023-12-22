package com.example.accessibilityverifier

import android.util.Log
import com.example.accessibilityverifier.Constants.BASE_URL
import com.example.accessibilityverifier.models.Results
import com.example.accessibilityverifier.models.SaveTestRequest
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL


object Repository {

    fun sendBodyObjectToAPIAndLogResult(bodyObject: ByteArray, dataoutputstr: String, apiUrl: String) {
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.setRequestProperty("accept", "application/json")
        //connection.outputStream.write(bodyObject)

        val wr = DataOutputStream(connection.outputStream)
        wr.writeBytes(dataoutputstr)
        wr.flush()
        wr.close()
        connection.connect()

        val responseCode = connection.responseCode
        val responseMessage = connection.responseMessage

        Log.d("XDEBUG","url: $apiUrl")
        Log.d("XDEBUG","Sending result:"+dataoutputstr)
        Log.d("XDEBUG","Request successful: ${responseCode == 200}")
        Log.d("XDEBUG","Status code: $responseCode")
        Log.d("XDEBUG","Message: $responseMessage")

        connection.disconnect()
    }

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

        Thread {
            sendAxeResultsToAPI(url, request)
        }.start()

        //retrofitCall(request)
    }

    fun sendAxeResultsToAPI(apiUrl: String, axeResults: SaveTestRequest) {
        // Serialize the AxeResults object to JSON
        val gson = GsonBuilder().disableHtmlEscaping().create()
        val json = gson.toJson(axeResults)
        Log.d("XDEBUG","json: $json")
        // Send the JSON data as the request body
        sendBodyObjectToAPIAndLogResult(json.toByteArray(), json, apiUrl)
    }

    fun retrofitCall(request: SaveTestRequest) {

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val yourAPIService = retrofit.create(ApiService::class.java)

        val response = yourAPIService.saveTest(request)
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