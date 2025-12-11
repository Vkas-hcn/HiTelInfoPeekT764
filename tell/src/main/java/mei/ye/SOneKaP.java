package mei.ye;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SOneKaP extends Service {

    public C15520a8 f36278e8;

    public IBinder onBind(Intent intent) {
        return this.f36278e8.getIBinder();
    }

    public void onCreate() {
        super.onCreate();
        this.f36278e8 = new C15520a8(this);
    }
}