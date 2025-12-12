package com.lecture.field.tell.net.ping

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.net.demo.Kole
import kotlin.random.Random

/**
 * 重试管理器
 * 负责处理埋点上报的重试逻辑
 */
class RetryManager(
    private val eventName: String,
    private val isRequired: Boolean // 是否是必传事件
) {
    private val handler = Handler(Looper.getMainLooper())
    private var currentRetryCount = 0
    private val maxRetryCount: Int
    private var retryRunnable: Runnable? = null
    private var isRetrying = false
    
    init {
        // 必传事件重试20次，非必传事件随机2-5次
        maxRetryCount = if (isRequired) 20 else Random.nextInt(2, 6)
    }
    
    /**
     * 开始重试流程
     * @param requestBody 请求体
     * @param finalCallback 最终回调
     */
    fun startRetry(requestBody: String, finalCallback: Kole.CallbackMy) {
        if (isRetrying) {
            return
        }
        
        isRetrying = true
        currentRetryCount = 0
        executeRetry(requestBody, finalCallback)
    }
    
    /**
     * 执行重试
     */
    private fun executeRetry(requestBody: String, finalCallback: Kole.CallbackMy) {
        if (currentRetryCount >= maxRetryCount) {
            ConTool.showLog("[$eventName] Maximum number of retries reached($maxRetryCount)Stop retrying")
            isRetrying = false
            finalCallback.onFailure("Max retry attempts reached")
            return
        }
        
        currentRetryCount++
        ConTool.showLog("[$eventName]-json=${requestBody}")

        // 发起请求
        Kole.postPutData(requestBody, object : Kole.CallbackMy {
            override fun onSuccess(response: String) {
                ConTool.showLog("[$eventName] No${currentRetryCount}requests successfully=$response")
                isRetrying = false
                stopRetry()
                finalCallback.onSuccess(response)
            }
            
            override fun onFailure(error: String) {
                ConTool.showLog("[$eventName] NO${currentRetryCount}requests failed: $error")

                if (currentRetryCount >= maxRetryCount) {
                    ConTool.showLog("[$eventName] The maximum number of retries has been reached and the report failed.")
                    isRetrying = false
                    finalCallback.onFailure("Failed after $maxRetryCount attempts: $error")
                } else {
                    // 计算下次重试时间（10-40秒随机）
                    val nextRetryDelay = Random.nextInt(10, 41) * 1000L
                    ConTool.showLog("[$eventName] will be in${nextRetryDelay / 1000}Seconds later${currentRetryCount + 1}retries")
                    
                    // 延迟后重试
                    retryRunnable = Runnable {
                        executeRetry(requestBody, finalCallback)
                    }
                    handler.postDelayed(retryRunnable!!, nextRetryDelay)
                }
            }
        })
    }
    
    /**
     * 停止重试
     */
    fun stopRetry() {
        retryRunnable?.let {
            handler.removeCallbacks(it)
            ConTool.showLog("[$eventName] Stop retrying")
        }
        retryRunnable = null
        isRetrying = false
    }
    
    /**
     * 检查是否正在重试
     */
    fun isRetrying(): Boolean = isRetrying
}
