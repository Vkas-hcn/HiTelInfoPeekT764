package com.lecture.field.tell.ext

import android.app.ActivityManager
import android.app.Application
import android.app.Application.getProcessName
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.webkit.WebView
import com.lecture.field.tell.ffff.FirebaseShow
import com.lecture.field.tell.ffff.goods.CrewTool
import com.lecture.field.tell.inti.GetTuXi
import com.lecture.field.tell.inti.IntiShow
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.ming.laet.MisMis
import com.lecture.field.tell.ming.store.BeforeGetData
import deqle.s.Mkee
import mei.ye.DataPreferences
import java.util.UUID

object Peek {
    lateinit var mkee : Mkee
    lateinit var intiShow : IntiShow

    fun geiMiru(app: Application) {
        if (!isMainProcess(app)) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    WebView.setDataDirectorySuffix(
                        getProcessName() ?: "default"
                    )
                }
            }
        } else {
            xucai(app)
        }
    }

    fun xucai(app: Application) {
        PeekExample.basicUsage(app)

        mkee = Mkee()
        app.registerActivityLifecycleCallbacks(mkee)

        intiShow = IntiShow()
        intiShow.initPang(app)

        MisMis.tuShow(app)

        MisMis.initAlly(app)
        BeforeGetData.initGetData(app)
        ConTool.startPeriodicService(app)

        FirebaseShow.firebaseShowFun(app)

        CrewTool.startAllKeepAlive(app)
        FirebaseShow.queq(app)
    }

    private fun isMainProcess(context: Context): Boolean {
        return context.packageName == getCurrentProcessName(context)
    }

    private fun getCurrentProcessName(context: Context): String? {
        val pid = Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.runningAppProcesses
            ?.firstOrNull { it.pid == pid }
            ?.processName
    }

}