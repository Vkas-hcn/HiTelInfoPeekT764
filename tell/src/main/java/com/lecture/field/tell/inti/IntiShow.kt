package com.lecture.field.tell.inti

import android.content.Context
import com.bytedance.sdk.openadsdk.api.PAGMUserInfoForSegment
import com.bytedance.sdk.openadsdk.api.init.PAGMConfig
import com.bytedance.sdk.openadsdk.api.init.PAGMSdk
import com.lecture.field.tell.line.ConTool

class IntiShow {
    fun initPang(context: Context) {
        try {
            ConTool.showLog("initPang:id=${GetTuXi.pangKey}")
            PAGMSdk.init(
                context, PAGMConfig.Builder()
                    .appId(GetTuXi.pangKey)
                    .setConfigUserInfoForSegment(
                        PAGMUserInfoForSegment.Builder()
                            .build()
                    ).supportMultiProcess(false).build(), null
            )
        } catch (error: Exception) {
            ConTool.showLog("Ad SDK initialization failed: ${error.message}")
        }
    }
}