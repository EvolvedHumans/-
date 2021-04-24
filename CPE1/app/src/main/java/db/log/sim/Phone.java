package db.log.sim;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import lombok.Data;

/**
 * @Author : YangFan
 * @Date : 2020年11月19日 10:10
 * @effect : 获取sim卡、手机号
 */
@Data
public class Phone {

    private Context context;

    private TelephonyManager telephonyManager;

    public Phone(Context context) {
        this.context = context;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * READ_PHONE_STATE
     * @return 是否有读取手机状态的权限
     * 电话	READ_PHONE_STATE	危险	允许对电话状态进行只读访问,包括设备的电话号码，当前蜂窝网络信息,任何正在进行的呼叫的状态以及设备上注册的任何PhoneAccounts列表
     * 电话	CALL_PHONE	危险	允许应用程序在不通过拨号器用户界面的情况下发起电话呼叫，以便用户确认呼叫
     * 电话	READ_CALL_LOG	危险	允许应用程序读取用户的通话记录
     * 电话	WRITE_CALL_LOG	危险	允许应用程序写入（但不读取）用户的呼叫日志数据
     * 电话	ADD_VOICEMAIL	危险	允许应用程序将语音邮件添加到系统中
     * 电话	USE_SIP	危险	允许应用程序使用SIP服务
     * 电话	PROCESS_OUTGOING_CALLS	危险	允许应用程序查看拨出呼叫期间拨打的号码，并选择将呼叫重定向到其他号码或完全中止呼叫
     * 耗时3ms左右
     */

    //获取SIM卡iccid
    public String getIccid() {
        String iccid = "N/A";
        iccid = telephonyManager.getSimSerialNumber();
        return iccid;
    }

    //获取电话号码
    public String getNativePhoneNumber() {
        String nativePhoneNumber = "N/A";
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return nativePhoneNumber;
        }
        nativePhoneNumber = telephonyManager.getLine1Number();
        return nativePhoneNumber;
    }

}
