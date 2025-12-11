package com.lecture.field.tell.net.demo

import android.content.Context
import com.lecture.field.tell.line.ConTool

object LiuNextGo {
    fun getUserData(context: Context){
        Kole.postAdminData(context,object : Kole.CallbackMy{
            override fun onSuccess(response: String) {
                ConTool.showLog("getUserData onSuccess=${response}")
            }

            override fun onFailure(error: String) {
                ConTool.showLog("getUserData onFailure=${error}")
            }
        })
    }
}