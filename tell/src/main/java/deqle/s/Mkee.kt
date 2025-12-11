package deqle.s

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.lecture.field.tell.line.ConTool
import uwo.wo.he.WaiAili
import kotlin.jvm.javaClass

class Mkee : Application.ActivityLifecycleCallbacks {


    val activityStack = mutableListOf<Activity>()

    fun getActivityList(): List<Activity> {
        return activityStack
    }

    private var lastOpenTime = 0L

    fun oonn(context: Context) {
        if (ConTool.canLife && System.currentTimeMillis() - lastOpenTime < 60000 * 10) return
        lastOpenTime = System.currentTimeMillis()
        try {
            ConTool.showLog("startForegroundService oonn")

            ContextCompat.startForegroundService(
                context,
                Intent(context, WaiAili::class.java)
            )
        }catch (e: Exception) {
            ConTool.showLog("startForegroundService error: ${e.message}")
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityStack.add(activity)
        oonn(activity)
        ConTool.showLog("onActivityCreated: ${activity.javaClass.simpleName}")
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        activityStack.remove(activity)
    }


}