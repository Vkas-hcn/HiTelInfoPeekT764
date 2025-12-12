package com.lecture.field.tell.net.need

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.net.demo.Kole
import kotlin.random.Random

/**
 * 数据更新管理器 - 负责长时间循环请求admin更新数据
 * 此任务独立运行，不执行goToCOreA，仅用于更新数据
 */
class DataUpdateManager(private val context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var isRunning = false
    
    /**
     * 启动长时间循环更新
     */
    fun startLongTermUpdate() {
        if (isRunning) {
            return
        }
        
        isRunning = true

        scheduleNextUpdate()
    }
    
    /**
     * 安排下次更新
     */
    private fun scheduleNextUpdate() {
        if (!isRunning) {
            return
        }
        
        // 获取更新间隔（分钟）
        val intervalMinutes = getUpdateInterval()
        // 添加随机偏移：前后随机5分钟
        val randomOffsetMinutes = Random.nextInt(-5, 6) // -5到5分钟
        val totalMinutes = intervalMinutes + randomOffsetMinutes
        
        // 确保间隔至少为1分钟
        val finalMinutes = maxOf(1, totalMinutes)
        val delayMillis = finalMinutes * 60 * 1000L
        

        updateRunnable = Runnable {
            executeUpdate()
        }
        
        handler.postDelayed(updateRunnable!!, delayMillis)
    }
    
    /**
     * 执行更新请求
     */
    private fun executeUpdate() {

        // 检查是否达到每日上限
        if (RequestCounter.hasReachedDailyLimit(context)) {
            scheduleNextUpdate()
            return
        }
        
        // 增加请求计数
        RequestCounter.incrementRequestCount(context)
        
        // 发起请求
        Kole.postAdminData(context, object : Kole.CallbackMy {
            override fun onSuccess(response: String) {
                // 数据已通过Kole.isCanSave自动保存
                // 不执行goToCOreA，仅更新数据
                
                // 安排下次更新
                scheduleNextUpdate()
            }
            
            override fun onFailure(error: String) {

                // 失败后也继续安排下次更新
                scheduleNextUpdate()
            }
        })
    }
    
    /**
     * 获取更新间隔（分钟）
     * 从jimi_yei的第一个值获取
     */
    private fun getUpdateInterval(): Int {
        return try {
            val (intervalA, _, _) = ConfigManager.parseJimiYei(context)
            intervalA
        } catch (e: Exception) {
            60 // 默认60分钟
        }
    }
    
    /**
     * 停止长时间更新
     */
    fun stop() {
        isRunning = false
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }
}
