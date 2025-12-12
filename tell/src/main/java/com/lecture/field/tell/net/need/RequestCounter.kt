package com.lecture.field.tell.net.need

import android.content.Context
import com.lecture.field.tell.line.ConTool
import mei.ye.DataPreferences
import java.text.SimpleDateFormat
import java.util.*

/**
 * 请求计数器 - 负责管理每天的请求次数
 */
object RequestCounter {
    
    private const val KEY_REQUEST_COUNT = "admin_request_count"
    private const val KEY_REQUEST_DATE = "admin_request_date"
    
    /**
     * 检查今天是否已达到请求上限
     */
    fun hasReachedDailyLimit(context: Context): Boolean {
        val limit = ConfigManager.getDailyLimit(context)
        val count = getTodayRequestCount(context)
        val reached = count >= limit
        
        if (reached) {
            ConTool.showLog("Today's request limit has been reached: $count/$limit")
        }
        
        return reached
    }
    
    /**
     * 增加请求计数
     */
    fun incrementRequestCount(context: Context) {
        val today = getCurrentDate()
        val prefs = DataPreferences.getInstance(context)
        val savedDate = prefs.getString(KEY_REQUEST_DATE, "")
        
        if (savedDate != today) {
            // 新的一天，重置计数
            prefs.putString(KEY_REQUEST_DATE, today)
            prefs.putInt(KEY_REQUEST_COUNT, 1)
        } else {
            // 同一天，增加计数
            val currentCount = prefs.getInt(KEY_REQUEST_COUNT, 0)
            val newCount = currentCount + 1
            prefs.putInt(KEY_REQUEST_COUNT, newCount)
        }
    }
    
    /**
     * 获取今天的请求次数
     */
    fun getTodayRequestCount(context: Context): Int {
        val today = getCurrentDate()
        val prefs = DataPreferences.getInstance(context)
        val savedDate = prefs.getString(KEY_REQUEST_DATE, "")
        
        return if (savedDate == today) {
            prefs.getInt(KEY_REQUEST_COUNT, 0)
        } else {
            0
        }
    }
    
    /**
     * 获取当前日期字符串（格式：yyyy-MM-dd）
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
