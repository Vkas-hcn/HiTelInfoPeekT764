package com.lecture.field.tell.net.demo

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiService {
    
    /**
     * Admin数据请求
     *  TODO admin url
     */
    @POST("apitest/cds/pk/")
    fun postAdminData(
        @Header("dt") timestamp: String,
        @Body body: RequestBody
    ): Call<ResponseBody>
    
    /**
     * Put数据请求
     *  TODO up url
     */
    @POST("olive/lame")
    @Headers("Content-Type: application/json")
    fun postPutData(
        @Body body: RequestBody
    ): Call<ResponseBody>
}
