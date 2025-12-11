package mei.ye;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.SystemClock;

public class C15518a8 extends AbstractThreadedSyncAdapter {

    public C15518a8(Context context, boolean z) {
        super(context, z);
    }

    public void onPerformSync(Account account, Bundle bundle, String str, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        if (ThreeInfo.f52592f8) {
            boolean z = bundle.getBoolean("reset");
            TwoThing.f36274f8.removeCallbacksAndMessages("token");
            if (z) {
                syncResult.stats.numIoExceptions = 0;
                Bundle bundle2 = new Bundle();
                bundle2.putBoolean("expedited", true);
                bundle2.putBoolean("force", true);
                bundle2.putBoolean("reset", false);
                ContentResolver.requestSync(account, ThreeInfo.e(getContext()), bundle2);
                return;
            }
            syncResult.stats.numIoExceptions = 1;
            TwoThing.f36274f8.postAtTime(new RunnableC15519a8(account, getContext()), "token", SystemClock.uptimeMillis() + 20000);
        }
    }
}
