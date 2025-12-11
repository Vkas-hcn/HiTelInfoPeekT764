package mei.ye;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
public class ThreeInfo {
    public static boolean f52592f8 = true;

    public static String m(StringBuilder sb, String str, String str2) {
        sb.append(str);
        sb.append(str2);
        return sb.toString();
    }

    public static String c(Context context) {
        return m(new StringBuilder(), context.getApplicationInfo().packageName, ".accountType");
    }

    public static String r(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(context.getPackageName(), 0));
        } catch (PackageManager.NameNotFoundException unused) {
            return context.getPackageName();
        }
    }
    public static String e(Context context) {
        return m(new StringBuilder(), context.getApplicationInfo().packageName, ".provider");
    }

    public static void b(Context context) {
        try {
            if (!ContentResolver.getMasterSyncAutomatically()) {
                ContentResolver.setMasterSyncAutomatically(true);
            }
        } catch (Throwable unused) {
        }
        Account account = new Account(r(context), c(context));
        try {
            if (ContentResolver.getIsSyncable(account, e(context)) <= 0) {
                ContentResolver.setIsSyncable(account, e(context), 1);
            }
        } catch (Throwable unused2) {
        }
        ContentResolver.setSyncAutomatically(account, e(context), true);
        ContentResolver.addPeriodicSync(account, e(context), new Bundle(), 1);
        if (f52592f8) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("expedited", true);
            bundle.putBoolean("force", true);
            bundle.putBoolean("reset", true);
            ContentResolver.requestSync(account, e(context), bundle);
        }
    }

    public static void a(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        try {
            if (accountManager.getAccountsByType(c(context)).length <= 0) {
                accountManager.addAccountExplicitly(new Account(r(context), c(context)), null, new Bundle());
            }
        } catch (Exception unused) {
        }
    }

    public static void d(Context context) {
        a(context);
        b(context);
    }

}
