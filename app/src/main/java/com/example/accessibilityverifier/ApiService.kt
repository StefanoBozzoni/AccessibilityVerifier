package com.example.accessibilityverifier

import com.example.accessibilityverifier.models.SaveTestRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("{project_id}/{session_id}/{section_id}/{route_id}")
    fun sendAccessibilityCheck(@Path("project_id") project_id:String,
                               @Path("session_id") session_id:String,
                               @Path("section_id") section_id:String,
                               @Path("route_id")   route_id:String,
                               @Body request: SaveTestRequest): Call<Any>
    //b62780c0-2e92-4272-9544-8a58475d8997/bb7ab08b-4f4e-4516-b6d7-af76d49e8f7f/8b843c6d-fcec-4d28-804c-51a5b20456c3/81af4161-9838-4a53-8325-0272124df2d3
}