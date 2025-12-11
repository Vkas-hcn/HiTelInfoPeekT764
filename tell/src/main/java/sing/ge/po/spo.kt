package sing.ge.po

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lecture.field.tell.line.ConTool

class spo : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        ConTool.startPeriodicService(this)

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        ConTool.startPeriodicService(this)
    }
}