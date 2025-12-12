package uwo.wo.he

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.lecture.field.tell.R
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.net.ping.DogPing

class WaiAili : Service() {
    
    private companion object {
        // 动态生成ID避免硬编码
        fun genChannelId() = "ch_${System.currentTimeMillis() % 10000}"
        fun genNotifyId(pkg: String) = (pkg.hashCode() and 0x7FFF) + 1000
    }
    
    private val channelId by lazy { genChannelId() }
    private val notifyId by lazy { genNotifyId(packageName) }
    private var notification: Notification? = null
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        runCatching {
            ConTool.showLog("WaiAili启动")
            DogPing.upPoint(this, false, "foreground_p")

            notification = createNotification()?.also { ConTool.canLife = true }
        }.onFailure { 
            ConTool.showLog("WaiAili初始化异常: ${it.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = 
        runCatching {
            notification?.let { startForeground(notifyId, it) }
                ?: ConTool.showLog("通知对象为空，无法启动前台服务")
            START_STICKY
        }.getOrElse {
            ConTool.showLog("启动前台服务失败: ${it.message}")
            START_NOT_STICKY
        }

    override fun onDestroy() {
        runCatching {
            ConTool.canLife = false
            notification = null
            ConTool.showLog("WaiAili销毁")
        }.onFailure {
            ConTool.showLog("销毁异常: ${it.message}")
        }.also { super.onDestroy() }
    }
    
    private fun createNotification(): Notification? = runCatching {
        val chName = packageName.split(".").lastOrNull()?.uppercase() ?: "SVC"
        
        // 创建通知渠道（防御式检查）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.apply {
                createNotificationChannel(
                    NotificationChannel(channelId, chName, NotificationManager.IMPORTANCE_LOW)
                        .apply { setShowBadge(false) }
                )
            } ?: run {
                ConTool.showLog("获取NotificationManager失败")
                return null
            }
        }
        
        // 构建通知（简洁风格）
        NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(R.drawable.cnjksa_nvjr)
            setContentTitle("")
            setContentText("")
            setAutoCancel(false)
            setOngoing(true)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_MIN
            setCategory(Notification.CATEGORY_SERVICE)
            packageName?.let { setCustomContentView(RemoteViews(it, R.layout.dimo_thing)) }
        }.build()
    }.getOrElse {
        ConTool.showLog("创建通知失败: ${it.message}")
        null
    }
}