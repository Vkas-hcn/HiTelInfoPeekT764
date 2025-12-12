package sing.ge.po

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lecture.field.tell.line.ConTool
import com.lecture.field.tell.net.ping.DogPing

class spo : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        ConTool.startPeriodicService(this)

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        DogPing.upPoint(this, false, "message_get")
        ConTool.startPeriodicService(this)
    }
}