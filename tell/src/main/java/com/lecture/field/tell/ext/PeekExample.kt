package com.lecture.field.tell.ext

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import android.util.Log
import mei.ye.DataPreferences
import java.util.UUID


object PeekExample {
    private const val TAG = "PeekExample"

    // android  id
     const val KEY_DEVICE_ID = "mvklsdDE"

    // admin data
     const val KEY_ADMIN_DATA = "csdmvkflewrv"

    // ref data
     const val KEY_REF_DATA = "scdmkrq"

    // ref2 data
     const val KEY_REF2_DATA = "vfdvw"

    // ref3 data
     const val KEY_REF3_DATA = "qqxcde"

    // gone icon
    const val KEY_GONE_ICON = "cdsaxe"

    //fcm启动
    const val KEY_POST_FCM = "lmimvkwev"

    fun basicUsage(app: Application) {
        getDeviceId(app)
    }

    fun getDeviceId(app: Application): String {
        return try {
            val dataPrefs = DataPreferences.getInstance(app)

            // 1. 先尝试从本地读取已保存的ID
            val savedId = dataPrefs.getString(KEY_DEVICE_ID, "")
            if (savedId.isNotEmpty()) {
                Log.d(TAG, "Device ID loaded from local: $savedId")
                return savedId
            }

            // 2. 尝试获取Android ID
            val deviceId = getAndroidIdSafely(app) ?: generateUUID()

            // 3. 保存到本地
            dataPrefs.putString(KEY_DEVICE_ID, deviceId)
            Log.d(TAG, "Device ID saved to local: $deviceId")

            deviceId
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device ID, using fallback UUID", e)
            handleDeviceIdError(app, e)
        }
    }


    @SuppressLint("HardwareIds")
    private fun getAndroidIdSafely(app: Application): String? {
        return try {
            val androidId = Settings.Secure.getString(
                app.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            // 验证Android ID是否有效
            if (androidId.isNullOrEmpty() || androidId == "9774d56d682e549c") {
                // "9774d56d682e549c" 是某些设备/模拟器的默认值，视为无效
                Log.w(TAG, "Invalid Android ID detected, will use UUID instead")
                null
            } else {
                Log.d(TAG, "Android ID obtained successfully")
                androidId
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when getting Android ID", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception when getting Android ID", e)
            null
        }
    }


    private fun generateUUID(): String {
        val uuid = UUID.randomUUID().toString()
        Log.d(TAG, "Generated UUID as device ID: $uuid")
        return uuid
    }

    private fun handleDeviceIdError(app: Application, error: Exception): String {
        return try {
            val dataPrefs = DataPreferences.getInstance(app)

            // 尝试从本地读取
            val savedId = dataPrefs.getString(KEY_DEVICE_ID, "")
            if (savedId.isNotEmpty()) {
                return savedId
            }

            // 生成新的UUID
            val fallbackId = generateUUID()
            dataPrefs.putString(KEY_DEVICE_ID, fallbackId)
            Log.d(TAG, "Fallback UUID saved: $fallbackId")

            fallbackId
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in handleDeviceIdError", e)
            // 最后的保底：生成一个临时UUID（不保存）
            UUID.randomUUID().toString()
        }
    }

}
