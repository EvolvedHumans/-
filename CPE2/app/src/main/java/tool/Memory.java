package tool;

import android.app.ActivityManager;
import android.content.Context;

public class Memory {
    /**
     * 获取系统的可用内存
     * @param context
     * @return
     */
    public static long Android_memory(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(outInfo);
        return outInfo.availMem/(1024*1024);
    }
}
