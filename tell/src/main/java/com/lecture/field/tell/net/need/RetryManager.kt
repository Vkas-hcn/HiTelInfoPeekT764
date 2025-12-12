package com.lecture.field.tell.net.need

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.lecture.field.tell.line.ConTool

/**
 * 重试管理器 - 负责处理请求重试逻辑
 */
class RetryManager(private val context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    private var retryCount = 0
    private val maxRetries = 5
    private val minRetryInterval = 30 * 1000L // 30秒
    private val maxRetryDuration = 5 * 60 * 1000L // 5分钟
    private var retryStartTime = 0L
    private var retryRunnable: Runnable? = null
    
    /**
     * 开始重试流程
     * @param onRetry 重试时的回调
     * @param onComplete 完成（成功或失败）时的回调
     */
    fun startRetry(onRetry: () -> Unit, onComplete: (success: Boolean) -> Unit) {
        retryCount = 0
        retryStartTime = System.currentTimeMillis()
        
        executeRetry(onRetry, onComplete)
    }
    
    /**
     * 执行重试
     */
    private fun executeRetry(onRetry: () -> Unit, onComplete: (success: Boolean) -> Unit) {
        // 检查是否超过最大重试次数
        if (retryCount >= maxRetries) {
            onComplete(false)
            return
        }
        
        // 检查是否超过最大重试时间
        val elapsed = System.currentTimeMillis() - retryStartTime
        if (elapsed >= maxRetryDuration) {
            onComplete(false)
            return
        }
        
        retryCount++

        // 执行重试
        onRetry()
        
        // 计算下次重试的延迟时间（至少30秒）
        val remainingTime = maxRetryDuration - elapsed
        val nextDelay = if (remainingTime < minRetryInterval) {
            remainingTime
        } else {
            // 在剩余时间内平均分配重试间隔，但不少于30秒
            val avgInterval = remainingTime / (maxRetries - retryCount + 1)
            maxOf(minRetryInterval, avgInterval)
        }
        

        // 如果还有重试机会，安排下次重试
        if (retryCount < maxRetries && elapsed + nextDelay < maxRetryDuration) {
            scheduleNextRetry(nextDelay, onRetry, onComplete)
        }
    }
    
    /**
     * 安排下次重试
     */
    private fun scheduleNextRetry(
        delay: Long,
        onRetry: () -> Unit,
        onComplete: (success: Boolean) -> Unit
    ) {
        retryRunnable = Runnable {
            executeRetry(onRetry, onComplete)
        }
        handler.postDelayed(retryRunnable!!, delay)
    }
    
    /**
     * 成功获取配置，停止重试
     */
    fun stopRetry() {
        retryRunnable?.let { handler.removeCallbacks(it) }
        retryRunnable = null
        retryCount = 0
    }
    
    /**
     * 取消所有重试
     */
    fun cancel() {
        retryRunnable?.let { handler.removeCallbacks(it) }
        retryRunnable = null
        retryCount = 0
    }
}
