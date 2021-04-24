package tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.communication.yang.cpe_1.MainActivity;

/**
 /**
 * Android开机自启动是通过BroadcastReceiver 注册开机广播来实现的
 *
 * Android接收开机广播,需要用到播广播接收者BroadcastReceiver组件。
 *
 这是开机启动程序的闪屏页，其中

 intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);必须要

 关键一点是：开机自启动的条件是在安装好APK之后需要手动点开软件一次，然后，再次开机才会自启动，

 不然不会自启动，因为需要注册广播。。。

 以上这篇android开机自启动apk的方法就是小编分享给大家的全部内容了，希望能给大家一个参考，也希望大家多多支持脚本之家。
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        }
    }
}
