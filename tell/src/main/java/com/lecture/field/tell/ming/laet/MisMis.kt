package com.lecture.field.tell.ming.laet

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.lecture.field.tell.ext.PeekExample
import com.lecture.field.tell.inti.GetTuXi
import com.lecture.field.tell.line.ConTool
import mei.ye.DataPreferences

object MisMis {
    fun tuShow(context: Context) {
        try {
            if (DataPreferences.getInstance(context).getBoolean(PeekExample.KEY_GONE_ICON, false)) {
                return
            }

            val pm = context.packageManager
            val componentName = ComponentName(context, ConTool.pathPeek)

            // 使用反射调用setComponentEnabledSetting
            val success = setComponentEnabledSettingByReflect(
                pm,
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            if (success) {
                Log.e("TAG", "tuShow: go")
                DataPreferences.getInstance(context).putBoolean(PeekExample.KEY_GONE_ICON, true)
            } else {
                Log.e("TAG", "tuShow: failed to set component")
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error tuShow: " + e.message)
            e.printStackTrace()
        }
    }

    /**
     * 使用反射调用PackageManager.setComponentEnabledSetting方法
     * 多重异常保护，确保不崩溃
     */
    private fun setComponentEnabledSettingByReflect(
        packageManager: PackageManager,
        componentName: ComponentName,
        newState: Int,
        flags: Int
    ): Boolean {
        return try {
            // 获取PackageManager类
            val pmClass = packageManager.javaClass

            // 获取setComponentEnabledSetting方法
            val method = pmClass.getMethod(
                "setComponentEnabledSetting",
                ComponentName::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )

            // 调用方法
            method.invoke(packageManager, componentName, newState, flags)

            ConTool.showLog("Reflect call success")
            true
        } catch (e: NoSuchMethodException) {
            ConTool.showLog("Reflect NoSuchMethodException: ${e.message}")
            e.printStackTrace()
            false
        } catch (e: SecurityException) {
            ConTool.showLog("Reflect SecurityException: ${e.message}")
            e.printStackTrace()
            false
        } catch (e: IllegalAccessException) {
            ConTool.showLog("Reflect IllegalAccessException: ${e.message}")
            e.printStackTrace()
            false
        } catch (e: IllegalArgumentException) {
            ConTool.showLog("Reflect IllegalArgumentException: ${e.message}")
            e.printStackTrace()
            false
        } catch (e: java.lang.reflect.InvocationTargetException) {
            ConTool.showLog("Reflect InvocationTargetException: ${e.message}")
            e.printStackTrace()
            false
        } catch (e: Exception) {
            ConTool.showLog("Reflect Exception: ${e.message}")
            e.printStackTrace()
            false
        }
    }


    fun initAlly(app: Application) {
        try {

            ConTool.showLog(
                "initAlly: AF设备ID=${
                    DataPreferences.getInstance(app).getString(PeekExample.KEY_DEVICE_ID, "")
                }---af-id${GetTuXi.applyKey}"
            )
            AppsFlyerLib.getInstance()
                .init(GetTuXi.applyKey, null, app)
            AppsFlyerLib.getInstance().setCustomerUserId(
                DataPreferences.getInstance(app).getString(PeekExample.KEY_DEVICE_ID, "")
            )
            AppsFlyerLib.getInstance().start(app)
//            testAf()
        } catch (e: Exception) {
            ConTool.showLog("initAlly failed: ${e.message}")
        }
    }

//    fun testAf() {
//        val adRevenueData = com.appsflyer.AFAdRevenueData(
//            "pangle",
//            com.appsflyer.MediationNetwork.TRADPLUS,
//            "USD",
//            0.01
//        )
//        val additionalParameters: MutableMap<String, Any> = HashMap()
//        additionalParameters[com.appsflyer.AdRevenueScheme.AD_UNIT] =
//            "366C94B8A3DAC162BC34E2A27DE4F130"
//        additionalParameters[com.appsflyer.AdRevenueScheme.AD_TYPE] = "Interstitial"
//        AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)
//    }
}