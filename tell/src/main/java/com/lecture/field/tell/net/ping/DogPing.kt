package com.lecture.field.tell.net.ping

import android.content.Context
import com.lecture.field.tell.ext.PeekExample
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.net.demo.Kole
import mei.ye.DataPreferences

object DogPing {

    // 重试管理器缓存
    private val retryManagers = mutableMapOf<String, RetryManager>()

    /**
     * 上报安装事件（一次性事件）
     * - 必传事件，重试20次
     * - 成功后不再上报
     * - 保存首次生成的数据，重试时使用相同数据
     */
    fun upInstall(context: Context) {
        // 检查是否已上报成功
        if (InstallEventManager.isInstallReported(context)) {
            ConTool.showLog("[upInstall] The installation event has been reported and skipped")
            return
        }
        
        // 获取或生成安装数据（首次生成后保存）
        val body = InstallEventManager.getOrCreateInstallData(context)

        // 创建重试管理器（必传事件）
        val retryManager = RetryManager( "upInstall", isRequired = true)
        retryManagers["upInstall"] = retryManager
        
        // 开始重试流程
        retryManager.startRetry(body, object : Kole.CallbackMy {
            override fun onSuccess(response: String) {
                // 标记为已上报
                InstallEventManager.markInstallReported(context)
                retryManagers.remove("upInstall")
            }

            override fun onFailure(error: String) {
                retryManagers.remove("upInstall")
            }
        })
    }

    /**
     * 上报广告事件
     * - 必传事件，重试20次
     */
    fun upAd(json: String, context: Context) {
        val eventKey = "upAd_${System.currentTimeMillis()}"
        val body = DogData.upAdJson(json, context)

        // 创建重试管理器（必传事件）
        val retryManager = RetryManager("upAd", isRequired = true)
        retryManagers[eventKey] = retryManager
        
        // 开始重试流程
        retryManager.startRetry(body, object : Kole.CallbackMy {
            override fun onSuccess(response: String) {
                retryManagers.remove(eventKey)
            }

            override fun onFailure(error: String) {
                retryManagers.remove(eventKey)
            }
        })
    }

    /**
     * 上报埋点事件
     * @param canRetry true=必传事件（重试20次），false=非必传事件（重试2-5次）
     */
    fun upPoint(
        context: Context,
        canRetry: Boolean,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null
    ) {
        val adminData = DataPreferences.getInstance(context).getString(PeekExample.KEY_ADMIN_DATA,"")
        if (!canRetry && adminData.isNotBlank() && !(ConTool.canPost(adminData))) {
            return
        }
        val eventKey = "upPoint_${name}_${System.currentTimeMillis()}"
        val body = DogData.upPointJson(
            context = context,
            name = name,
            key1 = key1,
            keyValue1 = keyValue1
        )

        // 创建重试管理器
        val retryManager = RetryManager( "upPoint-$name", isRequired = canRetry)
        retryManagers[eventKey] = retryManager
        
        // 开始重试流程
        retryManager.startRetry(body, object : Kole.CallbackMy {
            override fun onSuccess(response: String) {
                retryManagers.remove(eventKey)
            }

            override fun onFailure(error: String) {
                retryManagers.remove(eventKey)
            }
        })
    }

    fun ConfigG(context: Context,typeUser: Boolean, codeInt: String?) {
        val isuserData: String? = if (codeInt == null) {
            null
        } else if (codeInt != "200") {
            codeInt
        } else if (typeUser) {
            "a"
        } else {
            "b"
        }
        upPoint(
            context,
            true,
            "config_G",
            "getstring",
            isuserData
        )
    }
}