package com.lecture.field.tell.ming.store

import android.content.Context
import com.lecture.field.tell.ext.PeekExample
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.line.SongFun
import com.lecture.field.tell.net.demo.LiuNextGo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mei.ye.DataPreferences

object BeforeGetData {
    fun initGetData(context: Context) {
        val ref = DataPreferences.getInstance(context).getString(PeekExample.KEY_REF_DATA, "")
        when {
            ref.isNotEmpty() -> handleExistingRefData(context)
            else -> startRefMonitoring(context)
        }
    }
    private fun handleExistingRefData(context: Context) {
        startOneTimeAdminData(context)
        //上传安装事件
        SongFun.postInsFun()
    }
    private fun startRefMonitoring(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val ref = DataPreferences.getInstance(context).getString(PeekExample.KEY_REF_DATA, "")
                if (ref.isNotEmpty()) {
                    handleExistingRefData(context)
                    break
                }
                fetchReferrerData(context)
                delay(10_010)
            }
        }
    }

    private suspend fun fetchReferrerData(context: Context) {
        try {
            InstallReferrerManager.fetchInstallReferrer(context)?.let { referrer ->
                ConTool.showLog("Install referrer: $referrer")
                DataPreferences.getInstance(context).putString(PeekExample.KEY_REF_DATA, referrer)
                withContext(Dispatchers.Main) {
                    //上传安装事件
                    SongFun.postInsFun()
                }
                startOneTimeAdminData(context)
            }
        }catch (e:Exception) {
            ConTool.showLog("Referrer error: ${e.message}")
        }
    }

    private fun startOneTimeAdminData(context: Context) {
        // 启动请求流程
        LiuNextGo.getUserData(context)
    }
}