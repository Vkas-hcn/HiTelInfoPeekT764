package com.lecture.field.tell.net.demo

import android.content.Context
import kotlin.random.Random

object DuoFun {
    fun postPointFun(
        context: Context,
        canRetry: Boolean,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null
    ) {
    }

    fun ConfigG(context: Context,typeUser: Boolean, codeInt: String?) {
        val isuserData: String? = if (codeInt == null) {
            null
        } else if (codeInt != "200") {
            codeInt
        } else if (typeUser) {
            "a"
        } else {
            "b"
        }
        postPointFun(
            context,
            true,
            "config_G",
            "getstring",
            isuserData
        )
    }
}