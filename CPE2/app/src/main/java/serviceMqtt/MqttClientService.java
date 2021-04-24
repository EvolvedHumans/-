package serviceMqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.PackageSerialPort.EncapsulationFormat;
import db.agreement.CpeMqttTopicVo;
import db.agreement.DataTransmissionProtocol;
import db.analyticMethod.InitCpeAnalysis;
import db.log.LogParameters;
import lombok.Data;
import lombok.SneakyThrows;
import tool.SignalIntensity;
import tool.Timestamp;

@Data
public class MqttClientService extends Service {

    /**
     * 必需参数
     */
    private boolean serviceSecurity = false; //初始值不安全
    private Timer timer = null; //服务安全定时器

    /**
     * 基本信息获取
     */
    private String host;
    private String userName;
    private String passWord;
    private String clientId;

    /**
     * 订阅主题获取
     */
    private List<CpeMqttTopicVo> topicList = new LinkedList<CpeMqttTopicVo>();

    /**
     * MQTT通讯,唯一一个对象
     */
    private static MqttClient mqttClient = null;

    /**
     * 设备Uid
     */
    EquipmentIdentification equipmentIdentification = null;

    /**
     * 消息读取器
     */
    private SharedPreferences sharedPreferences = null;

    private SharedPreferences.Editor editor = null;

    /**
     * 戴天外网请求标志位
     */
    private static boolean dtiStateThread = false;

    /**
     * 传输信息接口
     */
    private IGetMqttClientMessageCallBack iGetMqttMessageCallBack;

    /**
     * 发送MQTT消息
     */
    public static void send(String topic, String data,Integer qos){
        try {
             mqttClient.publish(topic, data,qos);
        }catch (Exception exception){
            exception.getStackTrace();
        }
    }

    /**
     * 初始化MQTT通信
     */
    public void init(){

        sharedPreferences = getSharedPreferences("data",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        new InitThread().start();
    }

    /**
     * 当调用了startService()方法则会自动开启onStartCommand
     */
    public static void startService(Context mContext) {
        mContext.startService(new Intent(mContext, MqttClientService.class));
    }

    /**
     * 当调用了startService()方法则会自动关闭onStartCommand
     */
    public static void stopService(Context context) throws MqttException {
        Intent iService = new Intent(context, MqttClientService.class);
        iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.stopService(iService);
        mqttClient.disConnected();
        Log.e("MQTT", "关闭服务");
    }

    @SneakyThrows
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //串口通讯拿到信息以键值对形式存储->开启后台服务->后台获取CPE1设备标识和CPE2设备标识->调用init()动态访问

        init();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        Log.e("关闭MQTT","完毕");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new CustomBinder();
    }

    /**
     * 让IBinder调用UdpService，从而让其他类能够获取到UdpService服务
     */
    class CustomBinder extends Binder {
        public MqttClientService getMqttClientService() {
            return MqttClientService.this;
        }
    }

    /**
     * 安全位定时器
     */
    class TimerSecurity extends TimerTask {
        @Override
        public void run() {
            switch (mqttClient.getMqtt()) {
                //传给MainActivity
                case 0:iGetMqttMessageCallBack.mqttServiceSecurity(false);break;
                case 1:iGetMqttMessageCallBack.mqttServiceSecurity(true); break;
                case 2:iGetMqttMessageCallBack.mqttServiceSecurity(false);break;
            }
        }
    }

    /**
     * 初始化线程
     */
    class InitThread extends Thread{
        @SneakyThrows
        @Override
        public void run() {

            while (!sharedPreferences.getBoolean("judge",false)){
                Log.e("初始化配置失败","请重新配置！");
                return;
            }
            editor.putBoolean("judge",false);

            //1.配置获取基本信息
            host = sharedPreferences.getString
                    ("host","未读取到"); //ip 47.99.180.14 端口1883
            userName = sharedPreferences.getString
                    ("username","未读取到"); //用户名：admin
            passWord = sharedPreferences.getString
                    ("password","未读取到"); //密码：Dti2018
            //clientId = SignalIntensity.getSERIAL();//客户端标识
           // clientId = "D3883591oJ7Cg";
            clientId = sharedPreferences.getString("clientId", Timestamp.timestamp_string());

            Log.e("host",host);
            Log.e("userName",userName);
            Log.e("passWord",passWord);
            Log.e("clientId",clientId);

            //将JSON字符串转化为List集合
//            TypeToken<List<CpeMqttTopicVo>> typeToken = null;
            topicList =
                    InitCpeAnalysis.jsonToList
                            (sharedPreferences.getString("topicNameList",""));

            editor.apply();

            //2.MqttClient通讯类初始化配置
            mqttClient = new MqttClient(); //实例化通信MqttClient通讯类
            mqttClient.setContext(MqttClientService.this); //上下文
            mqttClient.setHost(host); //地址
            mqttClient.setUserName(userName); //账号
            mqttClient.setPassWord(passWord); //密码
            mqttClient.setClientId(clientId); //客户端ID
            mqttClient.setTopicList(topicList); //订阅主题集合
            mqttClient.isConnectService(); //连接服务端

            /**
             * CPE1中不需要区分主题,接口中不需要待主题
             */
            mqttClient.setOnMqttClientDataReceiveListener(new MqttClient.OnMqttClientDataReceiveListener() {
                @Override
                public void onDataReceive(String topic, String message) {

                    //TODO 1.底层数据封装
                    DataTransmissionProtocol dataTransmissionProtocol = new DataTransmissionProtocol();
                    dataTransmissionProtocol.setProtocolType(1);
                    dataTransmissionProtocol.setCpeTwoUid(SignalIntensity.getSERIAL());
                    dataTransmissionProtocol.setTopicName(topic);
                    dataTransmissionProtocol.setContent(message);

                    //TODO 2.转JSON字符串
                    message = InitCpeAnalysis.toJsonString(dataTransmissionProtocol);

                    //TODO 在这里进行数据的封装
                    iGetMqttMessageCallBack.onMqttReceive
                            (topic,EncapsulationFormat.initializationEncapsulation(message,1));

                }
            });

            timer = new Timer(); //服务安全
            timer.schedule(new TimerSecurity(),3000,3000); //开启每3秒一次
        }
    }
}
