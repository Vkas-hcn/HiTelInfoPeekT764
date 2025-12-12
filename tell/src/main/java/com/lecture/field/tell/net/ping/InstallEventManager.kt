package com.lecture.field.tell.net.ping

import android.content.Context
import com.lecture.field.tell.ext.PeekExample
import com.lecture.field.tell.line.ConTool
import mei.ye.DataPreferences

/**
 * 安装事件管理器
 * 负责管理upInstall一次性事件的逻辑
 */
object InstallEventManager {
    
    // SharedPreferences键名
    private const val KEY_INSTALL_REPORTED = "dog_ping_install_reported"
    private const val KEY_INSTALL_DATA = "dog_ping_install_data"
    
    /**
     * 检查安装事件是否已成功上报
     */
    fun isInstallReported(context: Context): Boolean {
        val reported = DataPreferences.getInstance(context)
            .getBoolean(KEY_INSTALL_REPORTED, false)
        ConTool.showLog("[InstallEvent] 检查上报状态: ${if (reported) "已上报" else "未上报"}")
        return reported
    }
    
    /**
     * 标记安装事件已成功上报
     */
    fun markInstallReported(context: Context) {
        DataPreferences.getInstance(context)
            .putBoolean(KEY_INSTALL_REPORTED, true)
        ConTool.showLog("[InstallEvent] 标记为已上报")
    }
    
    /**
     * 获取或生成安装事件数据
     * 如果已有保存的数据则返回保存的，否则生成新数据并保存
     */
    fun getOrCreateInstallData(context: Context): String {
        val prefs = DataPreferences.getInstance(context)
        var savedData = prefs.getString(KEY_INSTALL_DATA, "")
        
        if (savedData.isEmpty()) {
            // 首次生成安装数据
            savedData = DogData.upInstallJson(context)
            prefs.putString(KEY_INSTALL_DATA, savedData)
            ConTool.showLog("[InstallEvent] 首次生成并保存安装数据")
        } else {
            ConTool.showLog("[InstallEvent] 使用已保存的安装数据")
        }
        
        return savedData
    }

}
