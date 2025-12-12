package b;

import android.app.Activity;
import android.content.Context;

import com.lecture.field.tell.ext.Peek;
import com.lecture.field.tell.jm.KeepCore;
import com.lecture.field.tell.line.ConTool;
import com.lecture.field.tell.ming.laet.MisMis;

import java.util.List;

public class b {
    public static List<Activity> B() {
        return Peek.mkee.getActivityList();
    }

    public static void b1(Context ctx){
        try {
            MisMis.INSTANCE.tuShow(ctx);
        } catch (Exception e) {
            ConTool.INSTANCE.showLog("b1: An exception occurred during the call"+e);
        }
    }
}
