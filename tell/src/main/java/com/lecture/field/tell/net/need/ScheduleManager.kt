package com.lecture.field.tell.net.need

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.lecture.field.tell.line.ConTool
import kotlin.random.Random

/**
 * 定时任务管理器 - 负责延迟请求和定时循环请求
 */
class ScheduleManager(private val context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    private var periodicRunnable: Runnable? = null
    private var delayRunnable: Runnable? = null
    
    /**
     * 启动延迟请求（情况1：A配置用户）
     * 延迟1秒到10分钟之间
     */
    fun startDelayedRequest(onRequest: () -> Unit) {
        cancelAll()
        
        val minDelay = 1 * 1000L // 1秒
        val maxDelay = 10 * 60 * 1000L // 10分钟
        val randomDelay = Random.nextLong(minDelay, maxDelay)
        

        delayRunnable = Runnable {
            onRequest()
        }
        
        handler.postDelayed(delayRunnable!!, randomDelay)
    }
    
    /**
     * 启动定时循环请求（情况2：B配置用户）
     * @param intervalSeconds 请求间隔（秒）
     * @param executeImmediately 首次是否立即执行（默认true）
     * @param onRequest 请求回调
     */
    fun startPeriodicRequest(
        intervalSeconds: Int, 
        executeImmediately: Boolean = true,
        onRequest: () -> Unit
    ) {
        cancelAll()
        
        // 添加0-10秒的随机延迟
        val randomOffset = Random.nextInt(0, 11) * 1000L
        val totalInterval = (intervalSeconds * 1000L) + randomOffset
        

        periodicRunnable = object : Runnable {
            override fun run() {
                onRequest()
                
                // 下次请求时重新计算随机延迟
                val nextRandomOffset = Random.nextInt(0, 11) * 1000L
                val nextInterval = (intervalSeconds * 1000L) + nextRandomOffset

                handler.postDelayed(this, nextInterval)
            }
        }
        
        if (executeImmediately) {
            // 首次立即执行
            handler.post(periodicRunnable!!)
        } else {
            // 首次等待间隔时间后执行
            handler.postDelayed(periodicRunnable!!, totalInterval)
        }
    }
    
    /**
     * 取消所有定时任务
     */
    fun cancelAll() {
        periodicRunnable?.let {
            handler.removeCallbacks(it)
        }
        delayRunnable?.let {
            handler.removeCallbacks(it)
        }
        periodicRunnable = null
        delayRunnable = null
    }
    
    /**
     * 取消定时循环请求
     */
    fun cancelPeriodicRequest() {
        periodicRunnable?.let {
            handler.removeCallbacks(it)
        }
        periodicRunnable = null
    }
}
