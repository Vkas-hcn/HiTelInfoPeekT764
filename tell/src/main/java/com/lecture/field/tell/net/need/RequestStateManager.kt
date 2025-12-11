package com.lecture.field.tell.net.need

import com.lecture.field.tell.line.ConTool

/**
 * 请求状态管理器 - 负责控制请求状态，防止并发请求
 */
object RequestStateManager {
    
    // 是否正在请求
    @Volatile
    private var isRequesting = false
    
    // 是否正在等待配置
    @Volatile
    private var isWaitingForConfig = false
    
    /**
     * 检查是否可以发起新请求
     */
    fun canStartRequest(): Boolean {
        if (isRequesting) {
            ConTool.showLog("当前正在请求中，跳过本次请求")
            return false
        }
        if (isWaitingForConfig) {
            ConTool.showLog("正在等待配置，跳过本次请求")
            return false
        }
        return true
    }
    
    /**
     * 开始请求
     */
    fun startRequest() {
        isRequesting = true
        ConTool.showLog("开始请求...")
    }
    
    /**
     * 结束请求
     */
    fun endRequest() {
        isRequesting = false
        ConTool.showLog("请求结束")
    }
    
    /**
     * 设置等待配置状态
     */
    fun setWaitingForConfig(waiting: Boolean) {
        isWaitingForConfig = waiting
        ConTool.showLog("等待配置状态: $waiting")
    }
    
    /**
     * 重置所有状态
     */
    fun reset() {
        isRequesting = false
        isWaitingForConfig = false
        ConTool.showLog("重置请求状态")
    }
}
