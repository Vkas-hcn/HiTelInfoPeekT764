package com.lecture.field.tell.ffff

import android.app.Application
import android.content.Context
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.lecture.field.tell.ext.PeekExample
import com.lecture.field.tell.line.ConTool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mei.ye.DataPreferences
import org.json.JSONObject

object FirebaseShow {
    fun showAppVersion(context: Context): String {
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    }
    fun firebaseShowFun(context: Context) {
        val state = DataPreferences.getInstance(context).getBoolean(PeekExample.KEY_POST_FCM, false)
        if (state) {
            return
        }
        try {
            Firebase.messaging.subscribeToTopic(ConTool.firebaseKey)
                .addOnSuccessListener {
                    DataPreferences.getInstance(context).putBoolean(PeekExample.KEY_POST_FCM, true)
                }
                .addOnFailureListener {
                }
        } catch (e: Exception) {
        }
    }

    fun queq(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            while (true){
//                DaoneT.postPointFun(context,false, "session")
                delay(15 * 60 * 1000)
            }
        }
    }

    fun initFb(app: Application, jsonObject: JSONObject) {
        try {
            val fbStr = jsonObject.optString("dixier").split("-")[0]
            val token = jsonObject.optString("dixier").split("-")[1]
            if (fbStr.isBlank()) return
            if (token.isBlank()) return
            if (FacebookSdk.isInitialized()) return
            FacebookSdk.setApplicationId(fbStr)
            FacebookSdk.setClientToken(token)
            FacebookSdk.sdkInitialize(app)
            AppEventsLogger.Companion.activateApp(app)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}