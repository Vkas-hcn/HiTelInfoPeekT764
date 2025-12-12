package com.lecture.field.tell.ming

import android.app.Application
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.ming.laet.MisMis
import com.lecture.field.tell.ming.store.BeforeGetData

/**
 * 服务中心配置
 */
internal object ServiceHub {
    
    fun configure(app: Application, next: (Application) -> Unit) {
        MisMis.initAlly(app)
        BeforeGetData.initGetData(app)
        ConTool.startPeriodicService(app)
        next(app)
    }
}
