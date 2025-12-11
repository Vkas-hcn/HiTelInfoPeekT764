package com.lecture.field.tell.net.need

import android.content.Context
import com.lecture.field.tell.ext.PeekExample
import com.lecture.field.tell.line.ConTool
import mei.ye.DataPreferences
import org.json.JSONObject

/**
 * 配置管理类 - 负责解析和获取配置信息
 */
object ConfigManager {
    
    /**
     * 检查是否有配置
     */
    fun hasConfig(context: Context): Boolean {
        val data = getAdminData(context)
        return data.isNotEmpty()
    }
    
    /**
     * 判断是否为a用户（thing用户）
     */
    fun isUserA(context: Context): Boolean {
        val data = getAdminData(context)
        return ConTool.canUser(data)
    }
    
    /**
     * 获取admin数据
     */
    fun getAdminData(context: Context): String {
        return DataPreferences.getInstance(context)
            .getString(PeekExample.KEY_ADMIN_DATA, "")
    }
    
    /**
     * 解析配置中的jimi_yei字段
     * 返回值：[刷新间隔A(分钟), 刷新间隔B(秒), 请求上限(次)]
     */
    fun parseJimiYei(context: Context): Triple<Int, Int, Int> {
        return try {
            val data = getAdminData(context)
            if (data.isEmpty()) {
                return Triple(60, 60, 1000) // 默认值
            }
            
            val json = JSONObject(data)
            val jimiYei = json.optString("jimi_yei", "60-60-1000")
            val parts = jimiYei.split("-")
            
            if (parts.size >= 3) {
                Triple(
                    parts[0].toIntOrNull() ?: 60,
                    parts[1].toIntOrNull() ?: 60,
                    parts[2].toIntOrNull() ?: 1000
                )
            } else {
                Triple(60, 60, 1000)
            }
        } catch (e: Exception) {
            ConTool.showLog("parseJimiYei error: ${e.message}")
            Triple(60, 60, 1000)
        }
    }
    
    /**
     * 获取B用户的请求间隔（秒）
     */
    fun getUserBInterval(context: Context): Int {
        val (_, intervalB, _) = parseJimiYei(context)
        return intervalB
    }
    
    /**
     * 获取每天请求上限
     */
    fun getDailyLimit(context: Context): Int {
        val (_, _, limit) = parseJimiYei(context)
        return limit
    }
}
