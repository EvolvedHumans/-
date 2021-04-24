package serviceMqtt;

/**
 * @Author : YangFan
 * @Date : 2020年12月23日 17:49
 * @effect :
 */
public final class MqttConfig {


    public static Integer s = 0;


//    CPE1测试版配置：
//
//    接口配置
//    CPE后台地址：http://cpetest.dti2018.com
//    CPE配置接口地址：http://cpetest.dti2018.com/api/config
//    CPE日志接口地址: http://cpetest.dti2018.com/api/log
//    CPE心跳接口地址: http://cpetest.dti2018.com/api/heartbeat
//
//    MQTT配置
//    地址：tcp://47.99.180.14:1883
//    账号：admin
//    密码：Dti2018


    public static String DEBUG_MQTT_HOST = "tcp://47.99.180.14:1883";

    public static String DEBUG_RUNNING_HOST = "http://cpetest.dti2018.com/api/log";

    public static String DEBUG_HEART_HOST = "http://cpetest.dti2018.com/api/heartbeat";


    //    CPE1正式版配置：
//
//    接口配置
//    CPE后台地址：http://cpe.dti2018.com
//    CPE配置接口地址：http://cpe.dti2018.com/api/config
//    CPE日志接口地址: http://cpe.dti2018.com/api/log
//    CPE心跳接口地址: http://cpe.dti2018.com/api/heartbeat
//
//    MQTT配置
//    地址：tcp://47.98.228.90:1883
//    账号：admin
//    密码：Dti2018

    public static String MQTT_HOST = "tcp://47.98.228.90:1883";

    public static String RUNNING_HOST = "http://cpe.dti2018.com/api/log";

    public static String HEART_HOST = "http://cpe.dti2018.com/api/heartbeat";

    public static String mMqttHost(){
        if(s==0){
            return DEBUG_MQTT_HOST;
        }

        return MQTT_HOST;
    }


    public static String mRunningHost(){
        if(s==0){
            return DEBUG_RUNNING_HOST;
        }
        return RUNNING_HOST;
    }

    public static String mHeartHost(){
        if(s==0){
            return DEBUG_HEART_HOST;
        }
        return HEART_HOST;
    }



}
