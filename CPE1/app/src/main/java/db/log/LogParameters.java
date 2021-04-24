package db.log;

import android.Manifest;
import android.content.ContentProvider;
import android.content.Context;
import android.util.Log;

import db.log.sim.Phone;
import tool.Memory;
import tool.SDCard;
import tool.SignalIntensity;
import tool.Timestamp;

/**
 * @Author : YangFan
 * @Date : 2020年11月17日 14:17
 * @effect :
 */
public class LogParameters {

    public static HeartbeatLog Heart
            (Integer type,  Boolean status, Context context) throws InterruptedException {
        Thread.sleep(10000);
        Phone phone = new Phone(context);

        HeartbeatLog heartbeatLog = new HeartbeatLog();
        heartbeatLog.setType(type);//消息类型
        heartbeatLog.setCpeType(1);//CPE类型
        heartbeatLog.setUid(SignalIntensity.getSERIAL());//硬件标识
        heartbeatLog.setSimId(phone.getIccid());//SIM卡
        heartbeatLog.setTotalSize(SDCard.getAllSize());//存储空间
        heartbeatLog.setAvailableSize(SDCard.getAvailaleSize());//可用存储
        heartbeatLog.setAvailableMemory(Memory.Android_memory(context));//可用内存
        heartbeatLog.setPhoneNum(phone.getNativePhoneNumber());//手机号
        heartbeatLog.setStatus(status);//消息正常
        heartbeatLog.setTimestamp(Timestamp.timestamp());//时间戳

        heartbeatLog.save();

        return heartbeatLog;

    }

    public static void Running
            (Integer type, String data, Boolean states){
        RunningLog runningLog = new RunningLog();
        runningLog.setType(type);
        runningLog.setCpeType(1);
        runningLog.setUid(SignalIntensity.getSERIAL());
        runningLog.setData(data);
        runningLog.setStatus(states);
        runningLog.setTimestamp(Timestamp.timestamp());

        runningLog.save();

    }

}
