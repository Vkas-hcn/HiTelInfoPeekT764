package com.endless.boundaries.egelixi

import android.app.Application
import com.lecture.field.tell.ext.Peek
import mei.ye.ThreeInfo

class AllAppInfor: Application() {
    override fun onCreate() {
        super.onCreate()
//        ThreeInfo.d(this)
        Peek.geiMiru(this)
    }

}