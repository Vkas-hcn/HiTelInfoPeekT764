package mei.ye;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public class TwoThing extends Service {
    public static final Handler f36274f8 = new Handler(Looper.getMainLooper());

    public C15518a8 f36275e8;

    public IBinder onBind(Intent intent) {
        return this.f36275e8.getSyncAdapterBinder();
    }

    public void onCreate() {
        super.onCreate();
        this.f36275e8 = new C15518a8(getApplicationContext(), true);
    }
}