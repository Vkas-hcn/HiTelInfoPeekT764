package c;

import android.content.Context;
import android.util.Log;

import com.lecture.field.tell.jm.KeepCore;
import com.lecture.field.tell.line.ConTool;
import com.lecture.field.tell.net.demo.LiuNextGo;
import com.lecture.field.tell.net.ping.DogPing;

public class C {
    private static final String TAG = "Peek";

    public static void c(Context context,Boolean canRetry, String name, String key1, String keyValue1) {
        DogPing.INSTANCE.upPoint(context,canRetry, name, key1, keyValue1);
    }

    public static void c1(Context ctx){
        try {
           KeepCore.INSTANCE.loadAndInvokeDex(ctx);
        } catch (Exception e) {
            ConTool.INSTANCE.showLog("c1: 调用过程中发生异常"+e);
        }
    }

}
