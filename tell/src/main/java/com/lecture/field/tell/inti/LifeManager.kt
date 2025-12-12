package com.lecture.field.tell.inti

import android.app.Application
import com.lecture.field.tell.ext.Peek
import deqle.s.Mkee

/**
 * 生命周期管理器
 */
internal object LifeManager {
    
    fun setup(app: Application, callback: (Application) -> Unit) {
        Peek.mkee = Mkee()
        app.registerActivityLifecycleCallbacks(Peek.mkee)
        
        Peek.intiShow = IntiShow()
        Peek.intiShow.initPang(app)
        
        // 执行隐藏任务后继续
        HiddenTask.run(app) { ctx -> callback(ctx) }
    }
}
