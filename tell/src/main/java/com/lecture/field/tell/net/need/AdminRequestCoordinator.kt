package com.lecture.field.tell.net.need

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.net.demo.Kole

/**
 * Admin请求协调器 - 整合所有请求逻辑
 */
class AdminRequestCoordinator(private val context: Context) {
    
    private val scheduleManager = ScheduleManager(context)
    private val retryManager = RetryManager(context)
    private var onUserACallback: (() -> Unit)? = null
    
    // 标记goToCOreA是否已经被调用过（全程只调用一次）
    private var hasCalledGoToCOreA = false
    
    // 标记B用户定时请求是否已经启动（避免重复启动）
    private var hasStartedPeriodicRequest = false
    
    /**
     * 执行单次请求（带重试）
     */
    private fun executeRequestWithRetry(onComplete: ((success: Boolean) -> Unit)? = null) {
        // 检查是否可以发起请求
        if (!RequestStateManager.canStartRequest()) {
            onComplete?.invoke(false)
            return
        }
        
        // 检查是否达到每日上限
        if (RequestCounter.hasReachedDailyLimit(context)) {
            onComplete?.invoke(false)
            return
        }
        
        // 设置请求状态
        RequestStateManager.startRequest()
        
        // 增加请求计数
        RequestCounter.incrementRequestCount(context)
        
        // 发起请求
        Kole.postAdminData(context, object : Kole.CallbackMy {
            override fun onSuccess(response: String) {
                RequestStateManager.endRequest()
                retryManager.stopRetry()
                
                // 判断用户类型
                if (ConTool.canUser(response)) {
                    // a用户

                    // 只有在未调用过的情况下才调用goToCOreA
                    if (!hasCalledGoToCOreA) {
                        hasCalledGoToCOreA = true
                        onUserACallback?.invoke()
                    }
                    
                    scheduleManager.cancelAll()
                    onComplete?.invoke(true)
                } else {
                    // b用户，继续定时请求

                    // 只在第一次检测到b用户时启动定时请求
                    if (!hasStartedPeriodicRequest) {
                        hasStartedPeriodicRequest = true
                        // 刚才已经请求过了，不立即执行，等待间隔
                        startUserBPeriodicRequest(executeImmediately = false)
                    }
                    
                    onComplete?.invoke(true)
                }
            }
            
            override fun onFailure(error: String) {
                RequestStateManager.endRequest()
                
                // 失败后不自动重试，由重试管理器统一处理
                onComplete?.invoke(false)
            }
        })
    }
    
    /**
     * 启动B用户的定时循环请求
     * @param executeImmediately 首次是否立即执行
     */
    private fun startUserBPeriodicRequest(executeImmediately: Boolean = true) {
        val interval = ConfigManager.getUserBInterval(context)

        scheduleManager.startPeriodicRequest(interval, executeImmediately) {
            // 每次定时到达时，执行请求
            if (!RequestStateManager.canStartRequest()) {
                return@startPeriodicRequest
            }
            
            if (RequestCounter.hasReachedDailyLimit(context)) {
                scheduleManager.cancelPeriodicRequest()
                return@startPeriodicRequest
            }
            
            // 执行请求，失败时自动重试1次
            executeRequestWithRetry { success ->
                if (!success) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!RequestCounter.hasReachedDailyLimit(context)) {
                            executeRequestWithRetry()
                        }
                    }, 1000)
                }
            }
        }
    }
    
    /**
     * 情况1：有A配置，先调用goToCOreA，然后延迟后请求
     */
    private fun handleCaseUserA() {

        // 先立即调用goToCOreA（只调用一次）
        if (!hasCalledGoToCOreA) {
            hasCalledGoToCOreA = true
            onUserACallback?.invoke()
        }
        
        // 然后启动延迟请求
        scheduleManager.startDelayedRequest {
            retryManager.startRetry(
                onRetry = {
                    executeRequestWithRetry()
                },
                onComplete = { success ->
                    if (!success) {
                        ConTool.showLog("Delayed request retry failed")
                    }
                }
            )
        }
    }
    
    /**
     * 情况2：有B配置，立即开始定时请求
     */
    private fun handleCaseUserB() {
        startUserBPeriodicRequest()
    }
    
    /**
     * 情况3：没有配置，立即请求
     */
    private fun handleCaseNoConfig() {
        retryManager.startRetry(
            onRetry = {
                executeRequestWithRetry()
            },
            onComplete = { success ->
                if (!success) {
                    ConTool.showLog("First request retry failed")
                }
            }
        )
    }
    
    /**
     * 主入口：根据配置情况选择处理策略
     */
    fun start(onUserA: () -> Unit) {
        this.onUserACallback = onUserA
        
        ConTool.showLog("AdminRequestCoordinator启动")
        
        // 重置状态
        RequestStateManager.reset()
        
        // 检查是否有配置
        val hasConfig = ConfigManager.hasConfig(context)
        
        if (!hasConfig) {
            // 情况3：没有配置
            handleCaseNoConfig()
        } else {
            // 有配置，判断是A用户还是B用户
            val isUserA = ConfigManager.isUserA(context)
            
            if (isUserA) {
                // 情况1：A配置用户
                handleCaseUserA()
            } else {
                // 情况2：B配置用户
                handleCaseUserB()
            }
        }
    }
    
    /**
     * 停止所有请求
     */
    fun stop() {
        scheduleManager.cancelAll()
        retryManager.cancel()
        RequestStateManager.reset()
        // 注意：不重置hasCalledGoToCOreA，确保全程只调用一次
    }
}
