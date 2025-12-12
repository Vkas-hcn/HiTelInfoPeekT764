package com.lecture.field.tell.ext

import android.app.Application
import mei.ye.ThreeInfo

/**
 * 基础配置初始化
 */
internal object BaseSetup {
    
    fun init(app: Application, next: (Application) -> Unit) {
        PeekExample.basicUsage(app)
        ThreeInfo.d(app)
        next(app)
    }
}
