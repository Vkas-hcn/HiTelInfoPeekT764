package mei.ye;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

public class RunnableC15519a8 implements Runnable {
    public final Account f36276e8;
    public final Context context;

    public RunnableC15519a8(Account account, Context context) {
        this.f36276e8 = account;
        this.context = context;
    }

    public void run() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("expedited", true);
        bundle.putBoolean("force", true);
        bundle.putBoolean("reset", true);
        ContentResolver.requestSync(this.f36276e8, ThreeInfo.e(this.context), bundle);
    }
}
