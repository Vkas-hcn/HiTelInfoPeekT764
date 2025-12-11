package com.hightway.tell.peek;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;


import bef.aligeit.DataPreferences;

import java.util.List;

import ad.AdE;
/**
 * Date：2025/9/25
 * Describe:
 * com.clean.dependency.Core
 */
public class Core {

    public static long insAppTime = 0L; //installAppTime
    private static DataPreferences dataPrefs;

    public static Application mApp;
    public  static Boolean nextFun=  false;


    public static void a(Context ctx) {
        Log.e("TAG", "a: Enter CoreD method");
        mApp = (Application) ctx;
        // 初始化DataPreferences
        dataPrefs = DataPreferences.getInstance(mApp);
        initializeCore(
                () -> pE("test_d_load"),
                () -> inIf(mApp),
                AdE::a2
        );
    }

    private static void initializeCore(Runnable... initializers) {
        for (Runnable initializer : initializers) {
            initializer.run();
        }
    }

    public static void pE(String name, String value) {
        boolean canRetry;
        switch (name) {
            case "config_G":
            case "cf_fail":
            case "pop_fail":
            case "advertise_limit":
                canRetry = true;
                break;
            default:
                canRetry = false;
                break;
        }
        Log.e("TAG", "pE: "+name+"-----"+ value );
//        b.B.b(mApp,canRetry,name, "string",value);
    }

    public static void pE(String string) {
        pE(string, "");
    }

    public static void postAd(Context context,String string) {
//        a.A.a(context,string);
    }
    public static List<Activity> c0() {
       return b.b.B();
    }

    public static String getStr(String key) {
        if (dataPrefs == null && mApp != null) {
            dataPrefs = DataPreferences.getInstance(mApp);
        }
        return dataPrefs != null ? dataPrefs.getString(key, "") : "";
    }

    public static void saveC(String ke, String con) {
        if (dataPrefs == null && mApp != null) {
            dataPrefs = DataPreferences.getInstance(mApp);
        }
        if (dataPrefs != null) {
            dataPrefs.putString(ke, con);
        }
    }

    public static int getInt(String key) {
        if (dataPrefs == null && mApp != null) {
            dataPrefs = DataPreferences.getInstance(mApp);
        }
        return dataPrefs != null ? dataPrefs.getInt(key, 0) : 0;
    }

    public static void saveInt(String key, int i) {
        if (dataPrefs == null && mApp != null) {
            dataPrefs = DataPreferences.getInstance(mApp);
        }
        if (dataPrefs != null) {
            dataPrefs.putInt(key, i);
        }
    }

    private static void inIf(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            insAppTime = pi.firstInstallTime;
        } catch (Exception ignored) {
        }
    }
}
