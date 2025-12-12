package com.lecture.field.tell.ffff

import android.app.Application
import android.content.Context
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.lecture.field.tell.ext.PeekExample
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.net.ping.DogPing
import mei.ye.DataPreferences
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean

object FirebaseShow {

    private var sessionTimer: Timer? = null
    private val isRunning = AtomicBoolean(false)
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
        // 防止重复启动
        if (!isRunning.compareAndSet(false, true)) {
            return
        }

        runCatching {
            stopSessionTimer()

            sessionTimer = Timer("SessionTimer", true).apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        runCatching {
                            DogPing.upPoint(context.applicationContext, false, "session")
                        }.onFailure { e ->
                        }
                    }
                }, 0, 15 * 60 * 1000L) // 立即执行，然后每15分钟执行一次
            }

        }.onFailure { e ->
            isRunning.set(false)
        }
    }

    /**
     * 停止Session定时器
     */
    fun stopSessionTimer() {
        runCatching {
            sessionTimer?.cancel()
            sessionTimer = null
            isRunning.set(false)
        }.onFailure { e ->
            ConTool.showLog("停止定时器异常: ${e.message}")
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