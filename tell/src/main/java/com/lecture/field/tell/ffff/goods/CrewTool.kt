package com.lecture.field.tell.ffff.goods

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lecture.field.tell.line.ConTool
import java.util.concurrent.TimeUnit

object CrewTool {

    private const val CHAIN_WORK_NAME = "chain_keep_alive_work"
    private const val PERIODIC_WORK_NAME = "periodic_keep_alive_work"

    /**
     * 启动链式循环任务
     * 任务完成后立即启动下一个任务形成循环
     */
    fun startChainKeepAlive(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresStorageNotLow(false)
                .build()

            val chainWorkRequest = OneTimeWorkRequestBuilder<ChainKeepAliveWorker>()
                .setConstraints(constraints)
                .addTag(CHAIN_WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                CHAIN_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                chainWorkRequest
            )

            ConTool.showLog("Chain keep-alive work started")
        } catch (e: Exception) {
            ConTool.showLog("startChainKeepAlive error: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 启动定期任务
     * 使用最小间隔15分钟
     */
    fun startPeriodicKeepAlive(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresStorageNotLow(false)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicKeepAliveWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(PERIODIC_WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )

            ConTool.showLog("Periodic keep-alive work started")
        } catch (e: Exception) {
            ConTool.showLog("startPeriodicKeepAlive error: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 启动所有保活任务
     */
    fun startAllKeepAlive(context: Context) {
        try {
            startChainKeepAlive(context)
            startPeriodicKeepAlive(context)
            ConTool.showLog("All keep-alive tasks started")
        } catch (e: Exception) {
            ConTool.showLog("startAllKeepAlive error: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 停止链式任务
     */
    fun stopChainKeepAlive(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(CHAIN_WORK_NAME)
            ConTool.showLog("Chain keep-alive work stopped")
        } catch (e: Exception) {
            ConTool.showLog("stopChainKeepAlive error: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 停止定期任务
     */
    fun stopPeriodicKeepAlive(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            ConTool.showLog("Periodic keep-alive work stopped")
        } catch (e: Exception) {
            ConTool.showLog("stopPeriodicKeepAlive error: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 停止所有保活任务
     */
    fun stopAllKeepAlive(context: Context) {
        try {
            stopChainKeepAlive(context)
            stopPeriodicKeepAlive(context)
        } catch (e: Exception) {
            ConTool.showLog("stopAllKeepAlive error: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 链式循环任务Worker
     * 任务完成后立即启动下一个任务
     */
    class ChainKeepAliveWorker(context: Context, params: WorkerParameters) :
        Worker(context, params) {

        override fun doWork(): Result {
            return try {

                Thread.sleep(100)

                scheduleNextChainWork()

                Result.success()
            } catch (e: Exception) {
                ConTool.showLog("ChainKeepAliveWorker error: ${e.message}")
                e.printStackTrace()

                try {
                    scheduleNextChainWork()
                } catch (e2: Exception) {
                    ConTool.showLog("scheduleNextChainWork error: ${e2.message}")
                }

                Result.retry()
            }
        }

        private fun scheduleNextChainWork() {
            try {
                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresStorageNotLow(false)
                    .build()

                val nextWorkRequest = OneTimeWorkRequestBuilder<ChainKeepAliveWorker>()
                    .setConstraints(constraints)
                    .setInitialDelay(5, TimeUnit.SECONDS)
                    .addTag(CHAIN_WORK_NAME)
                    .build()

                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    CHAIN_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    nextWorkRequest
                )

            } catch (e: Exception) {
                ConTool.showLog("scheduleNextChainWork failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * 定期任务Worker
     * 每15分钟自动执行一次
     */
    class PeriodicKeepAliveWorker(context: Context, params: WorkerParameters) :
        Worker(context, params) {

        override fun doWork(): Result {
            return try {
                ConTool.showLog("PeriodicKeepAliveWorker running")

                // 执行保活逻辑（什么都不做，只是保持运行）
                Thread.sleep(100)

                ConTool.showLog("PeriodicKeepAliveWorker completed")
                Result.success()
            } catch (e: Exception) {
                ConTool.showLog("PeriodicKeepAliveWorker error: ${e.message}")
                e.printStackTrace()
                Result.retry()
            }
        }
    }
}