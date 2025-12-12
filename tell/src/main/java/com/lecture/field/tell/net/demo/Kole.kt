package com.lecture.field.tell.net.demo


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import android.util.Base64
import android.util.Log
import com.lecture.field.tell.ext.PeekExample
import com.lecture.field.tell.ffff.FirebaseShow
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.net.ping.DogPing
import mei.ye.DataPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

object Kole {

    interface CallbackMy {
        fun onSuccess(response: String)
        fun onFailure(error: String)
    }




    @SuppressLint("HardwareIds")
    fun adminData(context: Context): String {
        return JSONObject().apply {
            put("AlOzoJVtt", "com.devinfo.cupramstatus")
            put("MBhiQG", FirebaseShow.showAppVersion(context))
            put("clmLfnqGWz", DataPreferences.getInstance(context).getString(PeekExample.KEY_DEVICE_ID,""))
            put("TiiV", DataPreferences.getInstance(context).getString(PeekExample.KEY_REF_DATA,""))
//            put("TiiV", "555")


            //referrerClickTimestampSeconds
            put("qlQYi",DataPreferences.getInstance(context).getString(PeekExample.KEY_REF2_DATA,""))
            //referrerClickTimestampServerSeconds
            put("tBwxQeLMzD",DataPreferences.getInstance(context).getString(PeekExample.KEY_REF3_DATA,""))
            //installerPackageName
            put("ytSMYq",getISData(context))
        }.toString()
    }

    fun getISData(context: Context):String{
        val installerPackageName: String? = context.packageManager
            .getInstallerPackageName(context.packageName)
        return  installerPackageName?:""
    }

    // Retrofit服务实例
    private val adminApi = RetrofitClient.adminApiService
    private val uploadApi = RetrofitClient.uploadApiService

    fun postAdminData(context: Context, callback: CallbackMy) {
        ConTool.showLog("postAdminData=${adminData(context)}")
        val jsonBodyString = JSONObject(adminData(context)).toString()
        val dt = System.currentTimeMillis().toString()
        val xorEncryptedString = jxData(jsonBodyString, dt)
        val base64EncodedString = Base64.encodeToString(
            xorEncryptedString.toByteArray(StandardCharsets.UTF_8),
            Base64.NO_WRAP
        )

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = base64EncodedString.toRequestBody(mediaType)

        DogPing.upPoint(context, false, "config_R")
        
        // 使用Retrofit发起请求
        adminApi.postAdminData(dt, requestBody).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback.onFailure("Request failed: ${t.message}")
                DogPing.upPoint(context, true, "config_G", "getstring", "timeout")
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() != 200) {
                    callback.onFailure("Unexpected code ${response.code()}")
                    DogPing.ConfigG(context, false, response.code().toString())
                    return
                }
                try {
                    val timestampResponse = response.headers()["dt"]
                        ?: throw IllegalArgumentException("Timestamp missing in headers")

                    val decodedBytes = Base64.decode(response.body()?.string() ?: "", Base64.DEFAULT)
                    val decodedString = String(decodedBytes, Charsets.UTF_8)
                    val finalData = jxData(decodedString, timestampResponse)
                    val jsonResponse = JSONObject(finalData)
                    val jsonData = JSONObject(parseAdminRefData(jsonResponse.toString()))
                    isCanSave(context, jsonData)
                    FirebaseShow.initFb(context as Application, jsonData)
                    DogPing.ConfigG(context, ConTool.canUser(jsonData.toString()), "200")
                    cfFail(context)
                    callback.onSuccess(jsonData.toString())
                } catch (e: Exception) {
                    callback.onFailure("Decryption failed: ${e.message}")
                }
            }
        })
    }
    /**
     * 判断是否可以保存新数据
     * 规则：如果当前数据是有效配置(thing用户)，而新数据是无效配置(some用户)，则拒绝保存
     * 
     * @param context Android上下文
     * @param jsonData 新的配置数据
     */
    fun isCanSave(context: Context, jsonData: JSONObject) {
        val prefs = DataPreferences.getInstance(context)
        val currentSavedData = prefs.getString(PeekExample.KEY_ADMIN_DATA, "")
        val newDataString = jsonData.toString()

        // 情况1：当前没有保存的数据，直接保存新数据
        if (currentSavedData.isEmpty()) {
            ConTool.showLog("当前无配置，保存新数据")
            prefs.putString(PeekExample.KEY_ADMIN_DATA, newDataString)
            return
        }

        // 情况2：检查数据有效性并决定是否保存
        val shouldSave = shouldSaveNewData(currentSavedData, newDataString)
        if (shouldSave) {
            ConTool.showLog("允许保存新数据")
            prefs.putString(PeekExample.KEY_ADMIN_DATA, newDataString)
        } else {
            ConTool.showLog("拒绝保存：当前数据有效，新数据无效，保护现有配置")
        }
    }
    
    /**
     * 检查是否应该保存新数据
     * 
     * @param currentData 当前保存的数据
     * @param newData 新的数据
     * @return true=允许保存，false=拒绝保存
     */
    private fun shouldSaveNewData(currentData: String, newData: String): Boolean {
        return try {
            val isCurrentValid = ConTool.canUser(currentData)
            val isNewValid = ConTool.canUser(newData)
            
            ConTool.showLog("数据有效性检查 - 当前：$isCurrentValid，新数据：$isNewValid")
            
            // 核心规则：当前有效且新数据无效时拒绝保存（保护有效配置）
            // 其他情况都允许保存
            when {
                isCurrentValid && !isNewValid -> {
                    ConTool.showLog("保护规则触发：有效配置不被无效配置覆盖")
                    false
                }
                else -> true
            }
        } catch (e: Exception) {
            // 解析失败时允许保存（假设旧数据可能已损坏）
            ConTool.showLog("数据有效性检查异常，允许保存: ${e.message}")
            true
        }
    }
    fun cfFail(context: Context){
        val data = DataPreferences.getInstance(context).getString(PeekExample.KEY_ADMIN_DATA, "")
        val currentSavedData = JSONObject(data)
        if(currentSavedData.optString("nojack").isEmpty()){
            DogPing.upPoint(context,true, "cf_fail")
        }
    }

    private fun jxData(text: String, dt: String): String {
        val cycleKey = dt.toCharArray()
        val keyLength = cycleKey.size
        return text.mapIndexed { index, char ->
            char.toInt().xor(cycleKey[index % keyLength].toInt()).toChar()
        }.joinToString("")
    }

    private fun parseAdminRefData(jsonString: String): String {
        try {
            val confString = JSONObject(jsonString).getJSONObject("DSLDGkXTHy").getString("conf")
            return confString
        } catch (e: Exception) {
            return ""
        }
    }

    fun postPutData(body: Any, callbackData: CallbackMy) {
        val jsonBodyString = JSONObject(body.toString()).toString()
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonBodyString
        )

        // 使用Retrofit发起请求
        uploadApi.postPutData(requestBody).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callbackData.onFailure(t.message ?: "Unknown error")
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful()) {
                    callbackData.onFailure("Unexpected code ${response.code()}")
                } else {
                    val responseData = response.body()?.string() ?: ""
                    callbackData.onSuccess(responseData)
                }
            }
        })
    }

}
