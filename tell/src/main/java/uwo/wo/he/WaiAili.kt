package uwo.wo.he

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.lecture.field.tell.R
import com.lecture.field.tell.line.ConTool

class WaiAili : Service() {
    private var mNotification: Notification? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        ConTool.showLog("WaiAili onCreate")
        // 这个为demo类 创建前台服务，需要做一下差异化，不能直接就这样写了
        val channel = NotificationChannel(
            "peek",
            "peek Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
        mNotification = NotificationCompat.Builder(this, "peek").setAutoCancel(false)
            .setContentText("").setSmallIcon(R.drawable.cnjksa_nvjr).setOngoing(true)
            .setOnlyAlertOnce(true).setContentTitle("").setCategory(Notification.CATEGORY_CALL)
            .setCustomContentView(RemoteViews(packageName, R.layout.dimo_thing)).build()
        ConTool.canLife = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching {
            // id需要修改
            startForeground(5547, mNotification)
        }
        return START_STICKY  // 必须用这个模式
    }

    override fun onDestroy() {
        ConTool.canLife = false
        ConTool.showLog("WaiAili onDestroy")
        super.onDestroy()
    }
}