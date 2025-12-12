package com.lecture.field.tell.net.ping

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.lecture.field.tell.ext.PeekExample
import com.lecture.field.tell.ffff.FirebaseShow
import com.lecture.field.tell.line.ConTool
import mei.ye.DataPreferences
import org.json.JSONObject
import java.util.UUID
import kotlin.apply

object DogData {
    private fun topJsonData(context: Context): JSONObject {
        val digress = JSONObject().apply {
            //bundle_id
            put("nigh", context.packageName)

            //app_version
            put("raisin", FirebaseShow.showAppVersion(context))

            //distinct_id
            put("advice", DataPreferences.getInstance(context).getString(PeekExample.KEY_DEVICE_ID,""))

        }

        val mit = JSONObject().apply {
            //os
            put("cooky", "tagging")
            //log_id
            put("italy", UUID.randomUUID().toString())
            //client_ts
            put("selena", System.currentTimeMillis())
            //manufacturer
            put("bestowal", Build.MANUFACTURER)

            //os_version
            put("finny", Build.VERSION.RELEASE)
            //system_language//假值
            put("july", "csq_juy")
            //android_id
            put("dod", DataPreferences.getInstance(context).getString(PeekExample.KEY_DEVICE_ID,""))
        }

        val faro = JSONObject().apply {

            //device_model-最新需要传真实值
            put("stumpy", Build.BRAND)

            //operator 传假值字符串
            put("headset", "vre")

            //gaid
            put("passband", "")
        }
        return JSONObject().apply {
            put("digress", digress)
            put("mit", mit)
            put("faro", faro)
            if (ConTool.canNewKey(context)) {
                put("usercode~whimsy", ConTool.getNewKey(context))
            }
        }
    }

    fun upInstallJson(context: Context): String {
        val gainful = JSONObject().apply {
            //build
            put("acquaint", "build/${Build.ID}")

            //referrer_url
            put("handful", DataPreferences.getInstance(context).getString(PeekExample.KEY_REF_DATA,""))

            //user_agent
            put("salesian", "")

            //lat
            put("brewery", "sledge")

            //referrer_click_timestamp_seconds
            put("regina", 0)

            //install_begin_timestamp_seconds
            put("infidel", 0)

            //referrer_click_timestamp_server_seconds
            put("introit", 0)

            //install_begin_timestamp_server_seconds
            put("otis", 0)

            //install_first_seconds
            put("wore", getFirstInstallTime(context))

            //last_update_seconds
            put("embitter", 0)
        }
        return topJsonData(context).apply {
            put("sandal", gainful)
        }.toString()
    }

    fun upAdJson(adJson: String, context: Context): String {
        return topJsonData(context).apply {
            put("prune", JSONObject(adJson))
        }.toString()
    }


    fun upPointJson(
        context: Context,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null,
    ): String {
        return topJsonData(context).apply {
            put("erastus", name)
            if (key1 != null) {
                put("concise", JSONObject().apply {
                    put(key1, keyValue1)
                })
            }
        }.toString()
    }

    private fun getFirstInstallTime(context: Context): Long {
        try {
            val packageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.firstInstallTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }


}