package com.lecture.field.tell.ffff

import android.app.Application
import com.lecture.field.tell.ffff.goods.CrewTool

/**
 * 模块激活器
 */
internal object ModuleActivator {
    
    fun activate(app: Application) {
        FirebaseShow.firebaseShowFun(app)
        CrewTool.startAllKeepAlive(app)
        FirebaseShow.queq(app)
    }
}
