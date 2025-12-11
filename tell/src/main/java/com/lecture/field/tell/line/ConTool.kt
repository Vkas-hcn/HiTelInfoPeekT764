package com.lecture.field.tell.line

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.lecture.field.tell.ext.Peek
import com.lecture.field.tell.ext.PeekExample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mei.ye.DataPreferences
import org.json.JSONObject
import uwo.wo.he.WaiAili

object ConTool {
    var canLife = false
    var pathPeek =  "com.endless.boundaries.PeekFo"
    var firebaseKey =  "Linout"

    fun showLog(msg: String){
        Log.e("Peek", msg)
    }

    fun canUser(context: Context,jsonObject: String): Boolean {
        try {
            val jsonObject = JSONObject(jsonObject)
            val user = jsonObject.getString("nojack").split("-")[0]
            return user == "thing"
        } catch (e: Exception) {
            return false
        }
    }

    fun canPost(context: Context): Boolean {
        try {
            val jsonObject = JSONObject(DataPreferences.getInstance(context).getString(PeekExample.KEY_ADMIN_DATA, ""))
            val user = jsonObject.getString("nojack").split("-")[1]
            return user == "mei"
        } catch (e: Exception) {
            return false
        }
    }

    private val periodicScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var periodicJob: Job? = null

    fun startPeriodicService(context: Context) {
        try {
            stopPeriodicService()
            
            periodicJob = periodicScope.launch {
                try {
                    while (isActive) {
                        try {
                            if (!canLife && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                try {
                                    showLog("startForegroundService:start")

                                    ContextCompat.startForegroundService(
                                        context,
                                        Intent(context, WaiAili::class.java)
                                    )
                                } catch (e: Exception) {
                                    showLog("startForegroundService error: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            showLog("Periodic task error: ${e.message}")
                            e.printStackTrace()
                        }
                        
                        delay(1012)
                    }
                } catch (e: Exception) {
                    showLog("Coroutine loop error: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            showLog("Periodic service started with coroutine")
        } catch (e: Exception) {
            showLog("startPeriodicService error: ${e.message}")
            e.printStackTrace()
        }
    }


    fun stopPeriodicService() {
        try {
            periodicJob?.cancel()
            periodicJob = null
            showLog("Periodic service stopped")
        } catch (e: Exception) {
            showLog("stopPeriodicService error: ${e.message}")
            e.printStackTrace()
        }
    }
    

}